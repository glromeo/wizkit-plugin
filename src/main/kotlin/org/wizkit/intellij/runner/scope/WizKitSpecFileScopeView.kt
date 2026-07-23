package org.wizkit.intellij.runner.scope

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import com.intellij.webcore.ui.PathShortener
import org.wizkit.intellij.runner.WizKitRunSettings
import javax.swing.JComponent
import javax.swing.JPanel

class WizKitSpecFileScopeView(project: Project) : WizKitScopeView {
    private val specFileField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val panel: JPanel

    init {
        PathShortener.enablePathShortening(specFileField.textField, null)
        SwingHelper.installFileCompletionAndBrowseDialog(
                project,
                specFileField,
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                    .withTitle("Select Spec File")
        )
        panel = FormBuilder().setAlignLabelOnRight(false)
                .addLabeledComponent("Spec file:", specFileField)
                .panel
    }

    override fun getComponent(): JComponent {
        return panel
    }

    override fun resetFrom(settings: WizKitRunSettings) {
        specFileField.text = settings.specFile
    }

    override fun applyTo(settings: WizKitRunSettings) {
        settings.specFile = PathShortener.getAbsolutePath(specFileField.textField)
    }
}