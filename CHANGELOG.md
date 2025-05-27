# Doma Tools for IntelliJ

## [Unreleased]

## [0.8.0] - 2025-05-27

### New Features

- Reference custom function definition class from config file ([#185])
- Support custom functions ([#175])
- Dynamic retrieval of source directory names ([#181])

### Bug Fixes

- Enhance SQL test data validation to support case-insensitive boolean values ([#200])
- Update custom function invocation message to include class name ([#199])
- Replace Trailing Characters During Code Completion ([#196])
- Change the scope of the SQL formatting settings ([#195])
- Fix Test-Data Validation ([#193])
- Fix: Assert: must be called on EDT ([#180])
- Fix Custom Function Code Completion ([#184])
- Fix StringIndexOutOfBoundsException during file path retrieval ([#173])
- Fix missing inspections for injection files ([#172])

### Dependency Updates

- Update dependency com.pinterest.ktlint:ktlint-cli to v1.6.0 ([#177])
- Update plugin org.gradle.toolchains.foojay-resolver-convention to v1 ([#178])

[#185]:https://github.com/domaframework/doma-tools-for-intellij/pull/185
[#175]:https://github.com/domaframework/doma-tools-for-intellij/pull/175
[#181]:https://github.com/domaframework/doma-tools-for-intellij/pull/181
[#200]:https://github.com/domaframework/doma-tools-for-intellij/pull/200
[#199]:https://github.com/domaframework/doma-tools-for-intellij/pull/199
[#196]:https://github.com/domaframework/doma-tools-for-intellij/pull/196
[#195]:https://github.com/domaframework/doma-tools-for-intellij/pull/195
[#193]:https://github.com/domaframework/doma-tools-for-intellij/pull/193
[#180]:https://github.com/domaframework/doma-tools-for-intellij/pull/180
[#184]:https://github.com/domaframework/doma-tools-for-intellij/pull/184
[#173]:https://github.com/domaframework/doma-tools-for-intellij/pull/173
[#172]:https://github.com/domaframework/doma-tools-for-intellij/pull/172
[#177]:https://github.com/domaframework/doma-tools-for-intellij/pull/177
[#178]:https://github.com/domaframework/doma-tools-for-intellij/pull/178


## [0.7.0] - 2025-05-15

### New Features

- Error message details ([#162])
- Completion for directive items ([#160])
- for directive specific word element handling ([#150])
- Code completion and code inspection for static property call package class names ([#148])
- Add SQL test data validation inspections ([#136])

### Bug Fixes

- Fix to not display Dao parameter information in the document for elements defined with the for directive. ([#167])
- Do not suggest element names you define in the for directive element ([#166])
- Fix: issue where grouped conditions in block comments were recognized as bind variables ([#159])
- Fixed check for List type test data ([#157])
- Fix/formatter line comment indent ([#156])
- Fixed specific element types to be treated as primitive types ([#152])
- Fix: optional dao param type conversion ([#151])
- Move PluginUtil.kt to the util package in release configuration files ([#146])
- List type test data check and exclude else directive ([#145])
- Fix/document first for item ([#142])

### Dependency Updates

- Update dependency org.jetbrains.intellij.platform to v2.6.0 ([#164])
- Update dependency org.jetbrains.kotlin.jvm to v2.1.21 ([#161])
- Update dependency com.google.googlejavaformat:google-java-format to v1.27.0 ([#143])

## [0.6.0] - 2025-04-30

### New Features

- Add For Item Element Documentation ([#126])
- resolution for elements defined in the %for directive ([#117])
- File jump in jar ([#110])
- Add A reference to the SQL symbol type definer ([#107])

### Bug Fixes

- Added a condition to output logs only when references are resolved ([#128])
- Fix: StringIndexOutOfBoundsException: Range In FileTypeCheck ([#122])
- Support reference resolution for bind variables that reference classpath resources within JAR files. ([#115])
- Fix:sql formatter second option indent ([#102])
- Update changelog command and fix tag sorting in build.gradle.kts ([#104])
- Update changelog and improve version handling ([#103])

### Maintenance

- Overwriting a development version with a release version ([#109])

### Dependency Updates

- Update dependency gradle to v8.14 ([#121])
- Update dependency com.fasterxml.jackson.module:jackson-module-kotlin to v2.19.0 ([#120])

## [0.5.0] - 2025-04-14

### New Features

- Function Parameter Block Code Completion ([#80])

### Bug Fixes

- Add option --no-configuration-cache ([#96])
- Execute the required processing within `doLast`. ([#95])
- Call project before the task execution phase. ([#94])
- Null check for virtualFile ([#92])
- SQL format call log ([#88])
- Prevents the post processor from running if the usage flag is False ([#89])
- Strengthen checks when referencing nullable objects. ([#87])
- Fix/formatter indent ([#86])
- inspection in function parameters ([#82])
- Fix:Strings are concatenated when completing code for % directives ([#79])

### Maintenance

- Ci/publish version setting ([#99])
- Automating development version updates ([#93])
- Add beta to Build Version ([#91])

### Dependency Updates

- Update plugin org.gradle.toolchains.foojay-resolver-convention to v0.10.0 ([#90])
- Update plugin spotless to v7.0.3 ([#78])
- Update dependency org.jetbrains.intellij.platform to v2.5.0 ([#62])

## [0.4.0] - 2025-04-07

### New Features

- Implementing SQL formatting functionality(preview) ([#69])

### Maintenance

- Get the build version from the latest release draft ([#70])

### Dependency Updates

- Update dependency com.google.googlejavaformat:google-java-format to v1.26.0 ([#68])
- Update dependency org.jetbrains.kotlin.jvm to v2.1.20 ([#61])

## [0.3.2] - 2025-03-27

### Bug Fixes

- Add Char type to the condition as a literal ([#56])
- Fix:Prevent log file archives from being output to IDEA installation directory ([#57])
- Fix: Exclude java.stream.Collector type arguments from code inspection ([#58])
- Fix: Set a unique text attribute key ([#59])

### Maintenance

- Update version reference information 0.3.2 ([#64])
- Fixed Plugin Verifier version to 1.383 ([#65])
- Force unwrapping a nullable type into a null-safe object ([#48])
- Action cache control and refactoring ([#49])
- Regular expressions to match tag name format updates ([#45])
- Additional control over update log change actions ([#43])

### Dependency Updates

- Update dependency ch.qos.logback:logback-classic to v1.5.18 ([#60])

## [0.3.1] - 2025-03-11

### Bug Fixes

- Fix: Reference checks for repeated elements in the for directive do not work correctly ([#20])
- Fix: element type condition checking in DAO argument usage check. ([#21])

### Maintenance

- Fix:Release draft generation is delegated to release-drafter ([#35])
- Add: Update CHANGELOG.md Action ([#34])
- Fix: release-raft should only be run on the main branch ([#26])
- Add Develop Build Number ([#25])

### Dependency Updates

- Fix: IntelliJ Platform version upgrades ([#27])
- Update dependency org.jetbrains.intellij.platform to v2.3.0 ([#10])
- Change the library version upgrade settings ([#24])
- Update dependency gradle to v8.13 ([#9])
- Update dependency ubuntu to v24 ([#12])
- Update dependency org.seasar.doma:doma-core to v3.5.1 ([#11])
- Update dependency org.slf4j:slf4j-api to v2.0.17 ([#8])
- Update dependency ch.qos.logback:logback-classic to v1.5.17 ([#7])

## [0.3.0] - 2025-03-07

### Dao Support Features

- **Actions**
  - **Jump to SQL:** Added an action (with a gutter icon) that jumps to the SQL file from the Dao method.
    - Shortcut key: Alt+D
  - **Generate SQL:** Added an action to generate SQL files.
    - Shortcut key: Ctrl+Alt+G
- **Code Inspection**
  - Displays a quick fix when the corresponding SQL template file for a Dao method requiring one is not found.
  - Shows an error if there are parameter arguments not used as SQL bind variables.

### SQL Support Features

- **Actions**
  - **Jump to Dao:** Added an action to jump from the SQL file to the Dao method.
    - Shortcut key: Alt+D
  - **Jump to Declaration:** Added an action to jump from SQL bind variables to Dao parameters or class definitions.
    - Shortcut key: Alt+E
- **Code Inspection**
  - Displays an error when fields or methods that do not exist in the Dao parameters or class definition are used.
- **Code Completion**
  - Provides code completion for Dao method parameters, instance fields, and methods when used as bind variables.
  - Provides code completion for static fields and methods during static property calls.
  - Offers code completion for directive names.
  - Provides code completion for Doma built-in functions.
- **Refactoring**
  - Rename SQL file when renaming Dao method
  - Rename SQL file directory when renaming Dao
  - Change Dao package name or SQL file directory configuration when changing configuration

[Unreleased]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.7.0...HEAD
[0.7.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.2...v0.4.0
[0.3.2]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/domaframework/doma-tools-for-intellij/commits/v0.3.0
[#10]: https://github.com/domaframework/doma-tools-for-intellij/pull/10
[#102]: https://github.com/domaframework/doma-tools-for-intellij/pull/102
[#103]: https://github.com/domaframework/doma-tools-for-intellij/pull/103
[#104]: https://github.com/domaframework/doma-tools-for-intellij/pull/104
[#107]: https://github.com/domaframework/doma-tools-for-intellij/pull/107
[#109]: https://github.com/domaframework/doma-tools-for-intellij/pull/109
[#11]: https://github.com/domaframework/doma-tools-for-intellij/pull/11
[#110]: https://github.com/domaframework/doma-tools-for-intellij/pull/110
[#115]: https://github.com/domaframework/doma-tools-for-intellij/pull/115
[#117]: https://github.com/domaframework/doma-tools-for-intellij/pull/117
[#12]: https://github.com/domaframework/doma-tools-for-intellij/pull/12
[#120]: https://github.com/domaframework/doma-tools-for-intellij/pull/120
[#121]: https://github.com/domaframework/doma-tools-for-intellij/pull/121
[#122]: https://github.com/domaframework/doma-tools-for-intellij/pull/122
[#126]: https://github.com/domaframework/doma-tools-for-intellij/pull/126
[#128]: https://github.com/domaframework/doma-tools-for-intellij/pull/128
[#136]: https://github.com/domaframework/doma-tools-for-intellij/pull/136
[#142]: https://github.com/domaframework/doma-tools-for-intellij/pull/142
[#143]: https://github.com/domaframework/doma-tools-for-intellij/pull/143
[#145]: https://github.com/domaframework/doma-tools-for-intellij/pull/145
[#146]: https://github.com/domaframework/doma-tools-for-intellij/pull/146
[#148]: https://github.com/domaframework/doma-tools-for-intellij/pull/148
[#150]: https://github.com/domaframework/doma-tools-for-intellij/pull/150
[#151]: https://github.com/domaframework/doma-tools-for-intellij/pull/151
[#152]: https://github.com/domaframework/doma-tools-for-intellij/pull/152
[#156]: https://github.com/domaframework/doma-tools-for-intellij/pull/156
[#157]: https://github.com/domaframework/doma-tools-for-intellij/pull/157
[#159]: https://github.com/domaframework/doma-tools-for-intellij/pull/159
[#160]: https://github.com/domaframework/doma-tools-for-intellij/pull/160
[#161]: https://github.com/domaframework/doma-tools-for-intellij/pull/161
[#162]: https://github.com/domaframework/doma-tools-for-intellij/pull/162
[#164]: https://github.com/domaframework/doma-tools-for-intellij/pull/164
[#166]: https://github.com/domaframework/doma-tools-for-intellij/pull/166
[#167]: https://github.com/domaframework/doma-tools-for-intellij/pull/167
[#20]: https://github.com/domaframework/doma-tools-for-intellij/pull/20
[#21]: https://github.com/domaframework/doma-tools-for-intellij/pull/21
[#24]: https://github.com/domaframework/doma-tools-for-intellij/pull/24
[#25]: https://github.com/domaframework/doma-tools-for-intellij/pull/25
[#26]: https://github.com/domaframework/doma-tools-for-intellij/pull/26
[#27]: https://github.com/domaframework/doma-tools-for-intellij/pull/27
[#34]: https://github.com/domaframework/doma-tools-for-intellij/pull/34
[#35]: https://github.com/domaframework/doma-tools-for-intellij/pull/35
[#43]: https://github.com/domaframework/doma-tools-for-intellij/pull/43
[#45]: https://github.com/domaframework/doma-tools-for-intellij/pull/45
[#48]: https://github.com/domaframework/doma-tools-for-intellij/pull/48
[#49]: https://github.com/domaframework/doma-tools-for-intellij/pull/49
[#56]: https://github.com/domaframework/doma-tools-for-intellij/pull/56
[#57]: https://github.com/domaframework/doma-tools-for-intellij/pull/57
[#58]: https://github.com/domaframework/doma-tools-for-intellij/pull/58
[#59]: https://github.com/domaframework/doma-tools-for-intellij/pull/59
[#60]: https://github.com/domaframework/doma-tools-for-intellij/pull/60
[#61]: https://github.com/domaframework/doma-tools-for-intellij/pull/61
[#62]: https://github.com/domaframework/doma-tools-for-intellij/pull/62
[#64]: https://github.com/domaframework/doma-tools-for-intellij/pull/64
[#65]: https://github.com/domaframework/doma-tools-for-intellij/pull/65
[#68]: https://github.com/domaframework/doma-tools-for-intellij/pull/68
[#69]: https://github.com/domaframework/doma-tools-for-intellij/pull/69
[#7]: https://github.com/domaframework/doma-tools-for-intellij/pull/7
[#70]: https://github.com/domaframework/doma-tools-for-intellij/pull/70
[#78]: https://github.com/domaframework/doma-tools-for-intellij/pull/78
[#79]: https://github.com/domaframework/doma-tools-for-intellij/pull/79
[#8]: https://github.com/domaframework/doma-tools-for-intellij/pull/8
[#80]: https://github.com/domaframework/doma-tools-for-intellij/pull/80
[#82]: https://github.com/domaframework/doma-tools-for-intellij/pull/82
[#86]: https://github.com/domaframework/doma-tools-for-intellij/pull/86
[#87]: https://github.com/domaframework/doma-tools-for-intellij/pull/87
[#88]: https://github.com/domaframework/doma-tools-for-intellij/pull/88
[#89]: https://github.com/domaframework/doma-tools-for-intellij/pull/89
[#9]: https://github.com/domaframework/doma-tools-for-intellij/pull/9
[#90]: https://github.com/domaframework/doma-tools-for-intellij/pull/90
[#91]: https://github.com/domaframework/doma-tools-for-intellij/pull/91
[#92]: https://github.com/domaframework/doma-tools-for-intellij/pull/92
[#93]: https://github.com/domaframework/doma-tools-for-intellij/pull/93
[#94]: https://github.com/domaframework/doma-tools-for-intellij/pull/94
[#95]: https://github.com/domaframework/doma-tools-for-intellij/pull/95
[#96]: https://github.com/domaframework/doma-tools-for-intellij/pull/96
[#99]: https://github.com/domaframework/doma-tools-for-intellij/pull/99
[0.8.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/0.7.0...0.8.0
