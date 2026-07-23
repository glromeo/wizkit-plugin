package org.wizkit.intellij.runner

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.nodejs.debug.NodeDebugRunConfiguration
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.project.Project
import com.intellij.util.PathUtil
import org.wizkit.intellij.runner.scope.WizKitScope
import org.wizkit.intellij.runner.util.WizKitSerializationUtil
import org.wizkit.intellij.runner.util.TestNameUtil
import org.jdom.Element
import java.nio.file.Paths
import kotlin.io.path.isRegularFile


class WizKitRunConfiguration(project: Project, factory: ConfigurationFactory, name: String)
    : LocatableConfigurationBase<Any>(project, factory, name), NodeDebugRunConfiguration {
    var wizkitRunSettings = WizKitRunSettings()
    private var _wizkitPackage: NodePackage? = null

    override fun getConfigurationEditor() = WizKitConfigurationEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment) = WizKitRunProfileState(project, this, executor, environment)

    fun selectedWizKitPackage(): NodePackage {
        if (_wizkitPackage == null) {
            val interpreter = NodeJsLocalInterpreter.tryCast(wizkitRunSettings.nodeJs.resolve(project))
            val pkg = NodePackage.findPreferredPackage(project, "wizkit", interpreter)
            _wizkitPackage = pkg
            return pkg
        }
        return _wizkitPackage!!
    }

    fun setWizKitPackage(nodePackage: NodePackage) {
        _wizkitPackage = nodePackage
    }

    override fun checkConfiguration() {
        val scope = wizkitRunSettings.scope

        if (scope.requiresSpecFile()) {
            when {
                wizkitRunSettings.specFile.isBlank() -> throw RuntimeConfigurationError("Unspecified spec file")
                !Paths.get(wizkitRunSettings.specFile).isRegularFile()
                    -> throw RuntimeConfigurationError("No such spec file")
            }
        }

        if (scope.requiresTestNames() && wizkitRunSettings.testNames.isEmpty()) {
            throw RuntimeConfigurationError("Unspecified ${scope.name.lowercase()} name")
        }

        selectedWizKitPackage().validateForRunConfiguration("wizkit")
    }

    private fun WizKitScope.requiresSpecFile(): Boolean {
        return this == WizKitScope.SPEC_FILE || this == WizKitScope.SUITE || this == WizKitScope.TEST
    }

    private fun WizKitScope.requiresTestNames(): Boolean {
        return this == WizKitScope.SUITE || this == WizKitScope.TEST
    }

    override fun suggestedName(): String? = when (wizkitRunSettings.scope) {
        WizKitScope.ALL -> if (wizkitRunSettings.specFile.isNotBlank())
            PathUtil.getFileName(wizkitRunSettings.specFile)
        else
            "All Tests"
        WizKitScope.SPEC_FILE -> PathUtil.getFileName(wizkitRunSettings.specFile)
        WizKitScope.SUITE, WizKitScope.TEST -> TestNameUtil.getPresentableName(wizkitRunSettings.testNames)
    }

    override fun getActionName(): String? = when (wizkitRunSettings.scope) {
        WizKitScope.SUITE, WizKitScope.TEST -> wizkitRunSettings.testNames.lastOrNull()
        else -> super.getActionName()
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        WizKitSerializationUtil.writeXml(element, wizkitRunSettings)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        wizkitRunSettings = WizKitSerializationUtil.readXml(element)
    }
}

