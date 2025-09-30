# Test Case Implementation Guidelines
Follow the rules below when implementing test code.

## Common Test Case Implementation
- For individual test data, call `addSqlFile()` from `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt` within each test case method separately. This prevents unrelated test data from being registered in other test cases.
- For DAO class test data, use `addDaoJavaFile()` from `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt`.
- For Entity test data, use `addEntityJavaFile()` from `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt`.
- The default source directory for the virtual test project is `src/java/doma/example`. If you need to register files under a different package name, use `addOtherPackageJavaFile()` or `addOtherPackageSqlFile()`.
- To retrieve information about SQL files registered in the virtual test project, use `findSqlFile()` from `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt`.
- To retrieve information about DAO classes registered in the virtual test project, use `findDaoClass()` from `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt`.
- If you need to register classes other than DAO or Entity classes, such as subclasses of ExpressionFunction or domain classes, use `addOtherJavaFile()` from `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt`.

## Code Inspection Feature Test Case Implementation
### Description
- Implement tests for code inspection functionality using the following steps.

### Test Case Implementation
- Implement test case classes in `@src/test/kotlin/org/domaframework/doma/intellij/inspection`.
- Implement test cases for each subclass of **AbstractBaseJavaLocalInspectionTool**.
- Name the test case class as `<InspectionToolName>InspectionTest`.
- The test case class should extend `@src/test/kotlin/org/domaframework/doma/intellij/DomaSqlTest.kt`.
- Override **setUp()** to add mock **Entity** classes or other necessary components to the virtual project for testing.
- Register the target **AbstractBaseJavaLocalInspectionTool** subclass using **myFixture.enableInspections**.
- In test case functions, retrieve the target Dao class using **DomaSqlTest#findDaoClass()** and test the highlighting results using **myFixture.testHighlighting()**.
- Create the target Dao class or SQL file for inspection.
- Wrap the elements to be error-highlighted with the **<error>** tag and specify the error message to be displayed using the **descr** option.

### Test Data
- Implement test data Dao classes in **Java**.
- Annotate test data Dao classes with **@Dao**.
- Place them under the appropriate subpackage in `@src/test/testData/src/main/java/doma/example/dao/inspection`.
- Implement any other necessary classes as needed.
- When creating test cases, prepare and verify the following Entity classes to test parent-child relationships:
  - Parent class annotated with @Entity
  - Subclass without @Entity annotation
  - Subclass annotated with @Entity
- Using the above, create the following test cases for features that use `PsiClass`:
  - Test cases using fields/methods defined in parent classes
  - Test cases using fields/methods defined in subclasses
- Examples:
  - Test that bind variable definition inspection doesn't highlight errors when using fields defined in parent classes
  - Test that include/exclude option checking in DAO method annotations doesn't highlight errors when using fields defined in parent classes
  - Test that code completion shows fields/methods defined in parent classes as completion candidates
- Create and place SQL files in a directory named after the corresponding Dao class under `@src/test/testData/src/main/resources/META-INF/doma/example/dao`.
- When creating test cases for bind variables, prepare test data that checks instance field methods, static field methods, field methods of elements defined in loop directives, custom functions, and built-in functions. For cases combining multiple variables, ensure comprehensive coverage of all variable combination patterns.

## Action Feature Test Case Implementation
### Description
- Action feature test cases should be implemented as subclasses of `DomaSqlTest`.

### Test Case Implementation
- Implement test case methods that verify both the visibility of the action in the menu and the state of the system after the action is executed.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data. Specify the action invocation location by placing a `<caret>` marker in the file where the action will be executed.

## Intention Action Feature Test Case Implementation
### Description
- Intention action test cases should be implemented as subclasses of `DomaSqlTest`.
- Implement test case methods that verify both the visibility/non-visibility of the intention action menu and the state after executing the action.
- For menu display tests, check for the existence of a menu item whose family name matches the expected value in the list of actions displayed in the menu.
- After executing the action with `myFixture.launchAction()`, use `myFixture.checkResultByFile()` to compare the file contents after the action with the expected result file.
- For the "Convert SQL file to @Sql annotation" action, test that it can be invoked both from the DAO method and from the SQL file.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data.
- Place a `<caret>` marker at the location where you want to invoke the action.
- As with formatter tests, prepare an expected result file to compare the file contents after the action is executed.

## Line Marker Feature Test Case Implementation
### Description
- Line marker test cases should be implemented as subclasses of `DomaSqlTest`.

### Test Case Implementation
- Implement test case methods that verify the visibility of gutter icons, the number of gutter icons displayed for a file, and the behavior when a gutter icon is clicked.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data.
- For verifying the behavior when a gutter icon is clicked, use the first displayed gutter icon found in the test as the target for the action.

## Code Completion Feature Test Case Implementation
### Description
- Code completion test cases should be implemented as subclasses of `DomaSqlTest`.

### Test Case Implementation
- Implement test case methods that check the contents of the suggestion list (completion suggestions).
- Verify that expected words appear in the suggestions and that unexpected words do not appear.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data.
- Place a `<caret>` marker at the position where code completion should be invoked, and evaluate both the list of expected suggestions and the list of suggestions that should not be shown.

## Reference Resolution Feature Test Case Implementation
### Description
- Reference resolution test cases should be implemented as subclasses of `DomaSqlTest`.

### Test Case Implementation
- Implement test case methods that verify the combination of the source class and the element text for elements inside bind variables.
- For method reference resolution, also check the combination of argument types to ensure that the correct resolution is performed.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data.
- Map the list of target element texts for reference resolution to the information of the source class (and argument types for methods) obtained by resolution, and verify that the actual reference resolution results match the expected mapping.
- Since the same element text may resolve to multiple definitions, the mapping between element text and source class information should be one-to-many.
- For literals or elements that do not have a reference, set the expected value to null.

## Documentation Feature Test Case Implementation
### Description
- Documentation test cases should be implemented as subclasses of `DomaSqlTest`.

### Test Case Implementation
- Implement test case methods that prepare the expected documentation information in HTML format for elements inside bind variables, and compare it with the actual displayed text.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data.
- Place a `<caret>` marker at the position of the element for which you want to reference the documentation information.
- In the expected HTML text, describe the class path using `<a href>` tags. If the element has a type, describe the tags for each nested type. After the tag representing the type information, write the target element text.

## Refactoring Feature Test Case Implementation
### Description
- Refactoring test cases should be implemented as subclasses of `DomaSqlTest`.
- The refactoring feature includes renaming DAO method names and DAO class names. However, since creating test data for class renaming is difficult, only test cases for DAO method renaming are implemented.
- Prepare both the expected data before and after renaming, and implement test case methods that compare the state before and after renaming. Also, verify that the SQL file before renaming does not remain after the operation.

### Test Case Implementation
- Place a `<caret>` marker at the method name you want to rename, and after executing the rename, check that the method name and the associated SQL file name have been renamed accordingly.

### Test Data
- Prepare the necessary DAO classes and SQL files as test data.

## Formatter Feature Test Case Implementation
### Description
- Formatter test cases should be implemented as subclasses of `BasePlatformTestCase`.
- The test compares the test data with the expected value data to verify that the formatting process produces the expected result.
- The execution of the formatting and the comparison with the expected value data are implemented by `formatSqlFile()` in #file:SqlFormatTest.kt.

### Test Case Implementation
- Prepare both a pre-format file and an expected value file for each test case.
- The expected value file should be named `[test data file name]_format.sql` so that the test data and expected value data are displayed together in the project tab.

### Test Data
- Test data files do not need to be registered in the virtual project. Place them under `src/test/testData/sql/formatter`.

## Parser Feature Test Case Implementation
### Description
- Parser test cases should be implemented as subclasses of `ParsingTestCase`.
- Prepare the expected PSI tree structure data, and ensure that the test case method name matches the corresponding file name.
- By calling `doTest()`, the standard parser test provided by the framework will be executed.

### Test Case Implementation
- Obtain the expected PSI tree data by copying the tree information displayed in IntelliJ's debug environment via "Tools > View PSI Structure".
- To capture all differences, including spaces, make sure to check "Show PsiWhiteSpace" before copying the tree information as text.

## Reference
- For actual implementation examples using Doma, refer to the [Doma GitHub repository](https://github.com/domaframework/doma/tree/master/integration-test-java/src/main/java/org/seasar/doma/it/dao).
