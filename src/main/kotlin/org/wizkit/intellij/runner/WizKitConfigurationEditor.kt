package org.wizkit.intellij.runner

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.json.JsonFileType
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ui.*
import org.wizkit.intellij.runner.scope.WizKitScope
import org.wizkit.intellij.runner.scope.WizKitScopeView
import org.wizkit.intellij.runner.util.WizKitUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import javax.swing.*
import com.intellij.openapi.ui.TextBrowseFolderListener


class WizKitConfigurationEditor(private var project: Project) : SettingsEditor<WizKitRunConfiguration>() {
    private var nodeJsInterpreterField: NodeJsInterpreterField = NodeJsInterpreterField(project, false)
    private var nodeOptionsField: RawCommandLineEditor = RawCommandLineEditor()
    private var workingDirectoryField = createWorkingDirectoryField()
    private var envVars: EnvironmentVariablesTextFieldWithBrowseButton = EnvironmentVariablesTextFieldWithBrowseButton()
    private var wizkitPackageField: NodePackageField = NodePackageField(nodeJsInterpreterField, "wizkit")
    private var wizkitExecutableField = createWizKitExecutableField()
    private var wizkitOptionsField = createWizKitOptionsField()
    private var wizkitConfigFileField = createWizKitConfigFileField()
    private var scopeViewPanel = JPanel(BorderLayout())

    private var scopeButtons = mutableMapOf<WizKitScope, JRadioButton>()
    private var scopeViews = mutableMapOf<WizKitScope, WizKitScopeView>()

    private var longestLabelWidth = JLabel("Environment variables").preferredSize.width

    private var rootForm: JPanel

    init {
        rootForm = FormBuilder()
                .setAlignLabelOnRight(false)
                .addLabeledComponent("Node &interpreter", nodeJsInterpreterField)
                .addLabeledComponent("Node &options", nodeOptionsField)
                .addLabeledComponent("&Working directory", workingDirectoryField)
                .addLabeledComponent("&Environment variables", envVars)
                .addLabeledComponent("&WizKit package", wizkitPackageField)
                .addLabeledComponent("WizKit executable", wizkitExecutableField)
                .addLabeledComponent("WizKit &config file", wizkitConfigFileField)
                .addLabeledComponent("E&xtra WizKit options", wizkitOptionsField)
                .addSeparator()
                .addComponent(createScopeRadioButtonPanel())
                .addComponent(scopeViewPanel)
                .panel
    }

    private fun createWorkingDirectoryField(): TextFieldWithBrowseButton {
        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                project
            )
        )
        return field
    }

    private fun createWizKitOptionsField(): RawCommandLineEditor {
        val editor = RawCommandLineEditor()
        val field = editor.textField
        if (field is ExpandableTextField) {
            field.putClientProperty("monospaced", false)
        }

        if (field is ComponentWithEmptyText) {
            (field as ComponentWithEmptyText).emptyText.text = "CLI options, e.g. --fail-fast=true"
        }

        return editor
    }

    private fun createWizKitExecutableField(): TextFieldWithBrowseButton {
        val fullField = TextFieldWithBrowseButton()
        fullField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
                project
            )
        )
        return fullField
    }

    private fun createWizKitConfigFileField(): TextFieldWithHistoryWithBrowseButton {
        val fullField = TextFieldWithHistoryWithBrowseButton()
        val innerField = fullField.childComponent
        innerField.setHistorySize(-1)
        innerField.setMinimumAndPreferredWidth(0)

        SwingHelper.addHistoryOnExpansion(innerField) {
            innerField.history = emptyList<String>()
            listPossibleConfigFilesInProject().map { file ->
                FileUtil.toSystemDependentName(file.path)
            }.sorted()
        }

        SwingHelper.installFileCompletionAndBrowseDialog(
            project,
            fullField,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )

        return fullField
    }

    private fun listPossibleConfigFilesInProject(): List<VirtualFile> {
        val contentScope = ProjectScope.getContentScope(project)
        val scope = contentScope.intersectWith(GlobalSearchScope.notScope(ProjectScope.getLibrariesScope(project)))

        val files = FileTypeIndex.getFiles(JsonFileType.INSTANCE, scope) +
                FileTypeIndex.getFiles(JavaScriptFileType, scope)

        return files.filter { WizKitUtil.isWizKitConfigFile(it) }
    }

    private fun createScopeRadioButtonPanel(): JPanel {
        val testScopePanel = JPanel(FlowLayout(FlowLayout.CENTER, JBUI.scale(40), 0))
        val buttonGroup = ButtonGroup()

        WizKitScope.entries.forEach { scope ->
            val radioButton = JRadioButton(UIUtil.removeMnemonic(scope.label))

            val index = UIUtil.getDisplayMnemonicIndex(scope.label)
            if (index != -1) {
                radioButton.setMnemonic(scope.label[index + 1])
                radioButton.displayedMnemonicIndex = index
            }

            radioButton.addActionListener { setTestScope(scope) }

            scopeButtons[scope] = radioButton
            testScopePanel.add(radioButton)
            buttonGroup.add(radioButton)
        }

        return testScopePanel
    }

    private fun setTestScope(scope: WizKitScope) {
        val scopeView = getScopeView(scope)
        scopeButtons[scope]?.isSelected = true
        scopeViewPanel.removeAll()
        scopeViewPanel.add(scopeView.getComponent(), BorderLayout.CENTER)
        scopeViewPanel.revalidate()
        scopeViewPanel.repaint()
    }

    private fun getScopeView(scope: WizKitScope) = scopeViews.getOrPut(scope) {
        val scopeView = scope.createView(project)

        // align scope view fields with other fields in editor
        scopeView.getComponent().add(
                Box.createHorizontalStrut(longestLabelWidth),
                GridBagConstraints(
                        0, GridBagConstraints.RELATIVE,
                        1, 1,
                        0.0, 0.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.NONE,
                        JBUI.insetsRight(UIUtil.DEFAULT_HGAP),
                        0, 0
                )
        )

        scopeView
    }

    override fun createEditor(): JComponent = rootForm

    override fun applyEditorTo(config: WizKitRunConfiguration) {
        config.wizkitRunSettings = config.wizkitRunSettings.copy(
                nodeJs = nodeJsInterpreterField.interpreterRef,
                nodeOptions = nodeOptionsField.text,
                workingDir = workingDirectoryField.text,
                envData = envVars.data,
                wizkitExecutable = wizkitExecutableField.text,
                wizkitConfigFile = wizkitConfigFileField.text,
                extraWizKitOptions = wizkitOptionsField.text)

        scopeButtons.entries.find { it.value.isSelected }?.run {
            config.wizkitRunSettings.scope = key
            getScopeView(key).applyTo(config.wizkitRunSettings)
        }

        config.setWizKitPackage(wizkitPackageField.selected)
    }

    override fun resetEditorFrom(config: WizKitRunConfiguration) {
        val runSettings = config.wizkitRunSettings
        nodeJsInterpreterField.interpreterRef = runSettings.nodeJs
        nodeOptionsField.text = runSettings.nodeOptions
        workingDirectoryField.text = FileUtil.toSystemDependentName(runSettings.workingDir)
        envVars.data = runSettings.envData
        wizkitPackageField.selected = config.selectedWizKitPackage()
        wizkitExecutableField.text = runSettings.wizkitExecutable
        wizkitConfigFileField.text = runSettings.wizkitConfigFile
        wizkitOptionsField.text = runSettings.extraWizKitOptions
        setTestScope(runSettings.scope)
        getScopeView(runSettings.scope).resetFrom(runSettings)
    }
}
