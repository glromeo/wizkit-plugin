package org.wizkit.intellij.runner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import icons.JSWizKitIcons

class WizKitConfigurationType : ConfigurationTypeBase("JavascriptTestRunnerWizKit", "WizKit", "WizKit", JSWizKitIcons.WizKit), DumbAware {
    init {
        addFactory(object : ConfigurationFactory(this) {
            override fun getId() = type.id
            override fun createTemplateConfiguration(project: Project) = WizKitRunConfiguration(project, this, "WizKit")
            override fun getSingletonPolicy() = RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY
        })
    }

    companion object {
        fun getInstance(): WizKitConfigurationType {
            return Holder.INSTANCE
        }
    }


    private object Holder {
        val INSTANCE = ConfigurationTypeUtil.findConfigurationType(WizKitConfigurationType::class.java)
    }
}
