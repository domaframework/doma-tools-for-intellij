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
import org.domaframework.doma.intellij.setting.SettingComponent
import org.domaframework.doma.intellij.setting.state.DomaToolsFormatEnableSettings

class SqlFormatterTest : BasePlatformTestCase() {
    override fun getBasePath(): String? = "src/test/testData/sql/formatter"

    override fun getTestDataPath(): String? = "src/test/testData/sql/formatter"

    private val formatDataPrefix = "_format"

    override fun setUp() {
        super.setUp()
        settingSqlFormat(true)
    }

    private fun settingSqlFormat(enabled: Boolean) {
        val settings = DomaToolsFormatEnableSettings.getInstance()
        val component = SettingComponent()
        component.enableFormat = enabled
        settings.apply(component)
        assertEquals(enabled, settings.getState().isEnableSqlFormat)
    }

    override fun tearDown() {
        try {
            settingSqlFormat(false)
        } finally {
            super.tearDown()
        }
    }

    fun testSelectFormatter() {
        formatSqlFile("Select.sql", "Select$formatDataPrefix.sql")
    }

    fun testSelectEscapeFunctionNameFormatter() {
        formatSqlFile("SelectEscapeFunctionName.sql", "SelectEscapeFunctionName$formatDataPrefix.sql")
    }

    fun testSelectCaseEndFormatter() {
        formatSqlFile("SelectCaseEnd.sql", "SelectCaseEnd$formatDataPrefix.sql")
    }

    fun testSelectFromLateralFormatter() {
        formatSqlFile("SelectFromLateral.sql", "SelectFromLateral$formatDataPrefix.sql")
    }

    fun testSelectFromLateralSecondFormatter() {
        formatSqlFile("SelectFromLateralSecond.sql", "SelectFromLateralSecond$formatDataPrefix.sql")
    }

    fun testSelectJoinLateralFormatter() {
        formatSqlFile("SelectJoinLateral.sql", "SelectJoinLateral$formatDataPrefix.sql")
    }

    fun testSelectFromValuesFormatter() {
        formatSqlFile("SelectFromValues.sql", "SelectFromValues$formatDataPrefix.sql")
    }

    fun testSelectFromValuesUserDirectiveFormatter() {
        formatSqlFile("SelectFromValuesUserDirective.sql", "SelectFromValuesUserDirective$formatDataPrefix.sql")
    }

    fun testCreateTableFormatter() {
        formatSqlFile("CreateTable.sql", "CreateTable$formatDataPrefix.sql")
    }

    fun testCreateViewFormatter() {
        formatSqlFile("CreateView.sql", "CreateView$formatDataPrefix.sql")
    }

    fun testInsertFormatter() {
        formatSqlFile("Insert.sql", "Insert$formatDataPrefix.sql")
    }

    fun testInsertReturningFormatter() {
        formatSqlFile("InsertReturning.sql", "InsertReturning$formatDataPrefix.sql")
    }

    fun testInsertWithBindVariableFormatter() {
        formatSqlFile("InsertWithBindVariable.sql", "InsertWithBindVariable$formatDataPrefix.sql")
    }

    fun testUpdateFormatter() {
        formatSqlFile("Update.sql", "Update$formatDataPrefix.sql")
    }

    fun testUpdateReturningFormatter() {
        formatSqlFile("UpdateReturning.sql", "UpdateReturning$formatDataPrefix.sql")
    }

    fun testUpdateBindVariableFormatter() {
        formatSqlFile("UpdateBindVariable.sql", "UpdateBindVariable$formatDataPrefix.sql")
    }

    fun testUpdateBulkAssignmentFormatter() {
        formatSqlFile("UpdateBulkAssignment.sql", "UpdateBulkAssignment$formatDataPrefix.sql")
    }

    fun testDeleteFormatter() {
        formatSqlFile("Delete.sql", "Delete$formatDataPrefix.sql")
    }

    fun testDeleteReturningFormatter() {
        formatSqlFile("DeleteReturning.sql", "DeleteReturning$formatDataPrefix.sql")
    }

    fun testInsertConflictUpdateFormatter() {
        formatSqlFile("InsertConflictUpdate.sql", "InsertConflictUpdate$formatDataPrefix.sql")
    }

    fun testInsertConflictNothingFormatter() {
        formatSqlFile("InsertConflictNothing.sql", "InsertConflictNothing$formatDataPrefix.sql")
    }

    fun testInsertConflictUpdateWithOutTableFormatter() {
        formatSqlFile("InsertConflictUpdateWithOutTable.sql", "InsertConflictUpdateWithOutTable$formatDataPrefix.sql")
    }

    fun testWithSelect() {
        formatSqlFile("WithSelect.sql", "WithSelect$formatDataPrefix.sql")
    }

    fun testWithMultiQuery() {
        formatSqlFile("WithMultiQuery.sql", "WithMultiQuery$formatDataPrefix.sql")
    }

    fun testWithRecursive() {
        formatSqlFile("WithRecursive.sql", "WithRecursive$formatDataPrefix.sql")
    }

    fun testWithUnionAll() {
        formatSqlFile("WithUnionAll.sql", "WithUnionAll$formatDataPrefix.sql")
    }

    fun testWithInsert() {
        formatSqlFile("WithInsert.sql", "WithInsert$formatDataPrefix.sql")
    }

    fun testWithUpdate() {
        formatSqlFile("WithUpdate.sql", "WithUpdate$formatDataPrefix.sql")
    }

    fun testWithDelete() {
        formatSqlFile("WithDelete.sql", "WithDelete$formatDataPrefix.sql")
    }

    fun testNestedDirectivesFormatter() {
        formatSqlFile("NestedDirectives.sql", "NestedDirectives$formatDataPrefix.sql")
    }

    fun testSelectCaseEndWithConditionFormatter() {
        formatSqlFile("SelectCaseEndWithCondition.sql", "SelectCaseEndWithCondition$formatDataPrefix.sql")
    }

    fun testSelectDirectiveTestDataFormatter() {
        formatSqlFile("SelectDirectiveTestData.sql", "SelectDirectiveTestData$formatDataPrefix.sql")
    }

    fun testBasicBindVariablesFormatter() {
        formatSqlFile("BasicBindVariables.sql", "BasicBindVariables$formatDataPrefix.sql")
    }

    fun testConditionalDirectiveFormatter() {
        formatSqlFile("ConditionalDirective.sql", "ConditionalDirective$formatDataPrefix.sql")
    }

    fun testEmbeddedVariableFormatter() {
        formatSqlFile("EmbeddedVariable.sql", "EmbeddedVariable$formatDataPrefix.sql")
    }

    fun testExpandVariableFormatter() {
        formatSqlFile("ExpandVariable.sql", "ExpandVariable$formatDataPrefix.sql")
    }

    fun testInClauseBindVariableFormatter() {
        formatSqlFile("InClauseBindVariable.sql", "InClauseBindVariable$formatDataPrefix.sql")
    }

    fun testLiteralVariableFormatter() {
        formatSqlFile("LiteralVariable.sql", "LiteralVariable$formatDataPrefix.sql")
    }

    fun testLoopDirectiveFormatter() {
        formatSqlFile("LoopDirective.sql", "LoopDirective$formatDataPrefix.sql")
    }

    fun testNestForDirectiveFormatter() {
        formatSqlFile("NestForDirective.sql", "NestForDirective$formatDataPrefix.sql")
    }

    fun testPopulateVariableFormatter() {
        formatSqlFile("PopulateVariable.sql", "PopulateVariable$formatDataPrefix.sql")
    }

    fun testStaticFieldAccessFormatter() {
        formatSqlFile("StaticFieldAccess.sql", "StaticFieldAccess$formatDataPrefix.sql")
    }

    fun testUseDirectiveWithQueryFormatter() {
        formatSqlFile("UseDirectiveWithQuery.sql", "UseDirectiveWithQuery$formatDataPrefix.sql")
    }

    fun testUserDirectiveSelectQueryFormatter() {
        formatSqlFile("UserDirectiveSelectQuery.sql", "UserDirectiveSelectQuery$formatDataPrefix.sql")
    }

    fun testWithOptionalFormatter() {
        formatSqlFile("WithOptional.sql", "WithOptional$formatDataPrefix.sql")
    }

    fun testConditionalInClauseFormatter() {
        formatSqlFile("ConditionalInClause.sql", "ConditionalInClause$formatDataPrefix.sql")
    }

    fun testConditionalJoinClauseFormatter() {
        formatSqlFile("ConditionalJoinClause.sql", "ConditionalJoinClause$formatDataPrefix.sql")
    }

    fun testConditionalWhereClauseFormatter() {
        formatSqlFile("ConditionalWhereClause.sql", "ConditionalWhereClause$formatDataPrefix.sql")
    }

    fun testConditionalUnionFormatter() {
        formatSqlFile("ConditionalUnion.sql", "ConditionalUnion$formatDataPrefix.sql")
    }

    fun testConditionalSubqueryFormatter() {
        formatSqlFile("ConditionalSubquery.sql", "ConditionalSubquery$formatDataPrefix.sql")
    }

    fun testConditionalExistsFormatter() {
        formatSqlFile("ConditionalExists.sql", "ConditionalExists$formatDataPrefix.sql")
    }

    fun testComparisonOperatorsFormatter() {
        formatSqlFile("ComparisonOperators.sql", "ComparisonOperators$formatDataPrefix.sql")
    }

    fun testFunctionKeywordInConditionDirectiveFormatter() {
        formatSqlFile("FunctionKeywordInConditionDirective.sql", "FunctionKeywordInConditionDirective$formatDataPrefix.sql")
    }

    fun testFunctionNameColumnFormatter() {
        formatSqlFile("FunctionNameColumn.sql", "FunctionNameColumn$formatDataPrefix.sql")
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
