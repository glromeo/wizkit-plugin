package org.wizkit.intellij.runner

import com.intellij.json.JsonFileType
import com.intellij.openapi.util.IconLoader
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import javax.swing.Icon

class WizKitConfigIconProviderTest : BasePlatformTestCase() {

    fun `test shows wizkit icon for wizkit config file`() {
        val wizkitConfigFile = myFixture.configureByText("wizkit.json", "")

        val expectedIcon: Icon = IconLoader.getIcon("/icons/wizkit.png", WizKitConfigIconProvider::class.java)
        val actualIcon = WizKitConfigIconProvider().getIcon(wizkitConfigFile, 0)

        assertSame("Expected wizkit icon to be used", expectedIcon, actualIcon)
    }

    fun `test does not show wizkit icon on other files`() {
        val jsonFile = myFixture.configureByText(JsonFileType.INSTANCE, "")
        val txtNamedWizKit = myFixture.configureByText("wizkit.txt", "")

        assertNull("No icon expected for generic .json", WizKitConfigIconProvider().getIcon(jsonFile, 0))
        assertNull("No icon expected for .txt", WizKitConfigIconProvider().getIcon(txtNamedWizKit, 0))
    }
}
