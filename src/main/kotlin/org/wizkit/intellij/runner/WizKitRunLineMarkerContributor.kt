package org.wizkit.intellij.runner

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.wizkit.intellij.runner.util.WizKitUtil

/**
 * Provides run line markers for WizKit tests
 * This adds the run/debug actions to the gutter icons
 */
class WizKitRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (!(element.containingFile is JSFile && isRunnableFnCall(element))) {
            return null
        }

        // Get the standard run actions with default run icon
        val actions = ExecutorAction.getActions(0)
        if (actions.isNotEmpty()) {
            return Info(AllIcons.RunConfigurations.TestState.Run, actions, { "Run WizKit Test" })
        }
        
        return null
    }

    /**
     * Matches the identifier of a `suite`/`test` (or alias) invocation, either called
     * directly — `test("...")` — or through an `.only`/`.skip` modifier — `test.only("...")`.
     */
    private fun isRunnableFnCall(element: PsiElement): Boolean {
        // Only process actual leaf elements (identifiers)
        if (element !is LeafPsiElement || element.text !in WizKitUtil.runnableFnNames) {
            return false
        }

        val reference = element.parent
        if (reference !is JSReferenceExpression) {
            return false
        }

        val parent = reference.parent
        return parent is JSCallExpression ||
                (parent is JSReferenceExpression &&
                        parent.referenceName in WizKitUtil.runnableFnModifiers &&
                        parent.parent is JSCallExpression)
    }
}
