● Here's a walkthrough of the plugin architecture, layer by layer:
                                                                                                                                                                                                           
---                                                                                                                                                                                                      
Data Model

WizKitRunSettings — a simple Kotlin data class holding all configuration for a single run: which Node.js interpreter to use, node options, working dir, env vars, path to the wizkit executable, an    
optional WizKit config file, the test scope (ALL/SPEC_FILE/SUITE/TEST), the spec file path, and a list of test names to filter on.

WizKitScope — an enum with four values. Each entry carries a display label (with mnemonic &) and a createView() factory that returns the appropriate scope-specific UI panel.

  ---
Configuration Type Registration

WizKitConfigurationType — registered with IntelliJ as the "WizKit" run configuration type. Extends ConfigurationTypeBase and adds one ConfigurationFactory that:
- creates WizKitRunConfiguration instances as the template
- enforces SINGLE_INSTANCE_ONLY policy

It uses a lazy singleton via Holder to look itself up from the platform registry.

  ---
Run Configuration

WizKitRunConfiguration — the core model object IntelliJ persists per run config. Key responsibilities:
- Holds the WizKitRunSettings instance
- Lazily resolves the wizkit npm package via NodePackage.findPreferredPackage
- checkConfiguration() validates scope-dependent requirements (spec file must exist, test names must not be empty)
- suggestedName() / getActionName() drive the auto-generated name shown in the UI
- writeExternal / readExternal delegate to WizKitSerializationUtil for XML persistence (saved in .idea/)

  ---
Serialization

WizKitSerializationUtil — reads/writes WizKitRunSettings to/from a JDOM Element (IntelliJ's XML persistence format). Uses extension functions on Element to keep it clean. Paths are normalized to
forward-slash form. Test names are stored as a nested <test-names> element with repeated <test-name> children.

  ---
Execution Pipeline

WizKitRunProfileState — the heart of execution. When a run is triggered, startProcess() builds a GeneralCommandLine:

node [--inspect-brk] [nodeOptions] <wizkit/bin/wizkit.js>
[extraWizKitOptions]
[--config=<wizkitConfigFile>]
--reporter=<temp/intellij_reporter.js>   ← extracted from resources at runtime
[specFile]
[--filter=<escaped regex>]

The intellij_reporter.js is a bundled resource that's extracted to a temp file each run — it speaks the Service Messages protocol that IntelliJ's test console understands.

createConsole() wires up the SMTestRunner (IntelliJ's structured test result UI) with WizKitConsoleProperties that sets sensible defaults: show failed tests, scroll to source, select first defect.

WizKitProgramRunner (abstract) — implements GenericProgramRunner, saves all documents first, executes the state, wraps the result in a RunContentBuilder, and registers the "Re-run Tests" action.

WizKitRunProgramRunner / WizKitDebugProgramRunner — thin subclasses that just provide their runner ID. The debug runner relies on the --inspect-brk flag added in startProcess() when it detects
DefaultDebugExecutor.EXECUTOR_ID.

  ---
Context-Aware Config Creation

WizKitRunConfigurationProducer — IntelliJ calls this when you right-click in the editor. It inspects the PSI element at the cursor:

- File item → if it's a JS file containing WizKit constructs → SPEC_FILE scope; if it's a wizkit*.json → ALL scope with that config
- Inside a describe/it call → uses WizKitFileStructureBuilder (bundled JavaScript plugin) to extract the suite/test name hierarchy → SUITE or TEST scope

Template settings are preserved (Node interpreter, env vars, etc.) and only the scope-specific fields are overridden.

isConfigurationFromCompatibleContext() prevents duplicate run configs by checking if an existing config already covers the same scope + file + names.

  ---
Gutter Icons

WizKitRunLineMarkerContributor — walks the PSI tree looking for leaf elements whose text is one of describe, fdescribe, it, fit, where the parent chain is JSReferenceExpression → JSCallExpression.
When matched, it adds a green run arrow in the gutter that triggers ExecutorAction.getActions(0).

  ---
UI Editor

WizKitConfigurationEditor — a Swing SettingsEditor built with IntelliJ's FormBuilder. Shows fields for all settings plus a radio button group for scope. When scope changes, it swaps out a
scope-specific sub-panel (lazy-created, cached in scopeViews). applyEditorTo / resetEditorFrom are the standard IntelliJ editor contract methods.

  ---
Flow Summary

Right-click / gutter icon
→ WizKitRunConfigurationProducer  (creates/matches config)
→ WizKitConfigurationEditor       (user edits in Run dialog)
→ WizKitRunConfiguration          (validated, persisted as XML)
→ WizKitRunProgramRunner / WizKitDebugProgramRunner
→ WizKitRunProfileState.startProcess()
→ node ... wizkit ... --reporter=intellij_reporter.js
→ SMTestRunner console (structured test results)