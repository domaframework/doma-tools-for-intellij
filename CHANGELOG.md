# Doma Tools for IntelliJ

## [Unreleased]

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
