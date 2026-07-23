package org.wizkit.intellij.runner

import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.systemIndependentPath
import org.wizkit.intellij.runner.scope.WizKitScope
import java.io.File
import java.nio.file.Files

class WizKitRunConfigurationTest : BasePlatformTestCase() {

    private lateinit var subject: WizKitRunConfiguration

    public override fun setUp() {
        super.setUp()

        val configFactory = WizKitConfigurationType.getInstance().configurationFactories[0]
        subject = configFactory.createTemplateConfiguration(project) as WizKitRunConfiguration
    }

    fun `test resolves wizkit package`() {
        // WizKit needs to be listed as a dependency of the project, not just be available
        myFixture.addFileToProject("package.json", """{
            "dependencies": {
              "wizkit": "*"
            }
        }""")
        myFixture.addFileToProject("node_modules/wizkit/package.json", "")

        assertEquals("/src/node_modules/wizkit", subject.selectedWizKitPackage().systemIndependentPath)
    }

    fun `test configuration error when wizkit package not set`() {
        assertConfigurationErrorEquals("Unspecified wizkit package", subject)
    }

    fun `test configuration error when spec file not set`() {
        subject.wizkitRunSettings = WizKitRunSettings(scope = WizKitScope.SPEC_FILE)
        assertConfigurationErrorEquals("Unspecified spec file", subject)

        subject.wizkitRunSettings = WizKitRunSettings(scope = WizKitScope.SUITE)
        assertConfigurationErrorEquals("Unspecified spec file", subject)

        subject.wizkitRunSettings = WizKitRunSettings(scope = WizKitScope.TEST)
        assertConfigurationErrorEquals("Unspecified spec file", subject)
    }

    fun `test configuration error when spec file is not a file`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.SPEC_FILE,
            specFile = "doesnotexist"
        )
        assertConfigurationErrorEquals("No such spec file", subject)

        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.SPEC_FILE,
            specFile = Files.createTempDirectory("temp-dir").systemIndependentPath
        )
        assertConfigurationErrorEquals("No such spec file", subject)
    }

    fun `test configuration error when suite name not set`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.SUITE,
            specFile = File.createTempFile("App.spec", ".js").absolutePath
        )
        assertConfigurationErrorEquals("Unspecified suite name", subject)
    }

    fun `test configuration error when test name not set`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.TEST,
            specFile = File.createTempFile("App.spec", ".js").absolutePath
        )
        assertConfigurationErrorEquals("Unspecified test name", subject)
    }

    private fun assertConfigurationErrorEquals(expectedErrorMessage: String, configuration: WizKitRunConfiguration) {
        try {
            configuration.checkConfiguration()
            fail("should throw RuntimeConfigurationError")
        } catch (re: RuntimeConfigurationError) {
            assertEquals(expectedErrorMessage, re.localizedMessage)
        }
    }

    fun `test suggested and action name is 'All Tests' for all scope`() {
        subject.wizkitRunSettings = WizKitRunSettings(scope = WizKitScope.ALL)
        subject.setGeneratedName()

        assertEquals("All Tests", subject.suggestedName())
        assertEquals("All Tests", subject.actionName)
    }

    fun `test suggested and action name is file name for file scope`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.SPEC_FILE,
            specFile = "spec/App.spec.js"
        )
        subject.setGeneratedName()

        assertEquals("App.spec.js", subject.suggestedName())
        assertEquals("App.spec.js", subject.actionName)
    }

    fun `test suggested name is full test name for suite scope`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.SUITE,
            testNames = listOf("top suite", "nested suite")
        )

        assertEquals("top suite.nested suite", subject.suggestedName())
    }

    fun `test suggested name is full test name for test scope`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.TEST,
            testNames = listOf("suite name", "test name")
        )

        assertEquals("suite name.test name", subject.suggestedName())
    }

    fun `test action name is suite name for suite scope`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.SUITE,
            testNames = listOf("top suite", "nested suite")
        )
        subject.setGeneratedName()

        assertEquals("nested suite", subject.actionName)
    }

    fun `test action name is test name for test scope`() {
        subject.wizkitRunSettings = WizKitRunSettings(
            scope = WizKitScope.TEST,
            testNames = listOf("suite name", "test name")
        )
        subject.setGeneratedName()

        assertEquals("test name", subject.actionName)
    }

    fun `test action name is null for empty test names`() {
        subject.wizkitRunSettings = WizKitRunSettings(scope = WizKitScope.TEST)
        subject.setGeneratedName()

        assertNull(subject.actionName)
    }
}
