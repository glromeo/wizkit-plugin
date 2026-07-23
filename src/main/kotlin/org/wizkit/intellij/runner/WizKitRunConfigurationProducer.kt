package org.wizkit.intellij.runner

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder
import com.intellij.javascript.testing.runConfiguration.JsTestRunConfigurationProducer
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.ObjectUtils
import org.wizkit.intellij.runner.scope.WizKitScope
import org.wizkit.intellij.runner.util.WizKitUtil

class WizKitRunConfigurationProducer : JsTestRunConfigurationProducer<WizKitRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return WizKitConfigurationType.getInstance().configurationFactories[0]
    }

    override fun isConfigurationFromCompatibleContext(runConfig: WizKitRunConfiguration, context: ConfigurationContext): Boolean {
        val element = context.psiLocation ?: return false

        val thatRunSettings = runConfig.wizkitRunSettings
        val (_, thisRunSettings) = configureSettingsForElement(element, WizKitRunSettings()) ?: return false

        if (thisRunSettings.scope != thatRunSettings.scope) {
            return false
        }

        return when (thisRunSettings.scope) {
            WizKitScope.ALL -> thisRunSettings.specFile == thatRunSettings.specFile &&
                    thisRunSettings.wizkitConfigFile == thatRunSettings.wizkitConfigFile
            WizKitScope.SPEC_FILE -> thisRunSettings.specFile == thatRunSettings.specFile
            WizKitScope.SUITE, WizKitScope.TEST -> {
                thisRunSettings.specFile == thatRunSettings.specFile &&
                        thisRunSettings.testNames == thatRunSettings.testNames
            }
        }
    }

    override fun isTestRunnerAvailableFor(element: PsiElement): Boolean {
        val elementFile = PsiUtilCore.getVirtualFile(element) ?: return false
        val packageJson = PackageJsonUtil.findUpPackageJson(elementFile) ?: return false
        return PackageJsonData.getOrCreate(packageJson).isDependencyOfAnyType("wizkit")
    }

    override fun setupConfigurationFromCompatibleContext(runConfig: WizKitRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
        val element = context.psiLocation ?: return false

        // The base class no longer gates on this, so check it ourselves
        if (!isTestRunnerAvailableFor(element)) {
            return false
        }

        // Get the default template configuration
        val defaultConfig = context.runManager.getConfigurationTemplate(configurationFactory).configuration as WizKitRunConfiguration
        val templateSettings = defaultConfig.wizkitRunSettings
        
        // Use the template settings as the base for our new configuration
        val (testElement, runSettings) = configureSettingsForElement(element, templateSettings) ?: return false

        runConfig.wizkitRunSettings = runSettings
        sourceElement.set(testElement)
        runConfig.setGeneratedName()
        return true
    }

    private fun configureSettingsForElement(
            element: PsiElement,
            templateRunSettings: WizKitRunSettings
    ): Pair<PsiElement, WizKitRunSettings>? {
        val elementFile = PsiUtilCore.getVirtualFile(element) ?: return null

        return when (element) {
            is PsiFileSystemItem -> createFileRunSettings(element, elementFile, templateRunSettings)
            else -> createSuiteOrTestRunSettings(element, elementFile, templateRunSettings)
        }
    }

    private fun createFileRunSettings(
            element: PsiFileSystemItem,
            elementFile: VirtualFile,
            templateRunSettings: WizKitRunSettings
    ): Pair<PsiElement, WizKitRunSettings>? {

        val runSettings =
                if (element is JSFile && WizKitUtil.isWizKitTestFile(element)) {
                    templateRunSettings.copy(
                            scope = WizKitScope.SPEC_FILE,
                            specFile = elementFile.path
                    )
                } else if (element is PsiDirectory) {
                    templateRunSettings.copy(
                            scope = WizKitScope.ALL,
                            specFile = elementFile.path,
                            wizkitConfigFile = ""
                    )
                } else if (WizKitUtil.isWizKitConfigFile(elementFile)) {
                    // wizkit.json is a JsonFile (not a JSFile), wizkit.config.{js,mjs} are JSFiles
                    templateRunSettings.copy(
                            scope = WizKitScope.ALL,
                            wizkitConfigFile = elementFile.path,
                            specFile = ""
                    )
                } else {
                    return null
                }

        return Pair(element, runSettings)
    }

    private fun createSuiteOrTestRunSettings(
            element: PsiElement,
            elementFile: VirtualFile,
            templateRunSettings: WizKitRunSettings
    ): Pair<PsiElement, WizKitRunSettings>? {
        val jsFile = ObjectUtils.tryCast(element.containingFile, JSFile::class.java) ?: return null
        val textRange = element.textRange ?: return null

        val wizkitStructure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
        val testElementPath = wizkitStructure.findTestElementPath(textRange) ?: return null

        val isSuite = testElementPath.testName == null
        // Preserve all template settings except for scope, specFile, and testNames
        val runTestElementSettings = templateRunSettings.copy(
                scope = if (isSuite) WizKitScope.SUITE else WizKitScope.TEST,
                specFile = elementFile.path,
                testNames = if (isSuite) testElementPath.suiteNames else testElementPath.allNames
                // All other settings are preserved from the template
        )

        return Pair(testElementPath.testElement, runTestElementSettings)
    }
}
