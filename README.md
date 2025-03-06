# Doma Tools for IntelliJ

<!-- Plugin description -->
“Doma Tools” is a plugin that supports the development of Doma-based projects in IntelliJ.

It checks associations between Dao and SQL, and offers coding support features for Doma syntax,
such as generating SQL template files, navigating between files, and inspections to ensure the validity of bind variables.
<!-- Plugin description end -->

## Features

## Actions
The plugin adds some actions, gutter icons.
Shortcut keys can be used for actions

![action.png](images/action.png)

- **Jump to SQL(Alt+D)**
  - Jump to action from Dao to SQL
  - You can also jump to the SQL file from the gutter icon that is displayed together.
- **Generate SQL(Ctrl+Alt+G)**
  - Generate SQL file
- **Jump to Dao(Alt+D)**
  - Jump to action from SQL to Dao
  - You can also jump to the Dao Method from the gutter icon that is displayed together.
- **Jump to Declaration(Alt+E)**
  - Jump to action from SQL bind variable to declaration location
  - ex: Dao arguments, fields, method declaration

## Inspection
Check that bind variables are used appropriately for Dao and SQL associations.
The plugin also provides quick fixes for Dao methods where the required SQL files do not exist.

- Quick fix for missing SQL template file
  ![quickfix.png](images/quickfix.png)
- Checking for Dao method arguments not used in bind variables
  ![inspection.png](images/inspection.png)

## Completion
Adds code completion functionality to support indexing of Doma directives and bind variables

- Suggest Dao method arguments in bind variable directives
  ![complete_bindVariables.png](images/complete_bindVariables.png)
- Refer to class definition from Dao method argument type and suggest fields and methods
  ![complete_member.png](images/cpmplete_member.png)
- Suggest members defined as static in static fields and method calls
- Suggest Doma directives
- Directives such as Condition, Loop, Population are suggested after “%”
- Suggest built-in functions after “@”

## Refactoring
Along with the Dao name change, we will refactor the SQL file directory and file name.

- After refactoring the Dao name, change the SQL deployment directory name as well.
- After refactoring the Dao method name, we will also change the SQL file name.
- After refactoring the Dao package, we will also change the SQL directory.

## Settings
Some functions of "Doma Tools" can be customized from the settings screen.

- Enabling/disabling inspections and customizing error levels
- Highlight color settings for SQL elements
![setting_highlight.png](images/setting_highlight.png)
- Customize action shortcut keys
