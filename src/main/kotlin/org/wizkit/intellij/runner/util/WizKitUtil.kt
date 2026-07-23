package org.wizkit.intellij.runner.util

import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.vfs.VirtualFile

object WizKitUtil {
    // @wizkit/testing registration functions: suite/test and their aliases
    val runnableFnNames = arrayOf("suite", "test", "describe", "context", "it")

    // suite.only(...), test.skip(...), etc.
    val runnableFnModifiers = arrayOf("only", "skip")

    private val specFileRegex = Regex(""".*\.spec\.(js|jsx|ts|tsx|cjs|mjs)$""")

    fun isWizKitConfigFile(file: VirtualFile?): Boolean {
        return file != null &&
                file.isValid &&
                !file.isDirectory &&
                (file.name == "wizkit.config.js" || file.name == "wizkit.config.mjs" || file.name == "wizkit.json") &&
                !JSLibraryUtil.isProbableLibraryFile(file)
    }

    /**
     * Checks if a JavaScript file is a wizkit test file by matching the filename
     * against the *.spec.{js,jsx,ts,tsx,cjs,mjs} convention.
     */
    fun isWizKitTestFile(jsFile: JSFile): Boolean {
        return jsFile.virtualFile?.name?.let { specFileRegex.matches(it) } ?: false
    }

    /**
     * This function is here to serve as an aid in troubleshooting during development. It can be used to
     * display information of interest at various points of execution.
     */
    fun notify(message: String) {
        val notification = Notification("WizKit Plugin", "WizKit", message, NotificationType.INFORMATION)
        Notifications.Bus.notify(notification)
    }
}