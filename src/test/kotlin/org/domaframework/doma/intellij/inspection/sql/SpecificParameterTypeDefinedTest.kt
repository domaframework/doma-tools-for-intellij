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
package org.domaframework.doma.intellij.inspection.sql

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlBindVariableInspection
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlFunctionCallInspection
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlLoopDirectiveTypeInspection

/**
 * Test cases for error highlighting when specific parameter types are used within SQL files.
 *
 * This test class verifies that the inspection correctly identifies and highlights errors
 * when method parameters of specific types (such as Optional, List, Entity classes, etc.)
 * are referenced in SQL bind variables and directives.
 */
class SpecificParameterTypeDefinedTest : DomaSqlTest() {
    private val testDaoName = "inspection/option/SpecificParamTypeBindVariableInspectionTestDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile("$testDaoName.java")
        addOtherJavaFile("function", "HogeFunction.java")
        addOtherJavaFile("function", "HogeBiFunction.java")
        addOtherJavaFile("option", "HogeSelectOptions.java")
        addOtherJavaFile("collector", "HogeCollector.java")
        myFixture.enableInspections(
            SqlBindVariableInspection(),
            SqlLoopDirectiveTypeInspection(),
            SqlFunctionCallInspection(),
        )
    }

    fun testSelectOptions() {
        val sqlFileName = "$testDaoName/selectSelectOption.sql"
        inspectionSql(sqlFileName)
    }

    fun testSubTypeSelectOption() {
        val sqlFileName = "$testDaoName/selectSubTypeSelectOption.sql"
        inspectionSql(sqlFileName)
    }

    fun testCollector() {
        val sqlFileName = "$testDaoName/selectCollector.sql"
        inspectionSql(sqlFileName)
    }

    fun testSubTypeCollector() {
        val sqlFileName = "$testDaoName/selectSubTypeCollector.sql"
        inspectionSql(sqlFileName)
    }

    fun testFunction() {
        val sqlFileName = "$testDaoName/selectFunction.sql"
        inspectionSql(sqlFileName)
    }

    fun testSubTypeFunction() {
        val sqlFileName = "$testDaoName/selectSubTypeFunction.sql"
        inspectionSql(sqlFileName)
    }

    fun testBiFunction() {
        val sqlFileName = "$testDaoName/executeBiFunction.sql"
        inspectionSql(sqlFileName)
    }

    fun testSubTypeBiFunction() {
        val sqlFileName = "$testDaoName/executeSubTypeBiFunction.sql"
        inspectionSql(sqlFileName)
    }

    private fun inspectionSql(sqlFileName: String) {
        addSqlFile(sqlFileName)
        val sqlFile = findSqlFile(sqlFileName)
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }
}
