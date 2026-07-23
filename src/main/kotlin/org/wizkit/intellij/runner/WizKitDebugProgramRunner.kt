package org.wizkit.intellij.runner

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultDebugExecutor

class WizKitDebugProgramRunner : WizKitProgramRunner() {
    override fun getRunnerId() = "WizKitJavascriptTestRunnerDebug"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return DefaultDebugExecutor.EXECUTOR_ID == executorId && profile is WizKitRunConfiguration
    }
}
