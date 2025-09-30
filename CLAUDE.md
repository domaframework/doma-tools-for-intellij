# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Essential Build Commands

```bash
# Build the plugin
./gradlew build

# Generate parser and lexer from BNF/Flex files (required before build)
./gradlew generateLexer generateParser

# Run tests
./gradlew test

# Run single test class
./gradlew check

# Code formatting and linting
./gradlew spotlessApply
./gradlew spotlessCheck

# Run IntelliJ IDE with plugin for testing
./gradlew runIde

# Build plugin distribution
./gradlew buildPlugin

# Verify plugin compatibility
./gradlew verifyPlugin
```

---
## Architecture Overview

This is an IntelliJ IDEA plugin that provides comprehensive development support for the Doma framework (a Java ORM). The plugin bridges the gap between DAO (Data Access Object) Java/Kotlin classes and their corresponding SQL template files.

### Custom Language Support

The plugin defines a custom language "DomaSql" that extends standard SQL with Doma-specific directives:
- **Parser/Lexer**: Generated from `Sql.bnf` and `Sql.flex` files using GrammarKit
- **Language Definition**: `SqlLanguage`, `SqlFileType`, `SqlParserDefinition`
- **Custom Directives**: `/*%if*/`, `/*%for*/`, `/*# embedded */`, `/* bind variables */`

### Key Components Architecture

**Navigation & Actions**: Bidirectional jumping between DAO methods and SQL files
- `action/dao/JumpToSQLFromDaoAction` - DAO method → SQL file
- `action/sql/JumpToDaoFromSQLAction` - SQL file → DAO method
- `gutter/` - Visual gutter icons for quick navigation

**Code Analysis & Validation**:
- `inspection/dao/` - DAO-related inspections (parameter types, return types, SQL file existence)
- `inspection/sql/` - SQL-related inspections (bind variables, function calls, directive types)
- `common/validation/result/` - Validation result types for all checks

**IDE Integration**:
- `contributor/sql/SqlCompletionContributor` - Code completion for SQL files
- `formatter/` - SQL formatting with custom spacing and block handling
- `reference/` - PSI reference resolution for symbols in SQL files
- `refactoring/dao/` - Automatic SQL file/directory renaming when DAOs are refactored

### Critical Relationships

**DAO ↔ SQL Mapping**: The plugin uses naming conventions to map DAO methods to SQL files:
- DAO class `com.example.EmployeeDao` maps to SQL directory `META-INF/com/example/EmployeeDao/`
- Method `findByName` maps to SQL file `findByName.sql`

**PSI Integration**: Heavily uses IntelliJ's PSI (Program Structure Interface):
- `common/psi/PsiDaoMethod` - Core class representing DAO methods with SQL file resolution
- `common/dao/DaoClass` - Utilities for detecting `@Dao` annotated classes
- Custom PSI elements for SQL expressions and directives

## Development Notes

### Parser Generation
Always run `generateLexer` and `generateParser` tasks after modifying `Sql.bnf` or `Sql.flex`. Generated files go to `src/main/gen/` and are excluded from version control but included in compilation.

### Testing Strategy
Tests use `DomaProjectDescriptor` to set up IntelliJ test environments with Doma dependencies. Test data is in `src/test/testData/` with separate directories for different feature tests.

### Configuration Support
The plugin reads `doma.compile.config` files to support custom ExpressionFunctions. This configuration is cached and invalidated based on file modification timestamps.

### Formatter Implementation
The SQL formatter uses a custom block-based approach with `SqlBlock` hierarchy and `SqlFormattingModelBuilder`. It's currently in preview mode with limited customization options.

---
# Tasks for Claude Code
If you edit the code, run `./gradle spotless` and `/gradle check`.

---
# Code Guidelines
Custom language implementations are written in Java, while plugin features are implemented in Kotlin.
Custom language lexers and parsers are auto-generated using Grammar-Kit.

## Architecture
Plugin architecture organizes features into separate packages, with sub-packages categorized by class roles.
Features are separated into packages for DAO classes and SQL files.
## Package Structure
```
Feature Package
├── dao
│   └── AnAction subclass
└── sql
    └── AnAction subclass
```

## Common

**Accessing `PsiClass` Members**
When retrieving fields and methods from `PsiClass`, use `allFields` and `allMethods` instead of `fields` and `methods`.
This is necessary to include members defined in parent classes.

Alternatively, you can use [PsiParentClass](src/main/kotlin/org/domaframework/doma/intellij/common/psi/PsiParentClass.kt)
to achieve equivalent functionality with `findField()` and `findMethod()`.

**Separating Complex Logic**
Each feature requires implementing corresponding classes following the IntelliJ platform conventions.
Complex logic should not be implemented directly in these corresponding classes but delegated to separate classes (Processors or Handlers).

## Main Classes of Each Feature

### Actions
Action functionality for navigating between DAO files and SQL files

- **[Feature Package](src/main/kotlin/org/domaframework/doma/intellij/action)**
- **Main Classes**: `AnAction` subclasses

**Class Naming Rules**:
Class names should have `Action` as a suffix.
Classes should not have properties; necessary information should be obtained within functions.

### Intention Actions for SQL/Annotation Conversion

**ConvertSqlFileToAnnotationAction**: Converts SQL file content to @Sql annotation
- Extends `PsiElementBaseIntentionAction` for context-sensitive availability
- Available when:
  - Current file is a SQL file with corresponding DAO method
  - DAO method doesn't have @Sql annotation
  - DAO method has supported annotation (@Select, @Insert, @Update, @Delete, etc.) with sqlFile=true
- Uses `SqlAnnotationConverter` to perform the conversion
- Invoked via Alt+Enter on SQL file

**ConvertSqlAnnotationToFileAction**: Converts @Sql annotation to SQL file
- Extends `PsiElementBaseIntentionAction` for context-sensitive availability
- Available when:
  - Cursor is on a DAO method with @Sql annotation
  - Method has supported annotation (@Select, @Insert, @Update, @Delete, etc.)
- Uses `SqlAnnotationConverter` to perform the conversion
- Creates SQL file in appropriate directory structure (META-INF/...)
- Invoked via Alt+Enter on DAO method with @Sql annotation

Both actions use `WriteCommandAction` to ensure changes are undoable and properly tracked by IntelliJ's local history.

### LineMarker
Line marker functionality for DAO methods and DOMA directives in SQL

- **[Feature Package](src/main/kotlin/org/domaframework/doma/intellij/gutter)**
- **Main Classes**: `LineMarkerProvider` subclasses
  - Line marker class for DAO: `DaoLineMarkerProvider`
  - Line marker class for SQL: `SqlLineMarkerProvider`
  - **Class Naming Rules**: Use `Provider` as a suffix

### Inspections
Code inspection functionality for DAO methods and DOMA directives in SQL

- **[Feature Package](src/main/kotlin/org/domaframework/doma/intellij/inspection)**
- **Main Classes**: `InspectionTool`
  - Code inspection class for DAO: `AbstractBaseJavaLocalInspectionTool` subclasses
    - **DaoAnnotationOptionParameterInspection**: DAOメソッドに付与されたアノテーションオプション（include/exclude）にEntityクラスで定義されたフィールドが指定されていることを検査
    - **DaoAnnotationReturnTypeInspection**: DAOメソッドの戻り値が適切であることを検査
    - **DaoMethodParamTypeInspection**: DAOメソッドのパラメータが適切であることを検査
    - **SqlFileExistInspection**: DAOメソッドに対応するSQLファイルが存在することを検査
    - **UsedDaoMethodParamInspection**: DAOメソッドのパラメータがSQLファイル内で使用されていることを検査
  - Code inspection class for SQL: `LocalInspectionTool` subclasses
    - **SqlBindVariableInspection**: バインド変数内の要素定義元を検査
    - **SqlFunctionCallInspection**: カスタム関数、組み込み関数の定義元を検査
    - **SqlLoopDirectiveTypeInspection**: ループディレクティブで指定されている変数の型を検査
    - **SqlTestDataInspection**: ディレクティブの後ろにテストデータが存在していることを検査
  - **Class Naming Rules**: Use `Inspection` as a suffix
- Processor: Classes that check inspection target elements. Provides common check processing for inspection targets that meet specific conditions
  - **Class Naming Rules**: Use `Checker` or `Processor` as a suffix depending on the purpose
- Provider: Provider that returns a list of `InspectionTool` subclasses
  - **Class Naming Rules**: Use `Provider` as a suffix
- Visitor: Classes that search for inspection target elements. Called from `InspectionTool`
  - **Class Naming Rules**: Have the same name as the calling `InspectionTool` with `Visitor` as a suffix
- QuickFix: Classes that provide fix actions for inspection results
  - Consists of a `Factory` object that provides quick fix objects and the main `QuickFix` object
  - **Class Naming Rules**
    - `Factory` object: Use `QuickFixFactory` as a suffix
    - `QuickFix` object: Use `QuickFix` as a suffix
- [ValidationResult](src/main/kotlin/org/domaframework/doma/intellij/common/validation/result): Classes that provide error messages and highlights for code inspection results
  - **Class Naming Rules**: Use `ValidationResult` as a suffix. Name classes according to the message resources to be displayed

**Coding Rule**
For code inspection features, always implement `InspectionTool` and `Visitor` as separate classes.
When the logic within `Visitor` becomes complex, implement separate Processor classes for main processing or Handler classes for error highlighting.

### Completion
Code completion functionality for DOMA directive syntax in SQL
- **[Feature Package](src/main/kotlin/org/domaframework/doma/intellij/contributor/sql)**
- CompletionContributor: Entry point for code completion
  - **Class Naming Rules**: Use `CompletionContributor` as a suffix
- Provider: Classes that provide completion candidates in specific contexts
  - **Class Naming Rules**: Use `CompletionProvider` as a suffix
- Processor: Classes that filter and transform completion candidates
  - **Class Naming Rules**: Use `Processor` as a suffix
- [Handler](src/main/kotlin/org/domaframework/doma/intellij/common/sql/directive): Classes that generate suggestion candidates for each directive type
  - **Class Naming Rules**: Use `Handler` as a suffix
- [Collector](src/main/kotlin/org/domaframework/doma/intellij/common/sql/directive/collector): Classes that collect directive suggestion candidates. Called from `Handler`
  - **Class Naming Rules**: Use `Collector` as a suffix

### Refactoring
Functionality for renaming SQL files and directories when DAO classes or methods are renamed.
- **Feature Package**: `src/main/kotlin/org/domaframework/doma/intellij/refactoring/dao`
- **Processor (Rename Processor Classes)**
  - **DaoMethodRenameProcessor**: Detects renaming of DAO method names and renames the corresponding SQL file.
  - **DaoPackageRenameListenerProcessor**: Detects renaming of DAO class package names and renames the corresponding SQL directory. Implemented as a subclass of `RefactoringElementListenerProvider` to support changes from the project view.
  - **DaoRenameProcessor**: Detects renaming of DAO class names and renames the corresponding SQL directory.
  - **Class Naming Rules**: Use the `Processor` suffix.
  - Use `canProcessElement()` to determine if the element is a rename target, and `renameElement()` to perform the rename. Since the original rename process is not executed, always call `super.renameElement()` at the end.

### Reference Resolution
Functionality for resolving references to the definition of elements inside Doma bind variables in SQL.
- **Feature Package**: `src/main/kotlin/org/domaframework/doma/intellij/reference`
- **Contributor**: Registers reference resolution extension points in IntelliJ.
  - **Class Naming Rules**: Use the `ReferenceContributor` suffix.
- **Provider**: Selects elements for reference resolution and delegates to the appropriate reference class.
  - **Class Naming Rules**: Use the `ReferenceProvider` suffix.
  - Bind variables may have parent-child relationships such as `SqlElFieldAccessExpr` > `SqlElIdExpr`. Resolution is performed from the smallest granularity upward for the most precise result.
- **Reference**: Performs the actual reference resolution. Implemented as subclasses of `SqlElExprReference` (which extends `PsiReferenceBase`).
  - **SqlElClassExprReference**: Resolves fully qualified class names in static field access elements.
  - **SqlElIdExprReference**: Resolves the smallest unit of bind variable elements (DAO parameters, fields, etc.).
  - **SqlElForDirectiveIdExprReference**: Resolves elements defined within loop directives.
  - **SqlElFunctionCallExprReference**: Resolves custom and built-in function references.
  - **SqlElStaticFieldReference**: Resolves the top-level static field or method in static field access.
  - **Class Naming Rules**: Use the `Reference` suffix.
- **Manipulator**: Supports renaming of reference target elements. Used to determine the reference range when the element text is changed.
  - **Class Naming Rules**: Use the `Manipulator` suffix.

### Documentation
Functionality for displaying documentation for the definition of elements inside Doma bind variables in SQL.
- **Feature Package**: `src/main/kotlin/org/domaframework/doma/intellij/document`
- **Provider**: Registers documentation display extension points in IntelliJ.
  - **Class Naming Rules**: Use the `DocumentationProvider` suffix.
- **Generator**: Generates HTML for documentation display.
  - **DocumentDaoParameterGenerator**: Generates documentation for bind variable elements starting from DAO parameters.
  - **DocumentStaticFieldAccessGenerator**: Generates documentation for bind variable elements starting from static field access.
  - **Class Naming Rules**: Use the `DocumentationGenerator` suffix.

### Formatter
Functionality for formatting SQL.
- **Feature Package**: `src/main/kotlin/org/domaframework/doma/intellij/formatter`
- **Builder**: Entry point for formatting, implements the `FormattingModelBuilder` class.
  - **SqlFormattingModelBuilder**: Provides formatting models for SQL.
  - **SqlBlockRelationBuilder**: Sets parent-child relationships between blocks.
  - **SqlBlockBuilder**: Manages the start and end blocks of conditional directives and group blocks.
  - **SqlCustomSpacingBuilder**: Defines custom spacing settings for element types and blocks.
  - **Class Naming Rules**: Use the `FormattingModelBuilder` suffix.
- **Block**: Classes representing the basic unit of formatting blocks.
  - The abstract class `SqlBlock` is the base, and blocks are structured hierarchically according to the SQL structure.
  - **SqlKeywordGroupBlock**: Represents keyword group blocks.
  - **SqlSubGroupBlock**: Represents subgroups enclosed in `()`.
  - **SqlElBlockCommentBlock**: Represents directive blocks such as `/*%if*/` and `/*%for*/`.
  - Whether to preserve a newline before a block is determined by `isSaveSpace()`.
  - The parent group block is held in `parentBlock`.
  - The block's own indent is calculated by `createBlockIndentLen()`, and the indent base for child elements in its group is calculated by `createGroupIndentLen()`.
  - **Class Naming Rules**: Use the `Block` suffix.
- **Util**: Utility classes related to formatting.
  - Provides block object generation by branching and line break determination by keyword combinations.
  - **Class Naming Rules**: Use the `Util` suffix.
- **Handler**: Provides custom spacing settings and block class determination according to query type.
- **Processor**: Classes that determine spacing between blocks.
  - **InjectionSqlFormatter**: Provides common formatting for injected SQL.
  - **SqlInjectionPostProcessor**: Applies formatting to injected SQL in DAOs. Independently invokes formatting and executes reformatting from the `SqlFormatPreProcessor` for SQL.
  - **SqlFormatPreProcessor**: Inserts line breaks before the main SQL formatting process. Not automatically executed for injected SQL formatting.
  - **SqlFormatPostProcessor**: Removes trailing spaces and adds a newline at the end of SQL after formatting.
  - **Class Naming Rules**: Use the `Processor` suffix.
- **Visitor**: Visitor classes for collecting target elements to process within Processor.
  - **Class Naming Rules**: Use the `Visitor` suffix
