package org.wizkit.intellij.runner.scope

import org.wizkit.intellij.runner.WizKitRunSettings
import javax.swing.JComponent

interface WizKitScopeView {
    fun getComponent(): JComponent
    fun resetFrom(settings: WizKitRunSettings)
    fun applyTo(settings: WizKitRunSettings)
}
