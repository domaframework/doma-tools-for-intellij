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
import org.domaframework.doma.intellij.state.DomaToolsFunctionEnableSettings

class SqlFormatterTest : BasePlatformTestCase() {
    override fun getBasePath(): String? = "src/test/testData/sql/formatter"

    override fun getTestDataPath(): String? = "src/test/testData/sql/formatter"

    override fun setUp() {
        super.setUp()
        val settings = DomaToolsFunctionEnableSettings.getInstance()
        settings.state.isEnableSqlFormat = true
    }

    override fun tearDown() {
        try {
            val settings = DomaToolsFunctionEnableSettings.getInstance()
            settings.state.isEnableSqlFormat = false
        } finally {
            super.tearDown()
        }
    }

    fun testSelectFormatter() {
        formatSqlFime("Select.sql", "FormattedSelect.sql")
    }

    fun testCreateTableFormatter() {
        formatSqlFime("CreateTable.sql", "FormattedCreateTable.sql")
    }

    fun testCreateViewFormatter() {
        formatSqlFime("CreateView.sql", "FormattedCreateView.sql")
    }

    fun testInsertFormatter() {
        formatSqlFime("Insert.sql", "FormattedInsert.sql")
    }

    fun testDeleteFormatter() {
        formatSqlFime("Delete.sql", "FormattedDelete.sql")
    }

    private fun formatSqlFime(
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
