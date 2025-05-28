# Doma Tools for IntelliJ
[![Current Release](https://img.shields.io/badge/release-Marketplace-orange.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/26701-doma-tools)
[![Build](https://github.com/domaframework/doma-tools-for-intellij/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/domaframework/doma-tools-for-intellij/actions/workflows/build.yml)
[![Release](https://github.com/domaframework/doma-tools-for-intellij/actions/workflows/release.yml/badge.svg?event=release)](https://github.com/domaframework/doma-tools-for-intellij/actions/workflows/release.yml)

<!-- Plugin description -->
"Doma Tools" is a plugin that supports the development of [Doma](https://github.com/domaframework/doma) based projects in IntelliJ.

It checks associations between Dao and SQL, and offers coding support features for Doma syntax,
such as generating SQL template files, navigating between files, and inspections to ensure the validity of bind variables.

<!-- Plugin description end -->

# Features

## Actions
The plugin adds some actions, gutter icons.
Shortcut keys can be used for actions
![FileJump.gif](images/gif/FileJump.gif)

- **Jump to SQL**
  - Jump to action from Dao to SQL
  - You can also jump to the SQL file from the gutter icon that is displayed together.
- **Generate SQL**
  - Generate SQL file
- **Jump to Dao**
  - Jump to action from SQL to Dao
  - You can also jump to the Dao Method from the gutter icon that is displayed together.

## Inspection
Check that bind variables are used appropriately for Dao and SQL associations.
The plugin also provides quick fixes for Dao methods where the required SQL files do not exist.

- Quick fix for missing SQL template file
  ![quickfix.png](images/quickfix.png)
- Checking for Dao method arguments not used in bind variables
  ![inspection.png](images/inspection.png)
- Check the class name and package name for static property calls
  ![inspectionPackageName.png](images/inspectionPackageName.png)
- Optional types are recognized as their element type (e.g. Optional<String> is treated as String).
- Checks calls to custom functions and error-highlights any methods that aren’t defined in the classes registered via the settings.

## Completion
Adds code completion functionality to support indexing of Doma directives and bind variables

- Suggest Dao method arguments in bind variable directives
  ![Completion.gif](images/gif/Completion.gif)
- Refer to class definition from Dao method argument type and suggest fields and methods
- Provide code completion for class and package names used in static property calls.
  ![CompletionStatic.gif](images/gif/CompletionStatic.gif)
- Suggest members defined as static in static fields and method calls
- Suggest Doma directives
- Directives such as Condition, Loop, Population are suggested after “%”
- Suggest built-in functions after “@”
- Optional types are recognized as their element type (e.g. Optional<String> is treated as String).
- Suggest functions during code completion from the ExpressionFunctions implementation classes registered in the settings.

## Refactoring
Along with the Dao name change, we will refactor the SQL file directory and file name.

- After refactoring the Dao name, change the SQL deployment directory name as well.
- After refactoring the Dao method name, we will also change the SQL file name.
- After refactoring the Dao package, we will also change the SQL directory.
![RenameDao.gif](images/gif/RenameDao.gif)

## Formatter(Preview)
Provides code formatting for SQL syntax.
This feature is in preview. You cannot customize the indentation or keywords to be broken down!

Automatic indentation on newlines provided by the SQL formatting feature is disabled by default.

To enable auto-indentation, toggle the corresponding flag in the settings screen below.

`Settings > Other Settings > Doma Tools > Enable auto-indent for SQL`

![Format.gif](images/gif/Format.gif)

## Reference resolution
Ctrl+Click on a bind variable in an SQL file to jump to its source symbol:

This feature works in source JARs as well, but in binary JARs, if the DAO method’s argument parameter names have been changed, the references cannot be resolved and the feature will not function.

- The DAO method’s argument parameter definition
- The field and method definitions on that parameter’s type
- The class definition referenced by @ClassName@
- Resolve references for custom functions using the ExpressionFunctions implementation class in which they are defined.
- You can also jump using the **Go To > Declaration Usage** menu.

![Reference.gif](images/gif/Reference.gif)

## Settings
Some functions of "Doma Tools" can be customized from the settings screen.

- Enabling/disabling inspections and customizing error levels
- Highlight color settings for SQL elements
![setting_highlight.png](images/setting_highlight.png)
- Customize action shortcut keys
- Toggle the SQL formatting feature on or off
- Specify the class names that define custom functions.

**If you want to use custom functions defined in your own ExpressionFunctions implementation class,
place a `doma.compile.config` file directly under the resources directory and describe the `doma.expr.functions` entry.**

ex) doma.compile.config
```properties
doma.expr.functions=example.expression.MyExpressionFunctions
```

The contents of `doma.compile.config` are cached.
The cache is updated based on the file's last modified timestamp, so please make sure to save the file after making any changes.
