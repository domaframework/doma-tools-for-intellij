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

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$testDaoName.java",
        )
        addSqlFile(
            "$testDaoName/completeDaoArgument.sql",
            "$testDaoName/completeInstancePropertyFromDaoArgumentClass.sql",
            "$testDaoName/completeInstancePropertyWithMethodParameter.sql",
            "$testDaoName/completeTopElementBeforeAtsign.sql",
            "$testDaoName/completeJavaPackageClass.sql",
            "$testDaoName/completeDirective.sql",
            "$testDaoName/completeBatchInsert.sql",
            "$testDaoName/completeStaticPropertyFromStaticPropertyCall.sql",
            "$testDaoName/completePropertyAfterStaticPropertyCall.sql",
            "$testDaoName/completePropertyAfterStaticPropertyCallWithMethodParameter.sql",
            "$testDaoName/completePropertyAfterStaticMethodCall.sql",
            "$testDaoName/completeStaticPropertyAfterOtherElement.sql",
            "$testDaoName/completeBuiltinFunction.sql",
            "$testDaoName/completeDirectiveInsideIf.sql",
            "$testDaoName/completeDirectiveFieldInsideIfWithMethodParameter.sql",
            "$testDaoName/completeDirectiveInsideElseIf.sql",
            "$testDaoName/completeDirectiveInsideFor.sql",
            "$testDaoName/completeDirectiveInsideForWithMethodParameter.sql",
            "$testDaoName/completeDirectiveFieldInsideIf.sql",
            "$testDaoName/completeDirectiveFieldInsideElseIf.sql",
            "$testDaoName/completeDirectiveFieldInsideFor.sql",
            "$testDaoName/completeConcatenationOperator.sql",
            "$testDaoName/completeComparisonOperator.sql",
            "$testDaoName/completeParameterFirst.sql",
            "$testDaoName/completeParameterFirstProperty.sql",
            "$testDaoName/completeParameterFirstPropertyWithMethodParameter.sql",
            "$testDaoName/completeParameterSecond.sql",
            "$testDaoName/completeParameterSecondProperty.sql",
            "$testDaoName/completeParameterFirstInStaticAccess.sql",
            "$testDaoName/completeParameterFirstPropertyInStaticAccess.sql",
            "$testDaoName/completeParameterSecondInStaticAccess.sql",
            "$testDaoName/completeParameterSecondPropertyInStaticAccess.sql",
            "$testDaoName/completeParameterFirstInCustomFunctions.sql",
            "$testDaoName/completeParameterFirstPropertyInCustomFunctions.sql",
            "$testDaoName/completeParameterSecondInCustomFunctions.sql",
            "$testDaoName/completeParameterSecondPropertyInCustomFunctions.sql",
            "$testDaoName/completeCallStaticPropertyClassPackage.sql",
            "$testDaoName/completeCallStaticPropertyClass.sql",
            "$testDaoName/completeForItemHasNext.sql",
            "$testDaoName/completeForItemIndex.sql",
            "$testDaoName/completeOptionalDaoParam.sql",
            "$testDaoName/completeOptionalStaticProperty.sql",
            "$testDaoName/completeOptionalByForItem.sql",
            "$testDaoName/completeOptionalBatchAnnotation.sql",
            "$testDaoName/completeForDirectiveItem.sql",
            "$testDaoName/completeImplementCustomFunction.sql",
            "$testDaoName/completeNotImplementCustomFunction.sql",
        )
    }

    fun testCompleteDaoArgument() {
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
            "$testDaoName/completeTopElementBeforeAtsign.sql",
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
    }

    fun testCompleteJavaPackageClass() {
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
        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveInsideIf.sql",
            listOf("employee"),
            listOf("project"),
        )

        innerDirectiveCompleteTest(
            "$testDaoName/completeDirectiveFieldInsideIfWithMethodParameter.sql",
            listOf("projectNumber", "projectCategory", "getFirstEmployee()", "getTermNumber()"),
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
            listOf("managerId"),
            listOf("employee", "department", "rank"),
        )
    }

    fun testCompleteParameterInStaticAccess() {
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
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalDaoParam.sql",
            listOf("manager", "projectNumber", "getFirstEmployee()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalStaticProperty() {
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalStaticProperty.sql",
            listOf("userId", "userName", "email", "getUserNameFormat()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalByForItem() {
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalByForItem.sql",
            listOf("manager", "projectNumber", "getFirstEmployee()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalBatchAnnotation() {
        innerDirectiveCompleteTest(
            "$testDaoName/completeOptionalBatchAnnotation.sql",
            listOf("optionalIds"),
            listOf("get()", "orElseGet()", "isPresent()", "projectId"),
        )
    }

    fun testCompleteForDirectiveItem() {
        innerDirectiveCompleteTest(
            "$testDaoName/completeForDirectiveItem.sql",
            listOf("projects", "project", "project_has_next", "project_index"),
            listOf("get()", "size()", "toString()", "projectId"),
        )
    }

    fun testCompleteImplementCustomFunction() {
        addResourceCompileFile("doma.compile.config")
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
