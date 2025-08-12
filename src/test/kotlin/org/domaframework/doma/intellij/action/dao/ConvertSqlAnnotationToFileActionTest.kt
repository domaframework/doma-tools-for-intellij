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

import com.intellij.openapi.fileEditor.FileEditorManager
import org.domaframework.doma.intellij.DomaSqlTest

class ConvertSqlAnnotationToFileActionTest : DomaSqlTest() {
    private val sqlConversionPackage = "sqlconversion"
    private val convertActionName = "Convert to SQL file (set sqlFile=true)"
    private val convertFamilyName = "Convert @Sql annotation to SQL file"

    fun testIntentionAvailableForSelectWithSqlAnnotation() {
        val daoName = "SelectWithSqlAnnotationDao"
        doTest(daoName, "generateSqlFile")
    }

    fun testIntentionAvailableForSelectTextBlockWithSqlAnnotation() {
        val daoName = "SelectTextBlockWithSqlAnnotationDao"
        doTest(daoName, "generateSqlFileByTextBlock")
    }

    fun testIntentionAvailableForSelectHasAnyOptWithSqlAnnotation() {
        val daoName = "SelectHasAnyOptionWithSqlAnnotationDao"
        doTest(daoName, "generateSqlFileHasAnyOption")
    }

    fun testIntentionAvailableForInsertWithSqlAnnotation() {
        val daoName = "InsertWithSqlAnnotationDao"
        doTest(daoName, "insert")
    }

    fun testIntentionAvailableForUpdateWithSqlAnnotation() {
        val daoName = "UpdateReturningWithSqlAnnotationDao"
        doTest(daoName, "updateEmployeeReturning")
    }

    fun testIntentionAvailableForDeleteWithSqlAnnotation() {
        val daoName = "DeleteWithSqlAnnotationDao"
        doTest(daoName, "deleteEmployeeHasSqlFile")
    }

    fun testIntentionAvailableForScriptWithSqlAnnotation() {
        val daoName = "ScriptWithSqlAnnotationDao"
        doTest(daoName, "createTable", true)
    }

    fun testIntentionAvailableForBatchInsertWithSqlAnnotation() {
        val daoName = "BatchInsertWithSqlAnnotationDao"
        doTest(daoName, "batchInsert")
    }

    fun testIntentionAvailableForBatchUpdateWithSqlAnnotation() {
        val daoName = "BatchUpdateWithSqlAnnotationDao"
        doTest(daoName, "batchUpdate")
    }

    fun testIntentionAvailableForBatchDeleteWithSqlAnnotation() {
        val daoName = "BatchDeleteWithSqlAnnotationDao"
        doTest(daoName, "batchDelete")
    }

    fun testIntentionAvailableForSqlProcessorWithSqlAnnotation() {
        val daoName = "SqlProcessorWithSqlAnnotationDao"
        doTest(daoName, "executeProcessor")
    }

    fun testIntentionOverrideSqlFile() {
        val daoName = "SelectOverrideSqlFileDao"
        doTest(daoName, "overrideSqlFile")

        val sqlFileName = "overrideSqlFile"
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        val sqlFile = openedEditor.find { it.file.name == sqlFileName.substringAfter("/").plus(".sql") }

        if (sqlFile == null) {
            fail("SQL file $sqlFileName should be opened after conversion")
            return
        }

        myFixture.configureFromExistingVirtualFile(sqlFile.file)
        myFixture.checkResultByFile("resources/META-INF/doma/example/dao/$sqlConversionPackage/$daoName/$sqlFileName.after.sql")
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
}
