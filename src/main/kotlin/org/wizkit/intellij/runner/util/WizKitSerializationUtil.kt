package org.wizkit.intellij.runner.util

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.io.FileUtil
import org.wizkit.intellij.runner.WizKitRunSettings
import org.wizkit.intellij.runner.scope.WizKitScope
import org.jdom.Element

object WizKitSerializationUtil {
    private const val NODE_INTERPRETER = "node-interpreter"
    private const val NODE_OPTIONS = "node-options"
    private const val WORKING_DIR = "working-dir"
    private const val JASMINE_OPTIONS = "wizkit-options"
    private const val JASMINE_EXECUTABLE = "wizkit-executable"
    private const val JASMINE_CONFIG = "wizkit-config"
    private const val SCOPE = "scope"
    private const val SPEC_FILE = "spec-file"
    private const val TEST_NAMES = "test-names"
    private const val TEST_NAME = "test-name"

    fun writeXml(element: Element, runSettings: WizKitRunSettings) {
        element.write(NODE_INTERPRETER, runSettings.nodeJs.referenceName)
        element.write(NODE_OPTIONS, runSettings.nodeOptions)
        element.writePath(WORKING_DIR, runSettings.workingDir)
        element.write(JASMINE_OPTIONS, runSettings.extraWizKitOptions)
        element.writePath(JASMINE_EXECUTABLE, runSettings.wizkitExecutable)
        element.writePath(JASMINE_CONFIG, runSettings.wizkitConfigFile)
        element.write(SCOPE, runSettings.scope.name)
        element.writePath(SPEC_FILE, runSettings.specFile)
        element.writeTestNames(runSettings.testNames)
        runSettings.envData.writeExternal(element)
    }

    private fun Element.write(tagName: String, value: String) {
        if (value.isNotBlank()) {
            JDOMExternalizerUtil.writeCustomField(this, tagName, value)
        }
    }

    private fun Element.writePath(tagName: String, value: String) {
        this.write(tagName, FileUtil.toSystemIndependentName(value))
    }

    private fun Element.writeTestNames(testNames: List<String>) {
        if (testNames.isNotEmpty()) {
            val testNamesElement = Element(TEST_NAMES)
            JDOMExternalizerUtil.addChildrenWithValueAttribute(testNamesElement, TEST_NAME, testNames)
            this.addContent(testNamesElement)
        }
    }

    fun readXml(element: Element): WizKitRunSettings {
        return WizKitRunSettings(
                nodeJs = NodeJsInterpreterRef.create(element.read(NODE_INTERPRETER)),
                nodeOptions = element.read(NODE_OPTIONS),
                workingDir = element.read(WORKING_DIR),
                extraWizKitOptions = element.read(JASMINE_OPTIONS),
                wizkitExecutable = element.read(JASMINE_EXECUTABLE),
                wizkitConfigFile = element.read(JASMINE_CONFIG),
                envData = EnvironmentVariablesData.readExternal(element),
                scope = WizKitScope.valueOf(element.read(SCOPE)),
                specFile = element.read(SPEC_FILE),
                testNames = element.readTestNames()
        )
    }

    private fun Element.read(tagName: String): String {
        return JDOMExternalizerUtil.readCustomField(this, tagName) ?: ""
    }

    private fun Element.readTestNames(): List<String> {
        val testNamesElement = this.getChild(TEST_NAMES) ?: return emptyList()
        return JDOMExternalizerUtil.getChildrenValueAttributes(testNamesElement, TEST_NAME)
    }
}
