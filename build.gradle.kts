import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.17.0"
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

dependencies {
    intellijPlatform {
        local("C:/Program Files/JetBrains/IntelliJ IDEA")
        // JavaScript requires the CSS plugin's modules since IDEA 2026.1
        bundledPlugins("JavaScript", "JavaScriptDebugger", "com.intellij.modules.json", "com.intellij.css")
        // Since IDEA 2026.2 these platform modules live in bundled implementation-detail
        // plugins. Their descriptors have to be on the classpath or the plugin graph stays
        // unresolved in unit tests and JavaScript/JSON/CSS never load.
        bundledPlugin("intellij.testRunner.plugin")      // intellij.platform.{testRunner,smRunner}
        bundledPlugin("intellij.structureView.plugin")   // intellij.platform.structureView
        bundledPlugin("intellij.libraries.misc.plugin")  // intellij.libraries.lucene.common
        bundledPlugin("com.intellij.modules.jcef")       // intellij.libraries.jcef
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.18.0")
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        description = providers.gradleProperty("pluginDescription")
        changeNotes = providers.provider { latestReleaseNotesFile().readText() }

        ideaVersion {
            sinceBuild.set(providers.gradleProperty("pluginSinceBuild"))
        }
    }
}

fun latestReleaseNotesFile(): File {
    return fileTree("release_notes")
        .getFiles()
        .sorted()
        .last()
}
