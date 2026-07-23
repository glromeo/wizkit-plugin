# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

IntelliJ Platform plugin (plugin id `org.wizkit.wizkit`) that adds a "WizKit" run configuration type: run WizKit JavaScript tests from gutter icons / right-click context and see structured results in IntelliJ's test console. Written in Kotlin against the IntelliJ Platform Gradle Plugin 2.x.

## Commands

- `./gradlew build` — compile and run all tests
- `./gradlew test --tests "org.wizkit.intellij.runner.WizKitRunConfigurationProducerTest"` — run a single test class (add `.<methodName>` filtering with `--tests` patterns)
- `./gradlew buildPlugin` — produce the distributable zip at `build/distributions/IdeaWizKit-{version}.zip`
- `./gradlew runIde` — launch a sandboxed IDE with the plugin installed (sandbox lives in `build/idea-sandbox/`)

Build requirements:
- Kotlin toolchain targets **JDK 21** (`build.gradle.kts`); the README's "JDK 17" note is stale.
- The IntelliJ Platform dependency is resolved from a **local IDE install**: `local("C:/Program Files/JetBrains/IntelliJ IDEA")` in `build.gradle.kts`. On a machine without IDEA at that path, the build fails until that line is pointed at a local install or replaced with a repository-based version.

## Configuration layout

- `gradle.properties` — plugin name, group, version, description, `pluginSinceBuild`. Edit here, not in the build script.
- `release_notes/` — one markdown file per release; `changeNotes` in the plugin descriptor is taken from the **lexicographically last** file in this directory.
- `src/main/resources/META-INF/plugin.xml` — extension point registrations (configuration type, program runners, run configuration producer, line marker contributor, icon provider). New platform integrations must be registered here.
- Plugin depends on the bundled `JavaScript`, `JavaScriptDebugger`, and JSON plugins (declared both in `build.gradle.kts` and `plugin.xml`).

## Architecture

See `walkthrough.md` for a full layer-by-layer walkthrough. The short version — everything lives under `src/main/kotlin/org/wizkit/intellij/runner/`:

- **Model**: `WizKitRunSettings` (all per-run settings) + `WizKitScope` enum (ALL / SPEC_FILE / SUITE / TEST); each scope creates its own editor sub-panel in `scope/`.
- **Persistence**: `util/WizKitSerializationUtil` serializes settings to/from JDOM XML (stored in the user's `.idea/`).
- **Execution**: `WizKitRunProfileState.startProcess()` builds the `node ... wizkit.js --reporter=<temp copy of intellij_reporter.js> [specFile] [--filter=...]` command line. `src/main/resources/intellij_reporter.js` is a bundled reporter speaking IntelliJ's Service Messages protocol, extracted to a temp file each run. Debug runs add `--inspect-brk`.
- **Context integration**: `WizKitRunConfigurationProducer` (right-click → config from PSI element, gated on a `wizkit` dependency in package.json) and `WizKitRunLineMarkerContributor` (gutter run arrows on `suite`/`test`/`describe`/`context`/`it` calls, including `.only`/`.skip` modifiers) both parse JS via the bundled JavaScript plugin's PSI.

## Tests

Platform tests extend `BasePlatformTestCase` (JUnit 4 style, ``fun `test ...`()`` naming). The fixture project at `src/test/resources/testData/test-project/` is a checked-in fake npm project (including a stub `wizkit` package inside `node_modules/`) loaded with `myFixture.copyDirectoryToProject("test-project", "")`. Tests that need the wizkit dependency present must copy this project first — configuring a lone file yields "no wizkit dependency" behavior.
