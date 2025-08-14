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
package org.domaframework.doma.intellij.action.dao

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import org.domaframework.doma.intellij.DomaSqlTest

class ConvertSqlAnnotationToFileActionTest : DomaSqlTest() {
    private val sqlConversionPackage = "sqltofile"
    private val convertActionName = "Convert to SQL file (set sqlFile=true)"
    private val convertFamilyName = "Convert @Sql annotation to SQL file"

    fun testIntentionAvailableForSelectWithSqlAnnotation() {
        val daoName = "SelectWithSqlAnnotationDao"
        val sqlFileName = "generateSqlFile"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForSelectTextBlockWithSqlAnnotation() {
        val daoName = "SelectTextBlockWithSqlAnnotationDao"
        val sqlFileName = "generateSqlFileByTextBlock"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForSelectHasAnyOptWithSqlAnnotation() {
        val daoName = "SelectHasAnyOptionWithSqlAnnotationDao"
        val sqlFileName = "generateSqlFileHasAnyOption"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForInsertWithSqlAnnotation() {
        val daoName = "InsertWithSqlAnnotationDao"
        val sqlFileName = "insert"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForUpdateWithSqlAnnotation() {
        val daoName = "UpdateReturningWithSqlAnnotationDao"
        val sqlFileName = "updateEmployeeReturning"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForDeleteWithSqlAnnotation() {
        val daoName = "DeleteWithSqlAnnotationDao"
        val sqlFileName = "deleteEmployeeHasSqlFile"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForScriptWithSqlAnnotation() {
        val daoName = "ScriptWithSqlAnnotationDao"
        val sqlFileName = "createTable"
        doTest(daoName, sqlFileName, true)
        doTestSqlFormat(daoName, sqlFileName, true)
    }

    fun testIntentionAvailableForBatchInsertWithSqlAnnotation() {
        val daoName = "BatchInsertWithSqlAnnotationDao"
        val sqlFileName = "batchInsert"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForBatchUpdateWithSqlAnnotation() {
        val daoName = "BatchUpdateWithSqlAnnotationDao"
        val sqlFileName = "batchUpdate"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForBatchDeleteWithSqlAnnotation() {
        val daoName = "BatchDeleteWithSqlAnnotationDao"
        val sqlFileName = "batchDelete"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionAvailableForSqlProcessorWithSqlAnnotation() {
        val daoName = "SqlProcessorWithSqlAnnotationDao"
        val sqlFileName = "executeProcessor"
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionOverrideSqlFile() {
        val daoName = "SelectOverrideSqlFileDao"
        val sqlFileName = "overrideSqlFile"
        addResourceEmptySqlFile("$sqlConversionPackage/$daoName/$sqlFileName.sql")
        doTest(daoName, sqlFileName)
        doTestSqlFormat(daoName, sqlFileName)
    }

    fun testIntentionNotAvailableForMethodWithoutSqlAnnotation() {
        val daoName = "NoSqlAnnotationDao"
        addDaoJavaFile("$sqlConversionPackage/$daoName.java")
        val daoClass = findDaoClass("$sqlConversionPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)

        val intentions = myFixture.availableIntentions
        val convertIntention = intentions.find { it is ConvertSqlAnnotationToFileAction }

        assertNull("$convertFamilyName intention should NOT be available without @Sql annotation", convertIntention)
    }

    fun testIntentionNotAvailableForUnsupportedAnnotation() {
        val daoName = "UnsupportedAnnotationDao"
        addDaoJavaFile("$sqlConversionPackage/$daoName.java")
        val daoClass = findDaoClass("$sqlConversionPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)

        val intentions = myFixture.availableIntentions
        val convertIntention = intentions.find { it is ConvertSqlAnnotationToFileAction }

        assertNull("$convertFamilyName intention should NOT be available without @Sql annotation", convertIntention)
    }

    private fun doTest(
        daoName: String,
        sqlFileName: String,
        isScript: Boolean = false,
    ) {
        addDaoJavaFile("$sqlConversionPackage/$daoName.java")

        val daoClass = findDaoClass("$sqlConversionPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)
        val intention = myFixture.findSingleIntention(convertActionName)

        assertNotNull(
            "$convertActionName intention should be available",
            intention,
        )
        assertEquals(convertActionName, intention.text)
        assertEquals(convertFamilyName, intention.familyName)

        myFixture.launchAction(intention)
        myFixture.checkResultByFile("java/doma/example/dao/$sqlConversionPackage/$daoName.after.java")

        // Test SQL File Generation
        val sqlFile = "$sqlFileName.${if (isScript) "script" else "sql"}"
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertTrue(
            "Open File is Not $sqlFileName",
            openedEditor.any { it.file.name == sqlFile.substringAfter("/") },
        )

        val generatedSql = findSqlFile("$sqlConversionPackage/$daoName/$sqlFile")
        assertTrue("Not Found SQL File [$sqlFile]", generatedSql != null)
    }

    private fun doTestSqlFormat(
        daoName: String,
        sqlFileName: String,
        isScript: Boolean = false,
    ) {
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        val extension = if (isScript) "script" else "sql"
        val sqlFile = openedEditor.find { it.file.name == sqlFileName.substringAfter("/").plus(".$extension") }

        if (sqlFile == null) {
            fail("SQL file $sqlFileName.$extension should be opened after conversion")
            return
        }
        // If the generated `PsiFile` has an associated `Document`, explicitly reload it to ensure memory–disk consistency.
        // If not reloaded, the test may produce: *Unexpected memory–disk conflict in tests for*.
        val fdm = FileDocumentManager.getInstance()
        fdm.saveAllDocuments()
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        fdm.getDocument(sqlFile.file)?.let { fdm.reloadFromDisk(it) }

        myFixture.configureFromExistingVirtualFile(sqlFile.file)
        myFixture.checkResultByFile("resources/META-INF/doma/example/dao/$sqlConversionPackage/$daoName/$sqlFileName.after.$extension")
    }
}
