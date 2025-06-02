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
package org.domaframework.doma.intellij.refactor

import org.domaframework.doma.intellij.DomaSqlTest

/**
 * Refactoring test when changing DAO class name and method name
 */
class DaoMethodRenameTestCase : DomaSqlTest() {
    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "RenameDao.java",
            "RenameDaoMethod.java",
            "RenameDaoMethodWithoutSql.java",
            "RenameDaoMethodNotExistSql.java",
        )
        addResourceEmptySqlFile(
            "RenameDaoMethod/renameDaoMethodName.sql",
            "RenameDao/renameDaoClassName.sql",
        )
    }

    fun testRenameDaoClassName() {
        val dao = findDaoClass("RenameDao")
        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)
        myFixture.renameElementAtCaret("RenameDaoAfter")
        myFixture.checkResultByFile("/java/$packagePath/dao/RenameDaoAfter.java", false)

        val afterSqlFile = findSqlFile("RenameDaoAfter/renameDaoClassName.sql")
        val beforeSqlFile = findSqlFile("RenameDao/renameDaoClassName.sql")

        assertTrue("No Found SQL File", afterSqlFile != null)
        assertFalse("Exist SQL File", beforeSqlFile != null)
    }

    fun testRenameDaoMethod() {
        val dao = findDaoClass("RenameDaoMethod")
        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)

        myFixture.renameElementAtCaret("renameDaoMethodNameAfter")
        myFixture.checkResultByFile("/java/$packagePath/dao/RenameDaoMethodAfter.java", false)

        val afterSqlFile = findSqlFile("RenameDaoMethod/renameDaoMethodNameAfter.sql")
        val beforeSqlFile = findSqlFile("RenameDaoMethod/renameDaoMethodName.sql")

        assertTrue("No Found SQL File", afterSqlFile != null)
        assertFalse("Exist SQL File", beforeSqlFile != null)
    }

    fun testNonRequireSQLFileRenameMethodName() {
        val dao = findDaoClass("RenameDaoMethodWithoutSql")
        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)

        myFixture.renameElementAtCaret("notExistSqlAfter")
        myFixture.checkResultByFile(
            "/java/$packagePath/dao/RenameDaoMethodWithoutSqlAfter.java",
            false,
        )
    }

    fun testNonExistsSQLFileRenameMethodName() {
        val dao = findDaoClass("RenameDaoMethodNotExistSql")
        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)

        myFixture.renameElementAtCaret("renameDaoMethodNameAfter")
        myFixture.checkResultByFile(
            "/java/$packagePath/dao/RenameDaoMethodNotExistSqlAfter.java",
            false,
        )
    }
}
