package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object JSWizKitIcons {
    val WizKit = load("/icons/wizkit.png") // 16x16

    private fun load(path: String): Icon {
        return IconLoader.getIcon(path, JSWizKitIcons::class.java)
    }
}
