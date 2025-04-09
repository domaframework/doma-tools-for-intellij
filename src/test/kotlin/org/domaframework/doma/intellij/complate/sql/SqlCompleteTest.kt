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
    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "SqlCompleteTestDao.java",
        )
        addSqlFile(
            "SqlCompleteTestDao/completeDaoArgument.sql",
            "SqlCompleteTestDao/completeInstancePropertyFromDaoArgumentClass.sql",
            "SqlCompleteTestDao/completeJavaPackageClass.sql",
            "SqlCompleteTestDao/completeDirective.sql",
            "SqlCompleteTestDao/completeStaticPropertyFromStaticPropertyCall.sql",
            "SqlCompleteTestDao/completePropertyAfterStaticPropertyCall.sql",
            "SqlCompleteTestDao/completeBuiltinFunction.sql",
            "SqlCompleteTestDao/completeDirectiveInsideIf.sql",
            "SqlCompleteTestDao/completeDirectiveInsideElseIf.sql",
            "SqlCompleteTestDao/completeDirectiveInsideFor.sql",
            "SqlCompleteTestDao/completeDirectiveFieldInsideIf.sql",
            "SqlCompleteTestDao/completeDirectiveFieldInsideElseIf.sql",
            "SqlCompleteTestDao/completeDirectiveFieldInsideFor.sql",
            "SqlCompleteTestDao/completeConcatenationOperator.sql",
            "SqlCompleteTestDao/completeComparisonOperator.sql",
            "SqlCompleteTestDao/completeParameterFirst.sql",
            "SqlCompleteTestDao/completeParameterFirstProperty.sql",
            "SqlCompleteTestDao/completeParameterSecond.sql",
            "SqlCompleteTestDao/completeParameterSecondProperty.sql",
        )
        myFixture.enableInspections(SqlBindVariableValidInspector())
    }

    fun testCompleteDaoArgument() {
        val sqlFile = findSqlFile("SqlCompleteTestDao/completeDaoArgument.sql")
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
            "SqlCompleteTestDao/completeInstancePropertyFromDaoArgumentClass.sql",
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
            "SqlCompleteTestDao/completeJavaPackageClass.sql",
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

    fun testCompleteDirective() {
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeDirective.sql",
            listOf(
                "elseif",
                "else",
                "end",
                "expand",
            ),
            listOf(
                "employee",
                "if",
                "populate",
                "for",
                "!",
            ),
        )
    }

    fun testCompleteStaticPropertyFromStaticPropertyCall() {
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeStaticPropertyFromStaticPropertyCall.sql",
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

    fun testCompletePropertyAfterStaticPropertyCall() {
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completePropertyAfterStaticPropertyCall.sql",
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
            "SqlCompleteTestDao/completeBuiltinFunction.sql",
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
            "SqlCompleteTestDao/completeDirectiveInsideIf.sql",
            listOf("employee"),
            listOf("project"),
        )
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeDirectiveInsideElseIf.sql",
            listOf("employee", "project"),
            emptyList(),
        )
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeDirectiveInsideFor.sql",
            listOf("project"),
            listOf("employee"),
        )
    }

    fun testCompleteDirectiveFieldInside() {
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeDirectiveFieldInsideIf.sql",
            listOf("startsWith()"),
            listOf("employee", "project", "toLowCase"),
        )
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeDirectiveFieldInsideElseIf.sql",
            listOf("department"),
            listOf("employee", "project"),
        )
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeDirectiveFieldInsideFor.sql",
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
            "SqlCompleteTestDao/completeConcatenationOperator.sql",
            listOf("rank"),
            listOf("employee", "employeeId", "department"),
        )

        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeComparisonOperator.sql",
            listOf("rank"),
            listOf("employee", "employeeId", "department"),
        )
    }

    fun testCompleteParameter() {
        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeParameterFirst.sql",
            listOf("employee"),
            listOf("employeeId", "department", "rank", "startWith"),
        )

        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeParameterFirstProperty.sql",
            listOf("employeeId", "department", "rank"),
            listOf("employee"),
        )

        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeParameterSecond.sql",
            listOf("employee"),
            listOf("employeeId", "department", "rank", "startWith"),
        )

        innerDirectiveCompleteTest(
            "SqlCompleteTestDao/completeParameterSecondProperty.sql",
            listOf("managerId"),
            listOf("employee", "department", "rank"),
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
