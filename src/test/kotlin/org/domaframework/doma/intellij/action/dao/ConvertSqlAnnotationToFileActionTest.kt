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

class ConvertSqlAnnotationToFileActionTest : ConvertSqlActionTest() {
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
        doConvertActionTest(daoName, sqlConversionPackage, convertFamilyName)
    }

    fun testIntentionNotAvailableForUnsupportedAnnotation() {
        val daoName = "UnsupportedAnnotationDao"
        doConvertActionTest(daoName, sqlConversionPackage, convertFamilyName)
    }

    private fun doTest(
        daoName: String,
        sqlFileName: String,
        isScript: Boolean = false,
    ) {
        doConvertAction(
            daoName,
            convertFamilyName,
            sqlConversionPackage,
            convertActionName,
        )

        // Test SQL File Generation
        val sqlFile = "$sqlFileName.${if (isScript) "script" else "sql"}"
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertFalse(
            "Open File is $sqlFileName",
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
        doTestSqlFormat(
            daoName,
            sqlFileName,
            sqlConversionPackage,
            isScript,
        )
    }
}
