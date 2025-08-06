# Test Case Implementation Guidelines
Follow the rules below when implementing test code.

## Code Inspection Functionality
Implement tests for code inspection functionality using the following steps:

### **Implementation of Test Case Classes**
- Implement test case classes in `@src/test/kotlin/org/domaframework/doma/intellij/inspection`.
- Implement test cases for each subclass of **AbstractBaseJavaLocalInspectionTool**.
- Name the test case class as `<InspectionToolName>InspectionTest`.
- The test case class should extend `DomaSqlTest`.
- Override **setUp()** to add mock **Entity** classes or other necessary components to the virtual project for testing.
- Register the target **AbstractBaseJavaLocalInspectionTool** subclass using **myFixture.enableInspections**.
- In test case functions, retrieve the target Dao class using **DomaSqlTest#findDaoClass()** and test the highlighting results using **myFixture.testHighlighting()**.

### Implementation of Test Cases
Create the target Dao class or SQL file for inspection.
Wrap the elements to be error-highlighted with the **<error>** tag and specify the error message to be displayed using the **descr** option.

#### Test Data for Dao Classes
- Implement test data Dao classes in **Java**.
- Annotate test data Dao classes with **@Dao**.
- Place them under the appropriate subpackage in `@src/test/testData/src/main/java/doma/example/dao/inspection`.
- Implement any other necessary classes as needed.

#### Test Data for SQL Files
- Create and place SQL files in a directory named after the corresponding Dao class under `@src/test/testData/src/main/resources/META-INF/doma/example/dao`.

### Reference
For actual implementation examples using Doma, refer to the [Doma GitHub repository](https://github.com/domaframework/doma/tree/master/integration-test-java/src/main/java/org/seasar/doma/it/dao).
