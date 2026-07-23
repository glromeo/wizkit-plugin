package org.wizkit.intellij.runner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.wizkit.intellij.runner.scope.WizKitScope
import java.io.File

class WizKitRunProfileStateTest : BasePlatformTestCase() {

    private lateinit var runExecutor: DefaultRunExecutor
    private lateinit var configFactory: ConfigurationFactory

    override fun setUp() {
        super.setUp()

        File(project.basePath).mkdirs()

        runExecutor = DefaultRunExecutor()
        configFactory = WizKitConfigurationType.getInstance().configurationFactories[0]
    }

    fun `test spec file path in command`() {
        val testFilePath = "/path/to/App.spec.js"

        val testFileCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.SPEC_FILE, specFile = testFilePath)
        )
        val suiteCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.SUITE, specFile = testFilePath)
        )
        val testCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.TEST, specFile = testFilePath)
        )

        assertTrue(testFileCommand.endsWith(testFilePath))
        assertTrue(suiteCommand.endsWith(testFilePath))
        assertTrue(testCommand.endsWith(testFilePath))
    }

    fun `test test names added as filter`() {
        val testNames = listOf("App", "spec", "name with spaces")

        val suiteCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.SUITE, testNames = testNames)
        )
        val testCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.TEST, testNames = testNames)
        )

        assertTrue(suiteCommand.endsWith("\"--filter=App spec name with spaces\""))
        assertTrue(testCommand.endsWith("\"--filter=App spec name with spaces\""))
    }

    fun `test filter escapes special regex characters`() {
        val testNames = listOf("suite", "spec with * in it")

        val testCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.TEST, testNames = testNames)
        )

        assertTrue(testCommand.endsWith("\"--filter=suite spec with \\* in it\""))
    }

    fun `test filter not added when empty test names`() {
        val noFilterCommand = commandLineFromSettings(
            WizKitRunSettings(scope = WizKitScope.TEST, testNames = emptyList())
        )

        assertTrue("--filter" !in noFilterCommand)
    }

    private fun commandLineFromSettings(runSettings: WizKitRunSettings = WizKitRunSettings()): String {
        val config = WizKitRunConfiguration(project, configFactory, "")
        config.wizkitRunSettings = runSettings.copy()

        val execEnvironment = ExecutionEnvironmentBuilder.create(runExecutor, config).build()
        val runState = WizKitRunProfileState(project, config, runExecutor, execEnvironment)
        return (runState.startProcess() as KillableColoredProcessHandler).commandLine
    }
}
