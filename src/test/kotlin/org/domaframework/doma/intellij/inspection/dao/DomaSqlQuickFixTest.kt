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

import com.intellij.openapi.vfs.VirtualFile
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

        addOtherPackageJavaFile("doma/java/dao", "SourceNameDao.java")

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
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testInsertGenerateSQLFileQuickFix() {
        val testDaoName = "InsertQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testUpdateGenerateSQLFileQuickFix() {
        val testDaoName = "UpdateQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testDeleteGenerateSQLFileQuickFix() {
        val testDaoName = "DeleteQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testBatchInsertGenerateSQLFileQuickFix() {
        val testDaoName = "BatchInsertQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testBatchUpdateGenerateSQLFileQuickFix() {
        val testDaoName = "BatchUpdateQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testBatchDeleteGenerateSQLFileQuickFix() {
        val testDaoName = "BatchDeleteQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testScriptGenerateSQLFileQuickFix() {
        val testDaoName = "ScriptQuickFixTestDao"
        daoQuickFixTest(testDaoName, true) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testSqlProcessorGenerateSQLFileQuickFix() {
        val testDaoName = "SqlProcessorQuickFixTestDao"
        daoQuickFixTest(testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    fun testSourceNameDaoGenerateSQLFileQuickFix() {
        val originalPackageName = "doma/java"
        val testDaoName = "SourceNameDao"
        daoQuickFixTestOtherPackage(originalPackageName, testDaoName) { virtual ->
            highlightingDao(virtual)
        }
    }

    private fun highlightingDao(virtual: VirtualFile) {
        myFixture.testHighlighting(
            false,
            false,
            false,
            virtual,
        )
    }

    private fun daoQuickFixTest(
        testDaoName: String,
        isScript: Boolean = false,
        afterCheck: (VirtualFile) -> Unit,
    ) {
        val quickFixDaoName = getQuickFixTestDaoName(testDaoName)
        val dao = findDaoClass(quickFixDaoName)

        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)
        val intention =
            myFixture.findSingleIntention(MessageBundle.message("generate.sql.quickfix.title"))
        myFixture.launchAction(intention)

        val extension = if (isScript) "script" else "sql"
        findSqlFile("$quickFixDaoName/generateSQLFile.$extension")?.let { generatedSql ->
            afterCheck(generatedSql)
        }
    }

    private fun daoQuickFixTestOtherPackage(
        packageName: String,
        testDaoName: String,
        isScript: Boolean = false,
        afterCheck: (VirtualFile) -> Unit,
    ) {
        val daoPackage = packageName.plus("/dao")
        val dao = findDaoClass(daoPackage.replace("/", "."), testDaoName)

        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)
        val intention =
            myFixture.findSingleIntention(MessageBundle.message("generate.sql.quickfix.title"))
        myFixture.launchAction(intention)

        val extension = if (isScript) "script" else "sql"
        findSqlFile(packageName, "$testDaoName/generateSQLFile.$extension")?.let { generatedSql ->
            afterCheck(generatedSql)
        } ?: fail("Not Found SQL File: $packageName/$testDaoName/generateSQLFile.$extension")
    }

    private fun getQuickFixTestDaoName(daoName: String): String = "$packagename/$daoName"
}
