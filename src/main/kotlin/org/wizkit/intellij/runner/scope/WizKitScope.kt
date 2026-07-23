package org.wizkit.intellij.runner.scope

import com.intellij.openapi.project.Project

enum class WizKitScope constructor(val label: String) {
    ALL("&All tests") {
        override fun createView(project: Project): WizKitScopeView {
            return WizKitAllTestsScopeView()
        }
    },
    SPEC_FILE("Spec &file") {
        override fun createView(project: Project): WizKitScopeView {
            return WizKitSpecFileScopeView(project)
        }
    },
    SUITE("S&uite") {
        override fun createView(project: Project): WizKitScopeView {
            return WizKitSuiteScopeView(project)
        }
    },
    TEST("&Test") {
        override fun createView(project: Project): WizKitScopeView {
            return WizKitTestScopeView(project)
        }
    };

    abstract fun createView(project: Project): WizKitScopeView
}
