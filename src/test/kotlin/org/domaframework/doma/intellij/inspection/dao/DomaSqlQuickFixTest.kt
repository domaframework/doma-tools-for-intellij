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
package org.domaframework.doma.intellij.inspection.dao

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.inspection.dao.inspector.SqlFileExistInspection

/**
 * Quick fix execution test
 */
class DomaSqlQuickFixTest : DomaSqlTest() {
    private val packagename = "quickfix"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$packagename/SelectQuickFixTestDao.java",
            "$packagename/InsertQuickFixTestDao.java",
            "$packagename/UpdateQuickFixTestDao.java",
            "$packagename/DeleteQuickFixTestDao.java",
            "$packagename/BatchInsertQuickFixTestDao.java",
            "$packagename/BatchUpdateQuickFixTestDao.java",
            "$packagename/BatchDeleteQuickFixTestDao.java",
            "$packagename/ScriptQuickFixTestDao.java",
            "$packagename/SqlProcessorQuickFixTestDao.java",
        )

        addResourceEmptySqlFile(
            "$packagename/SelectQuickFixTestDao/existsSQLFile.sql",
            "$packagename/InsertQuickFixTestDao/existsSQLFile.sql",
            "$packagename/UpdateQuickFixTestDao/existsSQLFile.sql",
            "$packagename/DeleteQuickFixTestDao/existsSQLFile.sql",
            "$packagename/BatchInsertQuickFixTestDao/existsSQLFile.sql",
            "$packagename/BatchUpdateQuickFixTestDao/existsSQLFile.sql",
            "$packagename/BatchDeleteQuickFixTestDao/existsSQLFile.sql",
            "$packagename/ScriptQuickFixTestDao/existsSQLFile.script",
            "$packagename/SqlProcessorQuickFixTestDao/existsSQLFile.sql",
        )
        myFixture.enableInspections(SqlFileExistInspection())
    }

    fun testSelectGenerateSQLFileQuickFix() {
        val testDaoName = "SelectQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) {
            highlightingDao(testDaoName)
        }
    }

    fun testInsertGenerateSQLFileQuickFix() {
        val testDaoName = "InsertQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    fun testUpdateGenerateSQLFileQuickFix() {
        val testDaoName = "UpdateQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    fun testDeleteGenerateSQLFileQuickFix() {
        val testDaoName = "DeleteQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    fun testBatchInsertGenerateSQLFileQuickFix() {
        val testDaoName = "BatchInsertQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    fun testBatchUpdateGenerateSQLFileQuickFix() {
        val testDaoName = "BatchUpdateQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    fun testBatchDeleteGenerateSQLFileQuickFix() {
        val testDaoName = "BatchDeleteQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    fun testScriptGenerateSQLFileQuickFix() {
        val testDaoName = "ScriptQuickFixTestDao"
        daoQuickFixTest(testDaoName, true) { highlightingDao(testDaoName) }
    }

    fun testSqlProcessorGenerateSQLFileQuickFix() {
        val testDaoName = "SqlProcessorQuickFixTestDao"
        daoQuickFixTest(testDaoName, false) { highlightingDao(testDaoName) }
    }

    private fun highlightingDao(testDaoName: String) {
        val dao = findDaoClass(getQuickFixTestDaoName(testDaoName).replace("/", "."))
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    private fun daoQuickFixTest(
        testDaoName: String,
        isScript: Boolean,
        afterCheck: () -> Unit,
    ) {
        val quickFixDaoName = getQuickFixTestDaoName(testDaoName)
        val dao = findDaoClass(quickFixDaoName.replace("/", "."))

        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)
        val intention = myFixture.findSingleIntention(MessageBundle.message("generate.sql.quickfix.title"))
        myFixture.launchAction(intention)

        val generatedSql =
            findSqlFile("$quickFixDaoName/generateSQLFile.${if (isScript) "script" else "sql"}")
        assertTrue("Not Found SQL File", generatedSql != null)

        afterCheck()
    }

    private fun getQuickFixTestDaoName(daoName: String): String = "$packagename/$daoName"
}
