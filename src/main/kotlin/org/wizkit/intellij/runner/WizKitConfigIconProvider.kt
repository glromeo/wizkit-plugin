package org.wizkit.intellij.runner

import com.intellij.ide.IconProvider
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.util.ObjectUtils
import icons.JSWizKitIcons
import org.wizkit.intellij.runner.util.WizKitUtil
import javax.swing.Icon

class WizKitConfigIconProvider : IconProvider(), DumbAware {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        val jsonFile = ObjectUtils.tryCast(element, JsonFile::class.java) ?: return null

        return when {
            WizKitUtil.isWizKitConfigFile(jsonFile.virtualFile) -> JSWizKitIcons.WizKit
            else -> null
        }
    }
}