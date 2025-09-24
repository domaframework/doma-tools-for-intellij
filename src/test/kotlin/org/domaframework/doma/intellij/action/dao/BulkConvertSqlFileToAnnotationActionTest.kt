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

import org.domaframework.doma.intellij.bundle.MessageBundle

class BulkConvertSqlFileToAnnotationActionTest : ConvertSqlActionTest() {
    private val sqlConversionPackage = "sqltoannotation/bulk"
    private val convertFamilyName = MessageBundle.message("bulk.convert.sql.file.to.annotation.family")

    fun testBulkConvertToSqlAnnotation() {
        val daoName = "BulkConvertToSqlAnnotationDao"
        val targetSqlFileNames =
            listOf(
                "generateSqlFile.sql",
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
        targetSqlFileNames: List<String>,
    ) {
        targetSqlFileNames.forEach { file ->
            addSqlFile( "$sqlConversionPackage/$daoName/$file")
        }
        doAvailableConvertActionTest(
            daoName,
            sqlConversionPackage,
            convertFamilyName,
        )
        // Test SQL File Removed
        targetSqlFileNames.forEach { file ->
            val generatedSql = findSqlFile("$sqlConversionPackage/$daoName/$file")
            assertNull("SQL File [$file] should exists ", generatedSql)
        }
    }
}
