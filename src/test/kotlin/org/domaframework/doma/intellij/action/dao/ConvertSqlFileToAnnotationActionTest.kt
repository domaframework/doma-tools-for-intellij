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
import com.intellij.psi.PsiDocumentManager
import org.domaframework.doma.intellij.action.sql.ConvertSqlFileToAnnotationFromSqlAction
import org.domaframework.doma.intellij.bundle.MessageBundle

class ConvertSqlFileToAnnotationActionTest : ConvertSqlActionTest() {
    private val sqlToAnnotationPackage = "sqltoannotation"
    private val convertActionName = MessageBundle.message("convert.sql.file.to.annotation.from.sql.text")
    private val convertFamilyName = MessageBundle.message("convert.sql.file.to.annotation.from.sql.family")

    fun testIntentionAvailableForSelectWithSqlFile() {
        val daoName = "SelectWithSqlFileDao"
        val sqlFileName = "selectEmployee"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForInsertWithSqlFile() {
        val daoName = "InsertWithSqlFileDao"
        val sqlFileName = "insertEmployee"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForUpdateWithSqlFile() {
        val daoName = "UpdateWithSqlFileDao"
        val sqlFileName = "updateEmployee"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForDeleteWithSqlFile() {
        val daoName = "DeleteWithSqlFileDao"
        val sqlFileName = "deleteEmployee"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForScriptWithSqlFile() {
        val daoName = "ScriptWithSqlFileDao"
        val sqlFileName = "createTables"
        doTest(daoName, sqlFileName, isScript = true)
    }

    fun testIntentionAvailableForBatchInsertWithSqlFile() {
        val daoName = "BatchInsertWithSqlFileDao"
        val sqlFileName = "batchInsertEmployees"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForBatchUpdateWithSqlFile() {
        val daoName = "BatchUpdateWithSqlFileDao"
        val sqlFileName = "batchUpdateEmployees"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForBatchDeleteWithSqlFile() {
        val daoName = "BatchDeleteWithSqlFileDao"
        val sqlFileName = "batchDeleteEmployees"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionAvailableForSqlProcessorWithSqlFile() {
        val daoName = "SqlProcessorWithSqlFileDao"
        val sqlFileName = "processData"
        doTest(daoName, sqlFileName)
    }

    fun testIntentionNotAvailableForMethodWithSqlAnnotation() {
        val daoName = "MethodWithSqlAnnotationDao"
        addDaoJavaFile("$sqlToAnnotationPackage/$daoName.java")
        val daoClass = findDaoClass("$sqlToAnnotationPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)

        val intentions = myFixture.availableIntentions
        val convertIntention = intentions.find { it is ConvertSqlFileToAnnotationFromSqlAction }

        assertNull(
            "$convertFamilyName intention should NOT be available when @Sql annotation already exists",
            convertIntention,
        )
    }

    fun testIntentionNotAvailableForMethodWithoutSqlFile() {
        val daoName = "MethodWithoutSqlFileDao"
        addDaoJavaFile("$sqlToAnnotationPackage/$daoName.java")
        val daoClass = findDaoClass("$sqlToAnnotationPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)

        val intentions = myFixture.availableIntentions
        val convertIntention = intentions.find { it is ConvertSqlFileToAnnotationFromSqlAction }

        assertNull(
            "$convertFamilyName intention should NOT be available when SQL file doesn't exist",
            convertIntention,
        )
    }

    fun testSqlFormattingInAnnotation() {
        val daoName = "SelectWithComplexSqlFileDao"
        val sqlFileName = "selectComplexQuery"
        doTest(daoName, sqlFileName)
    }

    fun testSelectWithSqlFileConvertAnnotation() {
        val daoName = "SelectWithSqlFileConvertAnnotationDao"
        val sqlFileName = "selectEmployee.sql"
        addSqlFile("$sqlToAnnotationPackage/$daoName/$sqlFileName")
        doAvailableConvertActionTest(
            daoName,
            sqlToAnnotationPackage,
            convertFamilyName,
        )

        // Test SQL File Removed
        val generatedSql = findSqlFile("$sqlToAnnotationPackage/$daoName/$sqlFileName")
        assertNull("SQL File [$sqlFileName] should exists ", generatedSql)
    }

    private fun doTest(
        daoName: String,
        sqlFileName: String,
        isScript: Boolean = false,
    ) {
        addDaoJavaFile("$sqlToAnnotationPackage/$daoName.java")
        val extension = if (isScript) "script" else "sql"
        addSqlFile("$sqlToAnnotationPackage/$daoName/$sqlFileName.$extension")

        val sqlFile = findSqlFile("$sqlToAnnotationPackage/$daoName/$sqlFileName.$extension")
        if (sqlFile == null) {
            fail("SQL file $sqlFileName.$extension should exist in $sqlToAnnotationPackage/$daoName")
            return
        }

        myFixture.configureFromExistingVirtualFile(sqlFile)
        val intention = myFixture.findSingleIntention(convertActionName)

        assertNotNull(
            "$convertActionName intention should be available",
            intention,
        )
        assertEquals(convertActionName, intention.text)
        assertEquals(convertFamilyName, intention.familyName)

        myFixture.launchAction(intention)

        val docMgr = PsiDocumentManager.getInstance(project)
        val fdm = FileDocumentManager.getInstance()
        fdm.saveAllDocuments()
        docMgr.commitAllDocuments()

        val daoClass = findDaoClass("$sqlToAnnotationPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)
        myFixture.checkResultByFile("java/doma/example/dao/$sqlToAnnotationPackage/$daoName.after.java")

        val sqlFileAfter = findSqlFile("$sqlToAnnotationPackage/$daoName/$sqlFileName.$extension")
        assertNull("SQL file should be deleted after conversion", sqlFileAfter)
    }
}
