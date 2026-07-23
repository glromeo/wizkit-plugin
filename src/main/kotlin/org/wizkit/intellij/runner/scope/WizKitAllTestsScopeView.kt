package org.wizkit.intellij.runner.scope

import org.wizkit.intellij.runner.WizKitRunSettings
import javax.swing.JComponent
import javax.swing.JPanel

class WizKitAllTestsScopeView : WizKitScopeView {
    override fun getComponent(): JComponent {
        return JPanel()
    }

    override fun resetFrom(settings: WizKitRunSettings) {}

    override fun applyTo(settings: WizKitRunSettings) {}
}