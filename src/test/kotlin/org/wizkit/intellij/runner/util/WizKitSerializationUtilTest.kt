package org.wizkit.intellij.runner.util

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.LightPlatformTestCase
import org.wizkit.intellij.runner.WizKitRunSettings
import org.wizkit.intellij.runner.scope.WizKitScope
import org.jdom.Element
import org.jdom.output.XMLOutputter

class WizKitSerializationUtilTest : LightPlatformTestCase() {

    private lateinit var actualElement: Element
    private lateinit var emptyRunSettings: WizKitRunSettings

    public override fun setUp() {
        super.setUp()

        actualElement = Element("configuration")
        emptyRunSettings = WizKitRunSettings(
                nodeJs = NodeJsInterpreterRef.create(""),
                wizkitExecutable = ""
        )
    }

    fun `test saves all properties from run settings`() {
        val runSettings = emptyRunSettings.copy(
                nodeJs = NodeJsInterpreterRef.create("node"),
                nodeOptions = "--no-warnings",
                workingDir = "\$PROJECT_DIR\$",
                envData = EnvironmentVariablesData.create(mapOf("ENV_NAME" to "ENV_VALUE"), true),
                extraWizKitOptions = "--no-color",
                wizkitExecutable = "wizkit.js",
                wizkitConfigFile = "\$PROJECT_DIR\$/wizkit.json",
                scope = WizKitScope.TEST,
                specFile = "\$PROJECT_DIR\$/spec/App.spec.json",
                testNames = listOf("suite name", "test name")
        )

        WizKitSerializationUtil.writeXml(actualElement, runSettings)

        val expectedElement = JDOMUtil.load("""
            <configuration>
                <node-interpreter value='node' />
                <node-options value='--no-warnings' />
                <working-dir value='${'$'}PROJECT_DIR${'$'}' />
                <wizkit-options value='--no-color' />
                <wizkit-executable value='wizkit.js' />
                <wizkit-config value='${'$'}PROJECT_DIR${'$'}/wizkit.json' />
                <scope value='TEST' />
                <spec-file value='${'$'}PROJECT_DIR${'$'}/spec/App.spec.json' />
                <test-names>
                    <test-name value='suite name' />
                    <test-name value='test name' />
                </test-names>
                <envs>
                    <env name='ENV_NAME' value='ENV_VALUE' />
                </envs>
            </configuration>
        """)

        assertEquals(expectedElement, actualElement)
    }

    fun `test does not save empty properties from run settings`() {
        WizKitSerializationUtil.writeXml(actualElement, emptyRunSettings)

        val expectedElement = JDOMUtil.load("""
            <configuration>
                <scope value='ALL' />
                <envs />
            </configuration>
        """)

        assertEquals(expectedElement, actualElement)
    }

    fun `test saves system independent paths`() {
        val runSettings = emptyRunSettings.copy(
                workingDir = "working\\directory\\path",
                wizkitExecutable = "wizkit\\executable\\path",
                wizkitConfigFile = "wizkit\\config\\path",
                specFile = "test\\file\\path",
                scope = WizKitScope.SPEC_FILE
        )

        WizKitSerializationUtil.writeXml(actualElement, runSettings)

        val expectedElement = JDOMUtil.load("""
            <configuration>
                <working-dir value='working/directory/path' />
                <wizkit-executable value='wizkit/executable/path' />
                <wizkit-config value='wizkit/config/path' />
                <scope value='SPEC_FILE' />
                <spec-file value='test/file/path' />
                <envs />
            </configuration>
        """)

        assertEquals(expectedElement, actualElement)
    }

    private fun assertEquals(expected: Element, actual: Element) {
        val outputter = XMLOutputter()
        assertEquals(outputter.outputString(expected), outputter.outputString(actual))
    }

    fun `test reads run settings`() {
        val configElement = JDOMUtil.load("""
            <configuration>
                <node-interpreter value='node' />
                <node-options value='--no-warnings' />
                <working-dir value='${'$'}PROJECT_DIR${'$'}' />
                <wizkit-options value='--no-color' />
                <wizkit-executable value='wizkit.js' />
                <wizkit-config value='${'$'}PROJECT_DIR${'$'}/wizkit.json' />
                <scope value='TEST' />
                <spec-file value='${'$'}PROJECT_DIR${'$'}/spec/App.spec.json' />
                <test-names>
                    <test-name value='suite name' />
                    <test-name value='test name' />
                </test-names>
                <envs>
                    <env name='ENV_NAME' value='ENV_VALUE' />
                </envs>
            </configuration>
        """)

        val actualSettings = WizKitSerializationUtil.readXml(configElement)

        val expectedSettings = emptyRunSettings.copy(
                nodeJs = NodeJsInterpreterRef.create("node"),
                nodeOptions = "--no-warnings",
                workingDir = "\$PROJECT_DIR\$",
                envData = EnvironmentVariablesData.create(mapOf("ENV_NAME" to "ENV_VALUE"), true),
                extraWizKitOptions = "--no-color",
                wizkitExecutable = "wizkit.js",
                wizkitConfigFile = "\$PROJECT_DIR\$/wizkit.json",
                scope = WizKitScope.TEST,
                specFile = "\$PROJECT_DIR\$/spec/App.spec.json",
                testNames = listOf("suite name", "test name")
        )

        assertEquals(expectedSettings, actualSettings)
    }

    fun `test read handles missing properties`() {
        val configElement = JDOMUtil.load("""
            <configuration>
                <scope value='ALL' />
            </configuration>
        """)

        val actualSettings = WizKitSerializationUtil.readXml(configElement)

        assertEquals(emptyRunSettings, actualSettings)
    }
}