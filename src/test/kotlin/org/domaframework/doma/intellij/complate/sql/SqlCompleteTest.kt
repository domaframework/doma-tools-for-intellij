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
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlBindVariableValidInspector

/**
 * Code completion testing in SQL
 */
class SqlCompleteTest : DomaSqlTest() {
    private val testDapName = "SqlCompleteTestDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$testDapName.java",
        )
        addSqlFile(
            "$testDapName/completeDaoArgument.sql",
            "$testDapName/completeInstancePropertyFromDaoArgumentClass.sql",
            "$testDapName/completeJavaPackageClass.sql",
            "$testDapName/completeDirective.sql",
            "$testDapName/completeBatchInsert.sql",
            "$testDapName/completeStaticPropertyFromStaticPropertyCall.sql",
            "$testDapName/completePropertyAfterStaticPropertyCall.sql",
            "$testDapName/completeBuiltinFunction.sql",
            "$testDapName/completeDirectiveInsideIf.sql",
            "$testDapName/completeDirectiveInsideElseIf.sql",
            "$testDapName/completeDirectiveInsideFor.sql",
            "$testDapName/completeDirectiveFieldInsideIf.sql",
            "$testDapName/completeDirectiveFieldInsideElseIf.sql",
            "$testDapName/completeDirectiveFieldInsideFor.sql",
            "$testDapName/completeConcatenationOperator.sql",
            "$testDapName/completeComparisonOperator.sql",
            "$testDapName/completeParameterFirst.sql",
            "$testDapName/completeParameterFirstProperty.sql",
            "$testDapName/completeParameterSecond.sql",
            "$testDapName/completeParameterSecondProperty.sql",
            "$testDapName/completeCallStaticPropertyClassPackage.sql",
            "$testDapName/completeCallStaticPropertyClass.sql",
            "$testDapName/completeForItemHasNext.sql",
            "$testDapName/completeForItemIndex.sql",
            "$testDapName/completeOptionalDaoParam.sql",
            "$testDapName/completeOptionalStaticProperty.sql",
            "$testDapName/completeOptionalByForItem.sql",
            "$testDapName/completeOptionalBatchAnnotation.sql",
            "$testDapName/completeForDirectiveItem.sql",
        )
        myFixture.enableInspections(SqlBindVariableValidInspector())
    }

    fun testCompleteDaoArgument() {
        val sqlFile = findSqlFile("$testDapName/completeDaoArgument.sql")
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
            "$testDapName/completeInstancePropertyFromDaoArgumentClass.sql",
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
    }

    fun testCompleteJavaPackageClass() {
        innerDirectiveCompleteTest(
            "$testDapName/completeJavaPackageClass.sql",
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
            "$testDapName/completeForItemHasNext.sql",
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
            "$testDapName/completeForItemIndex.sql",
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
            "$testDapName/completeDirective.sql",
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
            "$testDapName/completeBatchInsert.sql",
            listOf(
                "employeeId",
                "employeeName",
            ),
            listOf(
                "userId",
                "get()",
                "size()",
            ),
        )
    }

    fun testCompleteStaticPropertyFromStaticPropertyCall() {
        innerDirectiveCompleteTest(
            "$testDapName/completeStaticPropertyFromStaticPropertyCall.sql",
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
    }

    fun testCompleteCallStaticPropertyClassPackage() {
        innerDirectiveCompleteTest(
            "$testDapName/completeCallStaticPropertyClassPackage.sql",
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
            "$testDapName/completeCallStaticPropertyClass.sql",
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
            "$testDapName/completePropertyAfterStaticPropertyCall.sql",
            listOf(
                "managerId",
            ),
            listOf(
                "userId",
                "employeeName",
            ),
        )
    }

    fun testCompleteBuiltinFunction() {
        innerDirectiveCompleteTest(
            "$testDapName/completeBuiltinFunction.sql",
            listOf(
                "isEmpty()",
                "isNotEmpty()",
                "isBlank()",
                "isNotBlank()",
            ),
            listOf(
                "escape()",
                "prefix()",
                "infix()",
                "suffix()",
                "roundDownTimePart()",
                "roundUpTimePart()",
            ),
        )
    }

    fun testCompleteDirectiveInside() {
        innerDirectiveCompleteTest(
            "$testDapName/completeDirectiveInsideIf.sql",
            listOf("employee"),
            listOf("project"),
        )
        innerDirectiveCompleteTest(
            "$testDapName/completeDirectiveInsideElseIf.sql",
            listOf("employee", "project"),
            emptyList(),
        )
        innerDirectiveCompleteTest(
            "$testDapName/completeDirectiveInsideFor.sql",
            listOf("project"),
            listOf("employee"),
        )
    }

    fun testCompleteDirectiveFieldInside() {
        innerDirectiveCompleteTest(
            "$testDapName/completeDirectiveFieldInsideIf.sql",
            listOf("startsWith()"),
            listOf("employee", "project", "toLowCase"),
        )
        innerDirectiveCompleteTest(
            "$testDapName/completeDirectiveFieldInsideElseIf.sql",
            listOf("department"),
            listOf("employee", "project"),
        )
        innerDirectiveCompleteTest(
            "$testDapName/completeDirectiveFieldInsideFor.sql",
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
            "$testDapName/completeConcatenationOperator.sql",
            listOf("rank"),
            listOf("employee", "employeeId", "department"),
        )

        innerDirectiveCompleteTest(
            "$testDapName/completeComparisonOperator.sql",
            listOf("rank"),
            listOf("employee", "employeeId", "department"),
        )
    }

    fun testCompleteParameter() {
        innerDirectiveCompleteTest(
            "$testDapName/completeParameterFirst.sql",
            listOf("employee"),
            listOf("employeeId", "department", "rank", "startWith"),
        )

        innerDirectiveCompleteTest(
            "$testDapName/completeParameterFirstProperty.sql",
            listOf("employeeId", "department", "rank"),
            listOf("employee"),
        )

        innerDirectiveCompleteTest(
            "$testDapName/completeParameterSecond.sql",
            listOf("employee"),
            listOf("employeeId", "department", "rank", "startWith"),
        )

        innerDirectiveCompleteTest(
            "$testDapName/completeParameterSecondProperty.sql",
            listOf("managerId"),
            listOf("employee", "department", "rank"),
        )
    }

    fun testCompleteOptionalDaoParam() {
        innerDirectiveCompleteTest(
            "$testDapName/completeOptionalDaoParam.sql",
            listOf("manager", "projectNumber", "getFirstEmployee()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalStaticProperty() {
        innerDirectiveCompleteTest(
            "$testDapName/completeOptionalStaticProperty.sql",
            listOf("userId", "userName", "email", "getUserNameFormat()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalByForItem() {
        innerDirectiveCompleteTest(
            "$testDapName/completeOptionalByForItem.sql",
            listOf("manager", "projectNumber", "getFirstEmployee()"),
            listOf("get()", "orElseGet()", "isPresent()"),
        )
    }

    fun testCompleteOptionalBatchAnnotation() {
        innerDirectiveCompleteTest(
            "$testDapName/completeOptionalBatchAnnotation.sql",
            listOf("optionalIds"),
            listOf("get()", "orElseGet()", "isPresent()", "projectId"),
        )
    }

    fun testCompleteForDirectiveItem() {
        innerDirectiveCompleteTest(
            "$testDapName/completeForDirectiveItem.sql",
            listOf("projects", "project", "project_has_next", "project_index"),
            listOf("get()", "size()", "toString()", "projectId"),
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
