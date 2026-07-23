package org.wizkit.intellij.runner

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.wizkit.intellij.runner.scope.WizKitScope
import java.io.File

class WizKitRunConfigurationProducerTest : BasePlatformTestCase() {

    private lateinit var configProducer: RunConfigurationProducer<WizKitRunConfiguration>
    private lateinit var configFactory: ConfigurationFactory

    public override fun setUp() {
        super.setUp()

        configProducer = RunConfigurationProducer.getInstance(WizKitRunConfigurationProducer::class.java)
        configFactory = WizKitConfigurationType.getInstance().configurationFactories[0]
    }

    override fun getTestDataPath(): String = File("src/test/resources/testData").absolutePath

    fun `test does not setup if wizkit dependency missing`() {
        assertNull(setupRunConfiguration(myFixture.configureByText("App.spec.js", "")))
    }

    fun `test creates all scope configuration from wizkit config file`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val wizkitConfig = myFixture.configureByFile("spec/support/wizkit.json")
        val config = setupRunConfiguration(wizkitConfig)!!

        val runSettings = config.wizkitRunSettings
        assertEquals(WizKitScope.ALL, runSettings.scope)
        assertEquals("/src/spec/support/wizkit.json", runSettings.wizkitConfigFile)
    }

    fun `test does not create all scope configuration from other json files`() {
        myFixture.copyDirectoryToProject("test-project", "")
        assertNull(setupRunConfiguration(myFixture.configureByFile("package.json")))
    }

    fun `test creates file scope configuration from wizkit spec file`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val specFile = myFixture.configureByFile("spec/App.spec.js")
        val config = setupRunConfiguration(specFile)!!

        val runSettings = config.wizkitRunSettings
        assertEquals(WizKitScope.SPEC_FILE, runSettings.scope)
        assertEquals("/src/spec/App.spec.js", runSettings.specFile)
    }

    fun `test does not create file scope configuration from other js files`() {
        myFixture.copyDirectoryToProject("test-project", "")
        assertNull(setupRunConfiguration(myFixture.configureByFile("App.js")))
    }

    fun `test creates suite scope configuration from wizkit suite element`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val specFile = myFixture.configureByFile("spec/CaretOnSuite.spec.js")
        val config = setupRunConfigurationAtCaret(specFile)!!

        val runSettings = config.wizkitRunSettings
        assertEquals(WizKitScope.SUITE, runSettings.scope)
        assertEquals("/src/spec/CaretOnSuite.spec.js", runSettings.specFile)
        assertEquals(listOf("app", "suite"), runSettings.testNames)
    }

    fun `test creates test scope configuration from wizkit test element`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val specFile = myFixture.configureByFile("spec/CaretOnTest.spec.js")
        val config = setupRunConfigurationAtCaret(specFile)!!

        val runSettings = config.wizkitRunSettings
        assertEquals(WizKitScope.TEST, runSettings.scope)
        assertEquals("/src/spec/CaretOnTest.spec.js", runSettings.specFile)
        assertEquals(listOf("app", "suite", "test"), runSettings.testNames)
    }


    fun `test existing configuration not compatible when context cannot setup configuration`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val jsFileContext = ConfigurationContext(myFixture.configureByFile("App.js"))

        assertConfigNotFromContext(createConfiguration(), jsFileContext)
    }

    fun `test existing configuration not compatible when context is different scope`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val existingSpecFileConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.SPEC_FILE
        ))

        val allTestsContext = ConfigurationContext(myFixture.configureByFile("spec/support/wizkit.json"))

        assertConfigNotFromContext(existingSpecFileConfig, allTestsContext)
    }

    fun `test existing configuration not compatible when different spec files`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val existingSpecFileConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.SPEC_FILE,
                specFile = "different.spec.js"
        ))
        val existingSuiteConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.SUITE,
                specFile = "different.spec.js"
        ))
        val existingTestConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.TEST,
                specFile = "different.spec.js"
        ))

        val specFileContext = ConfigurationContext(myFixture.configureByFile("spec/App.spec.js"))
        val suiteContext = ConfigurationContext(myFixture.configureByFile("spec/CaretOnSuite.spec.js").elementAtCaret())
        val testContext = ConfigurationContext(myFixture.configureByFile("spec/CaretOnTest.spec.js").elementAtCaret())

        assertConfigNotFromContext(existingSpecFileConfig, specFileContext)
        assertConfigNotFromContext(existingSuiteConfig, suiteContext)
        assertConfigNotFromContext(existingTestConfig, testContext)
    }

    fun `test existing configuration not compatible when different test names`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val existingSuiteConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.SUITE,
                specFile = "/src/spec/CaretOnSuite.spec.js",
                testNames = listOf("different suite")
        ))
        val existingTestConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.TEST,
                specFile = "/src/spec/CaretOnTest.spec.js",
                testNames = listOf("different test")
        ))

        val suiteContext = ConfigurationContext(myFixture.configureByFile("spec/CaretOnSuite.spec.js").elementAtCaret())
        val testContext = ConfigurationContext(myFixture.configureByFile("spec/CaretOnTest.spec.js").elementAtCaret())

        assertConfigNotFromContext(existingSuiteConfig, suiteContext)
        assertConfigNotFromContext(existingTestConfig, testContext)
    }

    fun `test existing configuration compatible when both all scope`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val allTestsConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.ALL,
                wizkitConfigFile = "/src/spec/support/wizkit.json"
        ))

        val configContext = ConfigurationContext(myFixture.configureByFile("spec/support/wizkit.json"))

        assertConfigFromContext(allTestsConfig, configContext)
    }

    fun `test existing configuration compatible when context is same test`() {
        myFixture.copyDirectoryToProject("test-project", "")

        val suiteConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.SUITE,
                specFile = "/src/spec/CaretOnSuite.spec.js",
                testNames = listOf("app", "suite")
        ))
        val suiteContext = ConfigurationContext(myFixture.configureByFile("spec/CaretOnSuite.spec.js").elementAtCaret())

        val testConfig = createConfiguration(WizKitRunSettings(
                scope = WizKitScope.TEST,
                specFile = "/src/spec/CaretOnTest.spec.js",
                testNames = listOf("app", "suite", "test")
        ))
        val testContext = ConfigurationContext(myFixture.configureByFile("spec/CaretOnTest.spec.js").elementAtCaret())

        assertConfigFromContext(suiteConfig, suiteContext)
        assertConfigFromContext(testConfig, testContext)
    }

    private fun setupRunConfigurationAtCaret(specFile: PsiFile): WizKitRunConfiguration? {
        val elementAtCaret = specFile.elementAtCaret()
        return setupRunConfiguration(elementAtCaret)
    }

    private fun setupRunConfiguration(element: PsiElement): WizKitRunConfiguration? {
        val configFromContext = configProducer.createConfigurationFromContext(ConfigurationContext(element))
        val config = configFromContext?.configuration ?: return null
        return config as WizKitRunConfiguration
    }

    private fun PsiFile.elementAtCaret(): PsiElement {
        return this.findElementAt(myFixture.caretOffset)!!
    }

    private fun assertConfigFromContext(config: WizKitRunConfiguration, context: ConfigurationContext) {
        assertTrue(configProducer.isConfigurationFromContext(config, context))
    }

    private fun assertConfigNotFromContext(config: WizKitRunConfiguration, context: ConfigurationContext) {
        assertFalse(configProducer.isConfigurationFromContext(config, context))
    }

    private fun createConfiguration(runSettings: WizKitRunSettings = WizKitRunSettings()): WizKitRunConfiguration {
        val config = WizKitRunConfiguration(project, configFactory, "")
        config.wizkitRunSettings = runSettings.copy()
        return config
    }
}