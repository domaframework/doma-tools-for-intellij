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
 * Code completion testing in SQL(Specific Param Type)
 */
class SqlSpecificParamTypeCompleteTest : DomaSqlTest() {
    private val testDaoName = "completion/SpecificParamTypeCompletionTestDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile("$testDaoName.java")
        addOtherJavaFile("collector", "HogeCollector.java")
        addOtherJavaFile("option", "HogeSelectOptions.java")
        addOtherJavaFile("function", "HogeFunction.java")
        addOtherJavaFile("function", "HogeBiFunction.java")
    }

    fun testSelectOptionArgument() {
        val sqlFileName = "selectSelectOption"
        val expectedSuggestions = listOf("id", "searchName")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testSubTypeSelectOptionArgument() {
        val sqlFileName = "selectSubTypeSelectOption"
        val expectedSuggestions = listOf("employee", "searchName", "hogeSelectOptions")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testCollectorArgument() {
        val sqlFileName = "selectCollector"
        val expectedSuggestions = listOf("salary")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testSubTypeCollectorArgument() {
        val sqlFileName = "selectSubTypeCollector"
        val expectedSuggestions = listOf("employee", "id")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testFunctionArgument() {
        val sqlFileName = "selectFunction"
        val expectedSuggestions = listOf("employee", "id")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testSubTypeFunctionArgument() {
        val sqlFileName = "selectSubTypeFunction"
        val expectedSuggestions = listOf("employee", "id")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testBiFunctionArgument() {
        val sqlFileName = "executeBiFunction"
        val expectedSuggestions = listOf("tableName")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    fun testSubTypeBiFunctionArgument() {
        val sqlFileName = "executeSubTypeBiFunction"
        val expectedSuggestions = listOf("tableName")
        innerDirectiveCompleteTest(sqlFileName, expectedSuggestions)
    }

    private fun innerDirectiveCompleteTest(
        sqlFileName: String,
        expectedSuggestions: List<String>,
    ) {
        addSqlFile("$testDaoName/$sqlFileName.sql")
        val sqlFile = findSqlFile("$testDaoName/$sqlFileName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        val lookupElements = myFixture.completeBasic()
        val suggestions = lookupElements.map { it.lookupString }
        println(suggestions.map { it })
        expectedSuggestions.forEach { expected ->
            assertTrue(
                "Expected '$expected' in completion suggestions: $suggestions",
                suggestions.contains(expected),
            )
        }
        assertTrue(
            "List sizes of [suggestions(${suggestions.size})] and [expectedSuggestions(${expectedSuggestions.size})] do not match",
            suggestions.size == expectedSuggestions.size,
        )
    }
}
