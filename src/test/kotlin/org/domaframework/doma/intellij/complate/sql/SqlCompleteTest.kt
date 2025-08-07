/*
 * Copyright Doma Tools Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.domaframework.doma.intellij.complate.sql

import org.domaframework.doma.intellij.DomaSqlTest

/**
 * Code completion testing in SQL
 */
class SqlCompleteTest : DomaSqlTest() {
    private val testDaoName = "SqlCompleteTestDao"
    private val testSpecificDaoName = "SpecificParamTypeCompletionDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$testDaoName.java",
        )
    }

    fun testCompleteDaoArgument() {
        addSqlFile("$testDaoName/completeDaoArgument.sql")
        val sqlFile = findSqlFile("$testDaoName/completeDaoArgument.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        val lookupElements = myFixture.completeBasic()
        val suggestions = lookupElements.map { it.lookupString }
        val expectedSuggestions = listOf("employee", "name")
        expectedSuggestions.forEach { expected ->
            assertTrue(
                "Expected '$expected' in completion suggestions: $suggestions",
                suggestions.contains(expected),
            )
        }
        assertTrue(
            "List sizes of [suggestions] and [expectedSuggestions] do not match",
            suggestions.size == expectedSuggestions.size,
        )
    }

    fun testCompleteInstancePropertyFromDaoArgumentClass() {
        addSqlFile(
            "$testDaoName/completeInstancePropertyFromDaoArgumentClass.sql",
            "$testDaoName/completeInstancePropertyWithMethodParameter.sql",
            "$testDaoName/completeFieldAccessBeforeOtherElement.sql",
            "$testDaoName/completeFieldAccessAfterOtherElement.sql",
            "$testDaoName/completeTopElementBeforeAtSign.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeInstancePropertyFromDaoArgumentClass.sql",
            listOf(
                "employeeId",
                "employeeName",
                "department",
                "rank",
                "projects",
                "getFirstProject()",
            ),
            listOf("getEmployeeRank()"),
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeInstancePropertyWithMethodParameter.sql",
            listOf(
                "byteValue()",
                "compareTo()",
                "doubleValue()",
                "toString()",
            ),
            listOf(
                "employeeId",
                "employeeName",
                "department",
                "rank",
                "getFirstProject()",
                "toLowerCase()",
                "charAt()",
                "contains()",
                "isBlank()",
            ),
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeTopElementBeforeAtSign.sql",
            listOf(
                "employee",
                "id",
                "userIds",
                "userId",
                "userId_has_next",
                "userId_index",
            ),
            listOf(
                "doma",
                "example",
                "projectNumber",
                "subProjects",
                "valueOf()",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeFieldAccessBeforeOtherElement.sql",
            listOf(
                "employeeId",
                "employeeName",
                "department",
                "rank",
                "projects",
                "getFirstProject()",
            ),
            listOf("projectNumber", "projectDetailId", "members", "getEmployeeRank()"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeFieldAccessAfterOtherElement.sql",
            listOf(
                "projectId",
                "projectName",
                "projectNumber",
                "projectCategory",
            ),
            listOf(
                "status",
                "rank",
                "getFirstEmployee()",
                "getTermNumber()",
                "employeeId",
                "employeeName",
                "department",
                "rank",
                "projects",
                "getFirstProject()",
            ),
        )
    }

    fun testCompleteJavaPackageClass() {
        addSqlFile("$testDaoName/completeJavaPackageClass.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeJavaPackageClass.sql",
            listOf(
                "CASE_INSENSITIVE_ORDER",
                "toString()",
                "getBytes()",
                "toLowerCase()",
                "trim()",
                "length()",
                "isEmpty()",
            ),
            listOf("value", "hash", "isLatin1()", "isASCII()"),
        )
    }

    fun testCompleteForItemHasNext() {
        addSqlFile("$testDaoName/completeForItemHasNext.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeForItemHasNext.sql",
            emptyList(),
            listOf(
                "get()",
                "startsWith()",
                "permissions",
                "MAX_VALUE",
                "MIN_VALUE",
                "FALSE",
                "TRUE",
                "TYPE",
                "toString()",
                "booleanValue()",
            ),
        )
    }

    fun testCompleteForItemIndex() {
        addSqlFile("$testDaoName/completeForItemIndex.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeForItemIndex.sql",
            emptyList(),
            listOf(
                "get()",
                "startsWith()",
                "permissions",
                "FALSE",
                "TRUE",
                "BYTES",
                "MAX_VALUE",
                "MIN_VALUE",
                "SIZE",
                "TYPE",
                "Integer()",
            ),
        )
    }

    fun testCompleteDirective() {
        addSqlFile("$testDaoName/completeDirective.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeDirective.sql",
            listOf(
                "elseif",
                "else",
            ),
            listOf(
                "end",
                "expand",
                "employee",
                "if",
                "populate",
                "for",
                "!",
            ),
        )
    }

    fun testCompleteBatchInsert() {
        addSqlFile("$testDaoName/completeBatchInsert.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeBatchInsert.sql",
            listOf(
                "getFirstProject()",
            ),
            listOf(
                "employeeName",
                "userId",
                "get()",
                "size()",
            ),
        )
    }

    fun testCompleteStaticPropertyFromStaticPropertyCall() {
        addSqlFile(
            "$testDaoName/completeStaticPropertyFromStaticPropertyCall.sql",
            "$testDaoName/completePropertyAfterStaticPropertyCall.sql",
            "$testDaoName/completePropertyAfterStaticPropertyCallWithMethodParameter.sql",
            "$testDaoName/completeStaticPropertyAfterOtherElement.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeStaticPropertyFromStaticPropertyCall.sql",
            listOf(
                "members",
                "projectNumber",
                "projectCategory",
                "getTermNumber()",
            ),
            listOf(
                "projectDetailId",
                "getCategoryName()",
                "addTermNumber()",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completePropertyAfterStaticPropertyCallWithMethodParameter.sql",
            listOf(
                "projectId",
                "projectNumber",
                "projectName",
                "projectCategory",
                "status",
                "manager",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
            listOf(
                "projectDetailId",
                "members",
                "employeeId",
                "projects",
                "contains()",
                "isBlank()",
            ),
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeStaticPropertyAfterOtherElement.sql",
            listOf(
                "projectNumber",
                "projectName",
                "projectCategory",
                "manager",
                "subProjects",
                "getTermNumber()",
                "getCustomNumber()",
            ),
            listOf(
                "doma",
                "employee",
            ),
        )
    }

    fun testCompleteCallStaticPropertyClassPackage() {
        addSqlFile(
            "$testDaoName/completeCallStaticPropertyClassPackage.sql",
            "$testDaoName/completeCallStaticPropertyClass.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeCallStaticPropertyClassPackage.sql",
            listOf(
                "doma",
                "com",
                "org",
            ),
            listOf(
                "resources",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeCallStaticPropertyClass.sql",
            listOf(
                "doma.example.entity.Employee",
                "doma.example.entity.EmployeeSummary",
                "doma.example.entity.Principal.Permission",
                "doma.example.entity.Principal",
                "doma.example.entity.Project",
                "doma.example.entity.ProjectDetail",
                "doma.example.entity.Employee.Rank",
                "doma.example.entity.User",
                "doma.example.entity.UserSummary",
            ),
            listOf(
                "resources",
                "com",
                "document",
                "org",
                "doma.example.dao.BatchDeleteTestDao",
                "doma.example.dao.DeleteTestDao",
            ),
        )
    }

    fun testCompletePropertyAfterStaticPropertyCall() {
        addSqlFile("$testDaoName/completePropertyAfterStaticPropertyCall.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completePropertyAfterStaticPropertyCall.sql",
            listOf(
                "managerId",
            ),
            listOf(
                "userId",
                "employeeName",
            ),
        )
    }

    fun testCompletePropertyAfterStaticMethodCall() {
        addSqlFile("$testDaoName/completePropertyAfterStaticMethodCall.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completePropertyAfterStaticMethodCall.sql",
            listOf(
                "getTermNumber()",
            ),
            listOf(
                "managerId",
                "userId",
                "employeeName",
            ),
        )
    }

    fun testCompleteBuiltinFunction() {
        addSqlFile("$testDaoName/completeBuiltinFunction.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeBuiltinFunction.sql",
            listOf(
                "isEmpty()",
                "isNotEmpty()",
                "isBlank()",
                "isNotBlank()",
            ),
            listOf(
                "userId()",
                "userName()",
                "userAge()",
                "langCode()",
                "isGest()",
                "getId()",
                "getName()",
                "getAge()",
                "getLangCode()",
                "isManager()",
            ),
        )
    }

    fun testCompleteDirectiveInside() {
        addSqlFile(
            "$testDaoName/completeDirectiveInsideIf.sql",
            "$testDaoName/completeDirectiveFieldInsideIfWithMethodParameter.sql",
            "$testDaoName/completeDirectiveInsideElseIf.sql",
            "$testDaoName/completeDirectiveInsideFor.sql",
            "$testDaoName/completeDirectiveInsideForWithMethodParameter.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveInsideIf.sql",
            listOf("employee"),
            listOf("project"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveFieldInsideIfWithMethodParameter.sql",
            listOf("projectId", "projectNumber", "projectCategory", "projectName"),
            listOf("project", "employee"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveInsideElseIf.sql",
            listOf("employee", "project"),
            emptyList(),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveInsideFor.sql",
            listOf("project"),
            listOf("employee", "member", "%for"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveInsideForWithMethodParameter.sql",
            listOf("clear()", "contains()", "get()", "getFirst()"),
            listOf("projectNumber", "projectCategory", "getFirstEmployee()", "getTermNumber()"),
        )
    }

    fun testCompleteDirectiveFieldInside() {
        addSqlFile(
            "$testDaoName/completeDirectiveFieldInsideIf.sql",
            "$testDaoName/completeDirectiveFieldInsideElseIf.sql",
            "$testDaoName/completeDirectiveFieldInsideFor.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveFieldInsideIf.sql",
            listOf("startsWith()"),
            listOf("employee", "project", "toLowCase"),
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveFieldInsideElseIf.sql",
            listOf("department"),
            listOf("employee", "project"),
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveFieldInsideFor.sql",
            listOf(
                "projectId",
                "projectName",
                "status",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
            listOf("project", "getCategoryName()"),
        )
    }

    fun testCompleteConcatenationOperator() {
        addSqlFile(
            "$testDaoName/completeConcatenationOperator.sql",
            "$testDaoName/completeComparisonOperator.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeConcatenationOperator.sql",
            listOf("rank"),
            listOf("employee", "employeeId", "department"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeComparisonOperator.sql",
            listOf("rank"),
            listOf("employee", "employeeId", "department"),
        )
    }

    fun testCompleteParameter() {
        addSqlFile(
            "$testDaoName/completeParameterFirst.sql",
            "$testDaoName/completeParameterFirstProperty.sql",
            "$testDaoName/completeParameterFirstPropertyWithMethodParameter.sql",
            "$testDaoName/completeParameterSecond.sql",
            "$testDaoName/completeParameterSecondProperty.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirst.sql",
            listOf("employee"),
            listOf("employeeId", "department", "rank", "startWith"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirstProperty.sql",
            listOf("employeeId", "department", "rank"),
            listOf("employee"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirstPropertyWithMethodParameter.sql",
            listOf("projectNumber", "projectCategory", "getFirstEmployee()"),
            listOf("employeeId", "employeeName", "getFirstProject()"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterSecond.sql",
            listOf("employee"),
            listOf("employeeId", "department", "rank", "startWith"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterSecondProperty.sql",
            listOf("employeeId", "employeeName", "department", "rank", "managerId"),
            listOf(
                "employee",
                "compareTo()",
                "doubleValue()",
            ),
        )
    }

    fun testCompleteParameterInStaticAccess() {
        addSqlFile(
            "$testDaoName/completeParameterFirstInStaticAccess.sql",
            "$testDaoName/completeParameterFirstPropertyInStaticAccess.sql",
            "$testDaoName/completeParameterSecondInStaticAccess.sql",
            "$testDaoName/completeParameterSecondPropertyInStaticAccess.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirstInStaticAccess.sql",
            listOf(
                "project",
            ),
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirstPropertyInStaticAccess.sql",
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
            listOf(
                "project",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterSecondInStaticAccess.sql",
            listOf("project"),
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterSecondPropertyInStaticAccess.sql",
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
            listOf("project"),
        )
    }

    fun testCompleteParameterInCustomFunctions() {
        addSqlFile(
            "$testDaoName/completeParameterFirstInCustomFunctions.sql",
            "$testDaoName/completeParameterFirstPropertyInCustomFunctions.sql",
            "$testDaoName/completeParameterSecondInCustomFunctions.sql",
            "$testDaoName/completeParameterSecondPropertyInCustomFunctions.sql",
        )
        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirstInCustomFunctions.sql",
            listOf(
                "project",
            ),
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterFirstPropertyInCustomFunctions.sql",
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
            listOf(
                "project",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterSecondInCustomFunctions.sql",
            listOf("project"),
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeParameterSecondPropertyInCustomFunctions.sql",
            listOf(
                "projectId",
                "projectName",
                "rank",
                "projectNumber",
                "projectCategory",
                "getFirstEmployee()",
                "getTermNumber()",
            ),
            listOf("project"),
        )
    }

    fun testCompleteOptionalDaoParam() {
        addSqlFile("$testDaoName/completeOptionalDaoParam.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalDaoParam.sql",
            listOf("manager", "projectNumber", "getFirstEmployee()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalStaticProperty() {
        addSqlFile("$testDaoName/completeOptionalStaticProperty.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalStaticProperty.sql",
            listOf("userId", "userName", "email", "getUserNameFormat()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalByForItem() {
        addSqlFile("$testDaoName/completeOptionalByForItem.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalByForItem.sql",
            listOf("manager", "projectNumber", "getFirstEmployee()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalBatchAnnotation() {
        addSqlFile("$testDaoName/completeOptionalBatchAnnotation.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalBatchAnnotation.sql",
            listOf("optionalIds"),
            listOf("get()", "orElseGet()", "isPresent()", "projectId"),
        )
    }

    fun testCompleteForDirectiveItem() {
        addSqlFile("$testDaoName/completeForDirectiveItem.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeForDirectiveItem.sql",
            listOf("projects", "project", "project_has_next", "project_index"),
            listOf("get()", "size()", "toString()", "projectId"),
        )
    }

    fun testCompleteImplementCustomFunction() {
        addResourceCompileFile("doma.compile.config")
        addSqlFile("$testDaoName/completeImplementCustomFunction.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeImplementCustomFunction.sql",
            listOf("userId", "userName", "userAge"),
            listOf(
                "getId()",
                "getName()",
                "getAge()",
                "getLangCode()",
                "isManager()",
                "langCode()",
                "isGest()",
                "isBlank()",
                "isNotBlank()",
            ),
        )
    }

    fun testCompleteNotImplementCustomFunction() {
        addResourceCompileFile("invalid.doma.compile.config")
        addSqlFile("$testDaoName/completeNotImplementCustomFunction.sql")
        innerDirectiveCompleteTest(
            "$testDaoName/completeNotImplementCustomFunction.sql",
            listOf(
                "isEmpty()",
                "isNotEmpty()",
                "isBlank()",
                "isNotBlank()",
            ),
            listOf(
                "userId()",
                "userName()",
                "userAge()",
                "langCode()",
                "isGest()",
                "getId()",
                "getName()",
                "getAge()",
                "getLangCode()",
                "isManager()",
            ),
        )
    }

    private fun innerDirectiveCompleteTest(
        sqlFileName: String,
        expectedSuggestions: List<String>,
        notExpectedSuggestions: List<String>,
    ) {
        val sqlFile = findSqlFile(sqlFileName)
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        val lookupElements = myFixture.completeBasic()
        assertNotNull("No suggestions found", lookupElements)

        val suggestions = lookupElements.map { it.lookupString }
        expectedSuggestions.forEach { expected ->
            assertTrue(
                "Expected '$expected' in completion suggestions: $suggestions",
                suggestions.contains(expected),
            )
        }
        notExpectedSuggestions.forEach { expected ->
            assertFalse(
                "Expected '$expected' in completion suggestions: $suggestions",
                suggestions.contains(expected),
            )
        }
    }
}
