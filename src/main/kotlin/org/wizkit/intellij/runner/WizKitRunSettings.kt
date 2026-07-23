package org.wizkit.intellij.runner

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import org.wizkit.intellij.runner.scope.WizKitScope

data class WizKitRunSettings(
    var nodeJs : NodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef(),
    var nodeOptions : String = "",
    var workingDir : String = "",
    var envData : EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,
    var extraWizKitOptions : String = "",
    var wizkitExecutable: String = "bin/wizkit.js",
    var wizkitConfigFile : String = "",
    var scope : WizKitScope = WizKitScope.ALL,
    var specFile : String = "",
    var testNames: List<String> = emptyList()
)