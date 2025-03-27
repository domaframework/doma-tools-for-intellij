# Doma Tools for IntelliJ

## [Unreleased]

## [0.3.3] - 2025-03-27

### Dependency Updates

- Update dependency org.jetbrains.kotlin.jvm to v2.1.20 ([#61])

[#61]:https://github.com/domaframework/doma-tools-for-intellij/pull/61


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

[#56]:https://github.com/domaframework/doma-tools-for-intellij/pull/56
[#57]:https://github.com/domaframework/doma-tools-for-intellij/pull/57
[#58]:https://github.com/domaframework/doma-tools-for-intellij/pull/58
[#59]:https://github.com/domaframework/doma-tools-for-intellij/pull/59
[#64]:https://github.com/domaframework/doma-tools-for-intellij/pull/64
[#65]:https://github.com/domaframework/doma-tools-for-intellij/pull/65
[#48]:https://github.com/domaframework/doma-tools-for-intellij/pull/48
[#49]:https://github.com/domaframework/doma-tools-for-intellij/pull/49
[#45]:https://github.com/domaframework/doma-tools-for-intellij/pull/45
[#43]:https://github.com/domaframework/doma-tools-for-intellij/pull/43
[#60]:https://github.com/domaframework/doma-tools-for-intellij/pull/60


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

[#20]:https://github.com/domaframework/doma-tools-for-intellij/pull/20
[#21]:https://github.com/domaframework/doma-tools-for-intellij/pull/21
[#35]:https://github.com/domaframework/doma-tools-for-intellij/pull/35
[#34]:https://github.com/domaframework/doma-tools-for-intellij/pull/34
[#26]:https://github.com/domaframework/doma-tools-for-intellij/pull/26
[#25]:https://github.com/domaframework/doma-tools-for-intellij/pull/25
[#27]:https://github.com/domaframework/doma-tools-for-intellij/pull/27
[#10]:https://github.com/domaframework/doma-tools-for-intellij/pull/10
[#24]:https://github.com/domaframework/doma-tools-for-intellij/pull/24
[#9]:https://github.com/domaframework/doma-tools-for-intellij/pull/9
[#12]:https://github.com/domaframework/doma-tools-for-intellij/pull/12
[#11]:https://github.com/domaframework/doma-tools-for-intellij/pull/11
[#8]:https://github.com/domaframework/doma-tools-for-intellij/pull/8
[#7]:https://github.com/domaframework/doma-tools-for-intellij/pull/7


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

[Unreleased]: https://github.com/domaframework/doma-tools-for-intellij/commits/main
[0.3.0]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.0
[0.3.1]: https://github.com/domaframework/doma-tools-for-intellij/compare/v0.3.0...0.3.1
[0.3.2]: https://github.com/domaframework/doma-tools-for-intellij/compare/0.3.1...0.3.2
[0.3.3]: https://github.com/domaframework/doma-tools-for-intellij/compare/0.3.2...0.3.3
