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
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlTestDataInspection

class TestDataCheckTest : DomaSqlTest() {
    private val testDaoName = "TestDataCheckDao"
    private val packageName = "inspection"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$packageName/$testDaoName.java",
        )
        addSqlFile(
            "$packageName/$testDaoName/literalDirective.sql",
            "$packageName/$testDaoName/bindVariableDirective.sql",
            "$packageName/$testDaoName/conditionAndLoopDirective.sql",
            "$packageName/$testDaoName/commentBlock.sql",
            "$packageName/$testDaoName/populateDirective.sql",
            "$packageName/$testDaoName/invalidTestData.sql",
            "$packageName/$testDaoName/expandDirective.sql",
            "$packageName/$testDaoName/invalidExpandDirective.sql",
        )
        myFixture.enableInspections(SqlTestDataInspection())
    }

    fun testLiteralDirective() {
        val sqlFile = findSqlFile("$packageName/$testDaoName/literalDirective.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableDirective() {
        val sqlFile = findSqlFile("$packageName/$testDaoName/bindVariableDirective.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testConditionAndLoopDirective() {
        val sqlFile = findSqlFile("$packageName/$testDaoName/conditionAndLoopDirective.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testCommentBlock() {
        val sqlFile =
            findSqlFile("$packageName/$testDaoName/commentBlock.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testPopulateDirective() {
        val sqlFile =
            findSqlFile("$packageName/$testDaoName/populateDirective.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testInvalidTestData() {
        val sqlFile =
            findSqlFile("$packageName/$testDaoName/invalidTestData.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testExpandDirective() {
        val sqlFile =
            findSqlFile("$packageName/$testDaoName/expandDirective.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testInvalidExpandDirective() {
        val sqlFile =
            findSqlFile("$packageName/$testDaoName/invalidExpandDirective.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }
}
