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
import org.domaframework.doma.intellij.bundle.MessageBundle

class BulkConvertSqlAnnotationToFileActionTest : ConvertSqlActionTest() {
    private val sqlConversionPackage = "sqltofile/bulk"
    private val convertActionName = MessageBundle.message("bulk.convert.sql.annotation.to.file.text")
    private val convertFamilyName = MessageBundle.message("bulk.convert.sql.annotation.to.file.family")

    fun testBulkConvertAnnotationToSqlFile() {
        val daoName = "BulkConvertToSqlFileDao"
        val targetSqlFileNames =
            listOf(
                "selectEmployee.sql",
                "insertEmployee.sql",
                "updateEmployee.sql",
                "deleteEmployee.sql",
                "batchInsertEmployees.sql",
                "batchUpdateEmployees.sql",
                "batchDeleteEmployees.sql",
                "createTables.script",
                "processData.sql",
            )
        doTest(daoName, targetSqlFileNames)
    }

    private fun doTest(
        daoName: String,
        targetMethods: List<String>,
    ) {
        doConvertAction(
            daoName,
            convertFamilyName,
            sqlConversionPackage,
            convertActionName,
        )

        targetMethods.forEach { file ->
            val generatedSql = findSqlFile("$sqlConversionPackage/$daoName/$file")
            if (generatedSql == null) {
                fail("Not Found SQL File [$file]")
                return
            }
            val extension = generatedSql.extension == "script"
            val fileNameWithoutExtension = generatedSql.nameWithoutExtension
            doTestSqlFormat(daoName, fileNameWithoutExtension, sqlConversionPackage, extension)
        }

        // Test SQL File Generation
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        val openSqlFile = openedEditor.find { it.file.extension in listOf("sql", "script") }
        assertFalse(
            "Open File is SQL File [${openSqlFile?.file?.name}]",
            openSqlFile == null,
        )
    }
}
