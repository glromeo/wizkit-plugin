package org.wizkit.intellij.runner.util

import com.intellij.lang.javascript.psi.JSFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class WizKitUtilTest : BasePlatformTestCase() {

    fun `test spec files are recognized by naming convention in every supported extension`() {
        assertIsTestFile("App.spec.js")
        assertIsTestFile("App.spec.jsx")
        assertIsTestFile("App.spec.ts")
        assertIsTestFile("App.spec.tsx")
        assertIsTestFile("App.spec.cjs")
        assertIsTestFile("App.spec.mjs")
    }

    fun `test files without the spec infix are not spec files`() {
        assertIsNotTestFile("App.js")
        assertIsNotTestFile("AppSpec.js")
        assertIsNotTestFile("spec.js")
    }

    fun `test spec file naming is case-sensitive`() {
        assertIsNotTestFile("App.Spec.js")
    }

    fun `test wizkit config files are recognized by name`() {
        assertIsConfigFile("wizkit.json")
        assertIsConfigFile("wizkit.config.js")
        assertIsConfigFile("wizkit.config.mjs")
    }

    fun `test other files are not config files`() {
        assertIsNotConfigFile("package.json")
        assertIsNotConfigFile("wizkit.config.json")
        assertFalse(WizKitUtil.isWizKitConfigFile(null))
    }

    fun `test config files inside libraries are ignored`() {
        val libraryConfig = myFixture.addFileToProject("node_modules/some-lib/wizkit.json", "{}")
        assertFalse(WizKitUtil.isWizKitConfigFile(libraryConfig.virtualFile))
    }

    private fun assertIsTestFile(fileName: String) {
        assertTrue("expected $fileName to be a wizkit spec file", isTestFile(fileName))
    }

    private fun assertIsNotTestFile(fileName: String) {
        assertFalse("expected $fileName not to be a wizkit spec file", isTestFile(fileName))
    }

    private fun assertIsConfigFile(fileName: String) {
        assertTrue("expected $fileName to be a wizkit config file", isConfigFile(fileName))
    }

    private fun assertIsNotConfigFile(fileName: String) {
        assertFalse("expected $fileName not to be a wizkit config file", isConfigFile(fileName))
    }

    private fun isTestFile(fileName: String): Boolean {
        val file = myFixture.configureByText(fileName, "") as JSFile
        return WizKitUtil.isWizKitTestFile(file)
    }

    private fun isConfigFile(fileName: String): Boolean {
        val file = myFixture.configureByText(fileName, "")
        return WizKitUtil.isWizKitConfigFile(file.virtualFile)
    }
}
