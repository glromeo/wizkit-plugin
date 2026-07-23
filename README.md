# WizKit IntelliJ Plugin  [![CircleCI](https://circleci.com/gh/wizkit/IdeaWizKit/tree/main.svg?style=svg)](https://circleci.com/gh/wizkit/IdeaWizKit/tree/main)

This adds a new run configuration to IntelliJ (and IntelliJ based IDEs) that runs WizKit and displays the results in the test console.

## Usage

### Installation
1. In your IDE preferences, go to the "Plugins" node  
    * You can install it directly from [jetbrains](https://plugins.jetbrains.com/plugin/10449-wizkit)
    * Or download from [release page](https://github.com/wizkit/IdeaWizKit/releases) and "Install plugin from disk"
    * If you really want to be on the cutting edge, you can download the build artifact from the latest build [on Circle CI](https://app.circleci.com/pipelines/github/wizkit/IdeaWizKit?branch=main)
2. Restart your IDE

### Configure
1. In the "Run" menu, click on "Edit Configurations", there should be a new "WizKit" default configuration
1. Click the "+" to add a new configuration and select "WizKit" as the template
1. Fill in your Node.js interpreter and select the correct WizKit package and set any other options you want
1. Run your tests!

## Development

This plugin uses the [IntelliJ Platform Gradle Plugin](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html) for resolving
IntelliJ dependencies and packaging.

The code for the plugin is written in [Kotlin](http://kotlinlang.org/)

Local builds expect to be run on JDK-21, but higher might also work. Managing multiple Java versions is possible
with something like [SDKMan](https://sdkman.io/)

The IntelliJ Platform is resolved from a local IDE install (see `local(...)` in `build.gradle.kts`); point that
path at your own installation, or replace it with a repository-based version.

### Building
Build and test the plugin with:  
`./gradlew build`

Build the plugin zip with:
`./gradlew buildPlugin`

The distribution zip will be available at `build/distributions/IdeaWizKit-{version}.zip`

### Testing
Run IntelliJ IDEA with this plugin installed:  
`./gradlew runIde`
