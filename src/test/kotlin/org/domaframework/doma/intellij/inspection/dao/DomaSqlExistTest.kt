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
import org.domaframework.doma.intellij.inspection.dao.inspector.SqlFileExistInspection

class DomaSqlExistTest : DomaSqlTest() {
    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "SelectTestDao.java",
            "InsertTestDao.java",
            "UpdateTestDao.java",
            "DeleteTestDao.java",
            "BatchInsertTestDao.java",
            "BatchUpdateTestDao.java",
            "BatchDeleteTestDao.java",
            "ScriptTestDao.java",
            "SqlProcessorTestDao.java",
        )
        addResourceEmptySqlFile(
            "SelectTestDao/existsSQLFile.sql",
            "InsertTestDao/existsSQLFile.sql",
            "UpdateTestDao/existsSQLFile.sql",
            "DeleteTestDao/existsSQLFile.sql",
            "BatchInsertTestDao/existsSQLFile.sql",
            "BatchUpdateTestDao/existsSQLFile.sql",
            "BatchDeleteTestDao/existsSQLFile.sql",
            "ScriptTestDao/existsSQLFile.script",
            "SqlProcessorTestDao/existsSQLFile.sql",
        )
        myFixture.enableInspections(SqlFileExistInspection())
    }

    fun testSelectExistsSQLFile() {
        val dao = findDaoClass("SelectTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testInsertExistsSQLFile() {
        val dao = findDaoClass("InsertTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testUpdateExistsSQLFile() {
        val dao = findDaoClass("UpdateTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testDeleteExistsSQLFile() {
        val dao = findDaoClass("DeleteTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testBatchInsertExistsSQLFile() {
        val dao = findDaoClass("BatchInsertTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testBatchUpdateExistsSQLFile() {
        val dao = findDaoClass("BatchUpdateTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testBatchDeleteExistsSQLFile() {
        val dao = findDaoClass("BatchDeleteTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testSqlProcessorExistsSQLFile() {
        val dao = findDaoClass("SqlProcessorTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }

    fun testScriptExistsSQLFile() {
        val dao = findDaoClass("ScriptTestDao")
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }
}
