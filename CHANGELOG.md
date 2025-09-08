# Doma Tools for IntelliJ

## [Unreleased]

## [2.2.2] - 2025-09-08

### Maintenance

- Refactor SQL Formatter Block Relations and Translate Comments ([#444])

### Dependency Updates

- Update dependency org.jetbrains.intellij.platform to v2.9.0 ([#443])

[#444]:https://github.com/domaframework/doma-tools-for-intellij/pull/444
[#443]:https://github.com/domaframework/doma-tools-for-intellij/pull/443


## [2.2.1] - 2025-09-03

### Bug Fixes

- Fix: Unnecessary Space in Parser-Level Comments ([#440])
- Improve SQL CASE Statement Formatting ([#441])
- Add Formatting Support for Table Modification Queries ([#435])

### Dependency Updates

- Update dependency com.fasterxml.jackson.module:jackson-module-kotlin to v2.20.0 ([#432])
- Update dependency org.jetbrains.intellij.platform to v2.8.0 ([#442])

## [2.2.0] - 2025-08-29

### New Features

- Exception Analyzer Support ([#419])

### Bug Fixes

- Fix SQL formatter issues with ORDER BY/GROUP BY clauses and improve block indentation handling ([#431])
- Handle Blocks with the Same Name as Functions or Keywords as Word Blocks ([#426])
- Fix: Subquery Indentation ([#429])
- Fix Indentation Issues for Injected SQL and Correct Java Indent Size Retrieval ([#422])

### Dependency Updates

- Update dependency org.jetbrains.intellij.plugins:verifier-cli to v1.395 ([#424])

## [2.1.2] - 2025-08-22

### Bug Fixes

- Fix: SQL Formatter ([#411])

## [2.1.1] - 2025-08-19

### Bug Fixes

- Fix: Freeze During SQL Formatting ([#394])
- Fix error on code completion for SQL file with unrecognized DAO method ([#396])

### Dependency Updates

- Update dependency org.jetbrains.intellij.plugins:verifier-cli to v1.394 ([#389])
- Update actions/checkout action to v5 ([#381])

## [2.1.0] - 2025-08-08

### New Features

- Check for Disallowed Parameter Types Used as SQL Bind Variables ([#377])
- Exclude Specific DAO Parameter Types from SQL Bind Variable Suggestions ([#373])
- Enhance annotation option parameter validation for embedded properties ([#368])

### Bug Fixes

- Fixes to Annotation Option Validation ([#378])
- Fix DAO parameter type select options handling ([#371])
- Formatting Support for Single-Line Injected SQL in @Sql Annotations ([#364])

### Dependency Updates

- Update dependency gradle to v9 ([#354])
- Update dependency org.jetbrains.changelog to v2.4.0 ([#365])

## [2.0.0] - 2025-08-04

### New Features

- Sql Format Official Version ([#289])

### Bug Fixes

- Support for Using `@DataType`-Annotated Records in DAO Method Return and Parameter Types ([#350])
- Fix primitive type validation for DAO method return types ([#352])
- Update Japanese localization for DAO parameter inspection messages ([#317])

### Dependency Updates

- Update dependency org.jetbrains.changelog to v2.3.0 ([#340])
- Update dependency org.jetbrains.intellij.platform to v2.7.0 ([#339])
- Update plugin spotless to v7.2.1 ([#327])
- Update dependency com.pinterest.ktlint:ktlint-cli to v1.7.1 ([#326])
- Update plugin spotless to v7.2.0 ([#325])
- Update dependency com.fasterxml.jackson.module:jackson-module-kotlin to v2.19.2 ([#324])
- Update dependency com.pinterest.ktlint:ktlint-cli to v1.7.0 ([#315])
- Update dependency com.google.googlejavaformat:google-java-format to v1.28.0 ([#309])
- Update plugin spotless to v7.1.0 ([#299])

## [1.1.1] - 2025-07-03

### Bug Fixes

- Enable SQL File Generation in Multi-Module Projects ([#292])
- Ensure Correct SQL File Path Generation When Package Name Matches Source Directory ([#286])

## [1.1.0] - 2025-06-25

### New Features

- DAO Method Parameter Validation ([#257])
- Check DAO Method Return Type ([#247])
- Factory DAO Method Check ([#272])
- Implement Return Type Checking for Select and Function Annotations ([#266])

### Bug Fixes

- Expand directive incorrectly treated as bind variable directive ([#255])
- Enhance the usage checks of DAO method parameters for subtypes. ([#273])
- Fix Return Type Check ([#270])
- Resource for parameter error messages of Processor methods ([#269])
- Refactoring dao method inspections ([#274])

### Dependency Updates

- Update dependency org.jetbrains.kotlin.jvm to v2.2.0 ([#271])
- Update dependency org.jetbrains.intellij.plugins:verifier-cli to v1.388 ([#258])
- Update dependency com.fasterxml.jackson.module:jackson-module-kotlin to v2.19.1 ([#253])

## [1.0.0] - 2025-06-09

### Bug Fixes

- No error when static field-access element shares the same name as a parameter ([#243])
- No error when a field-access element shares the same name as a parameter ([#241])
- Test data check skipped for directives at end of file ([#214])
- Add fallback for parent class name retrieval in validation property result ([#216])
- Code completion fails after existing values in method arguments ([#215])
- Instance member suggestions missing inside static property call arguments  ([#213])
- Cannot resolve references from within argument-parameter elements ([#207])
- Update error messages ([#205])
- Reflect Custom Error Levels in DAO Method Inspection ([#232])
- Fix FieldAccess code completion ([#229])
- Fix code completion for static field access ([#226])

### Maintenance

- Refactor: Split inspection logic ([#230])
- Add pull request trigger to release drafter workflow ([#168])

### Dependency Updates

- Update dependency gradle to v8.14.2 ([#242])
- Update plugin verifier version to 1.386 ([#209])
- Update pluginVerifier version in dependencies and build configuration ([#218])
- Update plugin spotless to v7.0.4 ([#203])
- Update dependency gradle to v8.14.1 ([#188])

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

## [0.7.0] - 2025-05-15

### New Features

- Error message details ([#162])
- Completion for directive items ([#160])
- for directive specific word element handling ([#150])
- Code completion and code inspection for static property call package class names ([#148])
- Add SQL test data validation inspections ([#136])

### Bug Fixes

- Fix to not display DAO parameter information in the document for elements defined with the for directive. ([#167])
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

### DAO Support Features

- **Actions**
  - **Jump to SQL:** Added an action (with a gutter icon) that jumps to the SQL file from the DAO method.
    - Shortcut key: Alt+D
  - **Generate SQL:** Added an action to generate SQL files.
    - Shortcut key: Ctrl+Alt+G
- **Code Inspection**
  - Displays a quick fix when the corresponding SQL template file for a DAO method requiring one is not found.
  - Shows an error if there are parameter arguments not used as SQL bind variables.

### SQL Support Features

- **Actions**
  - **Jump to DAO:** Added an action to jump from the SQL file to the DAO method.
    - Shortcut key: Alt+D
  - **Jump to Declaration:** Added an action to jump from SQL bind variables to DAO parameters or class definitions.
    - Shortcut key: Alt+E
- **Code Inspection**
  - Displays an error when fields or methods that do not exist in the DAO parameters or class definition are used.
- **Code Completion**
  - Provides code completion for DAO method parameters, instance fields, and methods when used as bind variables.
  - Provides code completion for static fields and methods during static property calls.
  - Offers code completion for directive names.
  - Provides code completion for Doma built-in functions.
- **Refactoring**
  - Rename SQL file when renaming DAO method
  - Rename SQL file directory when renaming DAO
  - Change DAO package name or SQL file directory configuration when changing configuration

[Unreleased]: https://github.com/domaframework/doma-tools-for-intellij/compare/v2.2.1...HEAD
[2.2.1]: https://github.com/domaframework/doma-tools-for-intellij/compare/v2.2.0...v2.2.1
[2.2.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v2.1.2...v2.2.0
[2.1.2]: https://github.com/domaframework/doma-tools-for-intellij/compare/v2.1.1...v2.1.2
[2.1.1]: https://github.com/domaframework/doma-tools-for-intellij/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v1.1.1...v2.0.0
[1.1.1]: https://github.com/domaframework/doma-tools-for-intellij/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.8.0...v1.0.0
[0.8.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.2...v0.4.0
[0.3.2]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/domaframework/doma-tools-for-intellij/commits/v0.3.0
[#99]: https://github.com/domaframework/doma-tools-for-intellij/pull/99
[#96]: https://github.com/domaframework/doma-tools-for-intellij/pull/96
[#95]: https://github.com/domaframework/doma-tools-for-intellij/pull/95
[#94]: https://github.com/domaframework/doma-tools-for-intellij/pull/94
[#93]: https://github.com/domaframework/doma-tools-for-intellij/pull/93
[#92]: https://github.com/domaframework/doma-tools-for-intellij/pull/92
[#91]: https://github.com/domaframework/doma-tools-for-intellij/pull/91
[#90]: https://github.com/domaframework/doma-tools-for-intellij/pull/90
[#9]: https://github.com/domaframework/doma-tools-for-intellij/pull/9
[#89]: https://github.com/domaframework/doma-tools-for-intellij/pull/89
[#88]: https://github.com/domaframework/doma-tools-for-intellij/pull/88
[#87]: https://github.com/domaframework/doma-tools-for-intellij/pull/87
[#86]: https://github.com/domaframework/doma-tools-for-intellij/pull/86
[#82]: https://github.com/domaframework/doma-tools-for-intellij/pull/82
[#80]: https://github.com/domaframework/doma-tools-for-intellij/pull/80
[#8]: https://github.com/domaframework/doma-tools-for-intellij/pull/8
[#79]: https://github.com/domaframework/doma-tools-for-intellij/pull/79
[#78]: https://github.com/domaframework/doma-tools-for-intellij/pull/78
[#70]: https://github.com/domaframework/doma-tools-for-intellij/pull/70
[#7]: https://github.com/domaframework/doma-tools-for-intellij/pull/7
[#69]: https://github.com/domaframework/doma-tools-for-intellij/pull/69
[#68]: https://github.com/domaframework/doma-tools-for-intellij/pull/68
[#65]: https://github.com/domaframework/doma-tools-for-intellij/pull/65
[#64]: https://github.com/domaframework/doma-tools-for-intellij/pull/64
[#62]: https://github.com/domaframework/doma-tools-for-intellij/pull/62
[#61]: https://github.com/domaframework/doma-tools-for-intellij/pull/61
[#60]: https://github.com/domaframework/doma-tools-for-intellij/pull/60
[#59]: https://github.com/domaframework/doma-tools-for-intellij/pull/59
[#58]: https://github.com/domaframework/doma-tools-for-intellij/pull/58
[#57]: https://github.com/domaframework/doma-tools-for-intellij/pull/57
[#56]: https://github.com/domaframework/doma-tools-for-intellij/pull/56
[#49]: https://github.com/domaframework/doma-tools-for-intellij/pull/49
[#48]: https://github.com/domaframework/doma-tools-for-intellij/pull/48
[#45]: https://github.com/domaframework/doma-tools-for-intellij/pull/45
[#442]: https://github.com/domaframework/doma-tools-for-intellij/pull/442
[#441]: https://github.com/domaframework/doma-tools-for-intellij/pull/441
[#440]: https://github.com/domaframework/doma-tools-for-intellij/pull/440
[#435]: https://github.com/domaframework/doma-tools-for-intellij/pull/435
[#432]: https://github.com/domaframework/doma-tools-for-intellij/pull/432
[#431]: https://github.com/domaframework/doma-tools-for-intellij/pull/431
[#43]: https://github.com/domaframework/doma-tools-for-intellij/pull/43
[#429]: https://github.com/domaframework/doma-tools-for-intellij/pull/429
[#426]: https://github.com/domaframework/doma-tools-for-intellij/pull/426
[#424]: https://github.com/domaframework/doma-tools-for-intellij/pull/424
[#422]: https://github.com/domaframework/doma-tools-for-intellij/pull/422
[#419]: https://github.com/domaframework/doma-tools-for-intellij/pull/419
[#417]: https://github.com/domaframework/doma-tools-for-intellij/pull/417
[#411]: https://github.com/domaframework/doma-tools-for-intellij/pull/411
[#396]: https://github.com/domaframework/doma-tools-for-intellij/pull/396
[#394]: https://github.com/domaframework/doma-tools-for-intellij/pull/394
[#389]: https://github.com/domaframework/doma-tools-for-intellij/pull/389
[#381]: https://github.com/domaframework/doma-tools-for-intellij/pull/381
[#378]: https://github.com/domaframework/doma-tools-for-intellij/pull/378
[#377]: https://github.com/domaframework/doma-tools-for-intellij/pull/377
[#373]: https://github.com/domaframework/doma-tools-for-intellij/pull/373
[#371]: https://github.com/domaframework/doma-tools-for-intellij/pull/371
[#368]: https://github.com/domaframework/doma-tools-for-intellij/pull/368
[#365]: https://github.com/domaframework/doma-tools-for-intellij/pull/365
[#364]: https://github.com/domaframework/doma-tools-for-intellij/pull/364
[#354]: https://github.com/domaframework/doma-tools-for-intellij/pull/354
[#352]: https://github.com/domaframework/doma-tools-for-intellij/pull/352
[#350]: https://github.com/domaframework/doma-tools-for-intellij/pull/350
[#35]: https://github.com/domaframework/doma-tools-for-intellij/pull/35
[#340]: https://github.com/domaframework/doma-tools-for-intellij/pull/340
[#34]: https://github.com/domaframework/doma-tools-for-intellij/pull/34
[#339]: https://github.com/domaframework/doma-tools-for-intellij/pull/339
[#327]: https://github.com/domaframework/doma-tools-for-intellij/pull/327
[#326]: https://github.com/domaframework/doma-tools-for-intellij/pull/326
[#325]: https://github.com/domaframework/doma-tools-for-intellij/pull/325
[#324]: https://github.com/domaframework/doma-tools-for-intellij/pull/324
[#317]: https://github.com/domaframework/doma-tools-for-intellij/pull/317
[#315]: https://github.com/domaframework/doma-tools-for-intellij/pull/315
[#309]: https://github.com/domaframework/doma-tools-for-intellij/pull/309
[#299]: https://github.com/domaframework/doma-tools-for-intellij/pull/299
[#292]: https://github.com/domaframework/doma-tools-for-intellij/pull/292
[#289]: https://github.com/domaframework/doma-tools-for-intellij/pull/289
[#286]: https://github.com/domaframework/doma-tools-for-intellij/pull/286
[#275]: https://github.com/domaframework/doma-tools-for-intellij/pull/275
[#274]: https://github.com/domaframework/doma-tools-for-intellij/pull/274
[#273]: https://github.com/domaframework/doma-tools-for-intellij/pull/273
[#272]: https://github.com/domaframework/doma-tools-for-intellij/pull/272
[#271]: https://github.com/domaframework/doma-tools-for-intellij/pull/271
[#270]: https://github.com/domaframework/doma-tools-for-intellij/pull/270
[#27]: https://github.com/domaframework/doma-tools-for-intellij/pull/27
[#269]: https://github.com/domaframework/doma-tools-for-intellij/pull/269
[#266]: https://github.com/domaframework/doma-tools-for-intellij/pull/266
[#26]: https://github.com/domaframework/doma-tools-for-intellij/pull/26
[#258]: https://github.com/domaframework/doma-tools-for-intellij/pull/258
[#257]: https://github.com/domaframework/doma-tools-for-intellij/pull/257
[#255]: https://github.com/domaframework/doma-tools-for-intellij/pull/255
[#253]: https://github.com/domaframework/doma-tools-for-intellij/pull/253
[#25]: https://github.com/domaframework/doma-tools-for-intellij/pull/25
[#247]: https://github.com/domaframework/doma-tools-for-intellij/pull/247
[#243]: https://github.com/domaframework/doma-tools-for-intellij/pull/243
[#242]: https://github.com/domaframework/doma-tools-for-intellij/pull/242
[#241]: https://github.com/domaframework/doma-tools-for-intellij/pull/241
[#24]: https://github.com/domaframework/doma-tools-for-intellij/pull/24
[#232]: https://github.com/domaframework/doma-tools-for-intellij/pull/232
[#230]: https://github.com/domaframework/doma-tools-for-intellij/pull/230
[#229]: https://github.com/domaframework/doma-tools-for-intellij/pull/229
[#226]: https://github.com/domaframework/doma-tools-for-intellij/pull/226
[#218]: https://github.com/domaframework/doma-tools-for-intellij/pull/218
[#216]: https://github.com/domaframework/doma-tools-for-intellij/pull/216
[#215]: https://github.com/domaframework/doma-tools-for-intellij/pull/215
[#214]: https://github.com/domaframework/doma-tools-for-intellij/pull/214
[#213]: https://github.com/domaframework/doma-tools-for-intellij/pull/213
[#21]: https://github.com/domaframework/doma-tools-for-intellij/pull/21
[#209]: https://github.com/domaframework/doma-tools-for-intellij/pull/209
[#207]: https://github.com/domaframework/doma-tools-for-intellij/pull/207
[#205]: https://github.com/domaframework/doma-tools-for-intellij/pull/205
[#203]: https://github.com/domaframework/doma-tools-for-intellij/pull/203
[#200]: https://github.com/domaframework/doma-tools-for-intellij/pull/200
[#20]: https://github.com/domaframework/doma-tools-for-intellij/pull/20
[#199]: https://github.com/domaframework/doma-tools-for-intellij/pull/199
[#196]: https://github.com/domaframework/doma-tools-for-intellij/pull/196
[#195]: https://github.com/domaframework/doma-tools-for-intellij/pull/195
[#193]: https://github.com/domaframework/doma-tools-for-intellij/pull/193
[#188]: https://github.com/domaframework/doma-tools-for-intellij/pull/188
[#185]: https://github.com/domaframework/doma-tools-for-intellij/pull/185
[#184]: https://github.com/domaframework/doma-tools-for-intellij/pull/184
[#181]: https://github.com/domaframework/doma-tools-for-intellij/pull/181
[#180]: https://github.com/domaframework/doma-tools-for-intellij/pull/180
[#178]: https://github.com/domaframework/doma-tools-for-intellij/pull/178
[#177]: https://github.com/domaframework/doma-tools-for-intellij/pull/177
[#175]: https://github.com/domaframework/doma-tools-for-intellij/pull/175
[#173]: https://github.com/domaframework/doma-tools-for-intellij/pull/173
[#172]: https://github.com/domaframework/doma-tools-for-intellij/pull/172
[#168]: https://github.com/domaframework/doma-tools-for-intellij/pull/168
[#167]: https://github.com/domaframework/doma-tools-for-intellij/pull/167
[#166]: https://github.com/domaframework/doma-tools-for-intellij/pull/166
[#164]: https://github.com/domaframework/doma-tools-for-intellij/pull/164
[#162]: https://github.com/domaframework/doma-tools-for-intellij/pull/162
[#161]: https://github.com/domaframework/doma-tools-for-intellij/pull/161
[#160]: https://github.com/domaframework/doma-tools-for-intellij/pull/160
[#159]: https://github.com/domaframework/doma-tools-for-intellij/pull/159
[#157]: https://github.com/domaframework/doma-tools-for-intellij/pull/157
[#156]: https://github.com/domaframework/doma-tools-for-intellij/pull/156
[#152]: https://github.com/domaframework/doma-tools-for-intellij/pull/152
[#151]: https://github.com/domaframework/doma-tools-for-intellij/pull/151
[#150]: https://github.com/domaframework/doma-tools-for-intellij/pull/150
[#148]: https://github.com/domaframework/doma-tools-for-intellij/pull/148
[#146]: https://github.com/domaframework/doma-tools-for-intellij/pull/146
[#145]: https://github.com/domaframework/doma-tools-for-intellij/pull/145
[#143]: https://github.com/domaframework/doma-tools-for-intellij/pull/143
[#142]: https://github.com/domaframework/doma-tools-for-intellij/pull/142
[#136]: https://github.com/domaframework/doma-tools-for-intellij/pull/136
[#128]: https://github.com/domaframework/doma-tools-for-intellij/pull/128
[#126]: https://github.com/domaframework/doma-tools-for-intellij/pull/126
[#122]: https://github.com/domaframework/doma-tools-for-intellij/pull/122
[#121]: https://github.com/domaframework/doma-tools-for-intellij/pull/121
[#120]: https://github.com/domaframework/doma-tools-for-intellij/pull/120
[#12]: https://github.com/domaframework/doma-tools-for-intellij/pull/12
[#117]: https://github.com/domaframework/doma-tools-for-intellij/pull/117
[#115]: https://github.com/domaframework/doma-tools-for-intellij/pull/115
[#110]: https://github.com/domaframework/doma-tools-for-intellij/pull/110
[#11]: https://github.com/domaframework/doma-tools-for-intellij/pull/11
[#109]: https://github.com/domaframework/doma-tools-for-intellij/pull/109
[#107]: https://github.com/domaframework/doma-tools-for-intellij/pull/107
[#104]: https://github.com/domaframework/doma-tools-for-intellij/pull/104
[#103]: https://github.com/domaframework/doma-tools-for-intellij/pull/103
[#102]: https://github.com/domaframework/doma-tools-for-intellij/pull/102
[#10]: https://github.com/domaframework/doma-tools-for-intellij/pull/10
[2.2.2]: https://github.com/domaframework/doma-tools-for-intellij/compare/2.2.1...2.2.2
