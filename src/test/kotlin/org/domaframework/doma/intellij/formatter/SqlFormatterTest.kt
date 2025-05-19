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
package org.domaframework.doma.intellij.formatter

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ThrowableRunnable
import org.domaframework.doma.intellij.common.helper.ActiveProjectHelper
import org.domaframework.doma.intellij.setting.SettingComponent
import org.domaframework.doma.intellij.setting.state.DomaToolsFormatEnableSettings

class SqlFormatterTest : BasePlatformTestCase() {
    override fun getBasePath(): String? = "src/test/testData/sql/formatter"

    override fun getTestDataPath(): String? = "src/test/testData/sql/formatter"

    override fun setUp() {
        super.setUp()
        settingSqlFormat(true)
        if (project != null) {
            ActiveProjectHelper.setCurrentActiveProject(project)
        }
    }

    private fun settingSqlFormat(enabled: Boolean) {
        val settings = DomaToolsFormatEnableSettings.getInstance(project)
        val component = SettingComponent()
        component.enableFormat = enabled
        settings.apply(component)
        assertEquals(enabled, settings.getState().isEnableSqlFormat)
    }

    override fun tearDown() {
        try {
            settingSqlFormat(false)
            ActiveProjectHelper.setCurrentActiveProject(null)
        } finally {
            super.tearDown()
        }
    }

    fun testSelectFormatter() {
        formatSqlFile("Select.sql", "FormattedSelect.sql")
    }

    fun testCreateTableFormatter() {
        formatSqlFile("CreateTable.sql", "FormattedCreateTable.sql")
    }

    fun testCreateViewFormatter() {
        formatSqlFile("CreateView.sql", "FormattedCreateView.sql")
    }

    fun testInsertFormatter() {
        formatSqlFile("Insert.sql", "FormattedInsert.sql")
    }

    fun testInsertWithBindVariableFormatter() {
        formatSqlFile("InsertWithBindVariable.sql", "FormattedInsertWithBindVariable.sql")
    }

    fun testUpdateFormatter() {
        formatSqlFile("Update.sql", "FormattedUpdate.sql")
    }

    fun testUpdateBindVariableFormatter() {
        formatSqlFile("UpdateBindVariable.sql", "FormattedUpdateBindVariable.sql")
    }

    fun testUpdateTupleAssignmentFormatter() {
        formatSqlFile("UpdateTupleAssignment.sql", "FormattedUpdateTupleAssignment.sql")
    }

    fun testDeleteFormatter() {
        formatSqlFile("Delete.sql", "FormattedDelete.sql")
    }

    private fun formatSqlFile(
        beforeFile: String,
        afterFile: String,
    ) {
        myFixture.configureByFiles(beforeFile)
        val currentFile = myFixture.file
        WriteCommandAction
            .writeCommandAction(project)
            .run<RuntimeException?>(
                ThrowableRunnable {
                    CodeStyleManager.getInstance(project).reformatText(
                        currentFile,
                        arrayListOf(currentFile.textRange),
                    )
                },
            )
        myFixture.checkResultByFile(afterFile)
    }
}
