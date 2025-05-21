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
@file:Suppress("OverrideOnly")

package org.domaframework.doma.intellij.gutteraction.dao

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileEditor.FileEditorManager
import org.domaframework.doma.intellij.DomaSqlTest
import java.awt.event.InputEvent
import java.awt.event.MouseEvent

/**
 * Action test for jumping from Dao method to SQL file
 */
class DaoJumpActionTest : DomaSqlTest() {
    private val packageName = "gutteraction"
    private val actionId = "org.domaframework.doma.intellij.action.JumpToSQLFromDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$packageName/SelectGutterTestDao.java",
            "$packageName/InsertGutterTestDao.java",
            "$packageName/UpdateGutterTestDao.java",
            "$packageName/DeleteGutterTestDao.java",
            "$packageName/BatchInsertGutterTestDao.java",
            "$packageName/BatchUpdateGutterTestDao.java",
            "$packageName/BatchDeleteGutterTestDao.java",
            "$packageName/ScriptGutterTestDao.java",
            "$packageName/SqlProcessorGutterTestDao.java",
        )
        val notDisplayedPackage = "notdisplayed"
        addDaoJavaFile(
            "$packageName/$notDisplayedPackage/SelectInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/InsertInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/UpdateInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/DeleteInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/BatchInsertInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/BatchUpdateInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/BatchDeleteInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/ScriptInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/SqlProcessorInvalidCaretTestDao.java",
            "$packageName/$notDisplayedPackage/InvalidCaretTestDao.java",
        )
        addResourceEmptySqlFile(
            "$packageName/SelectGutterTestDao/existsSQLFile1.sql",
            "$packageName/InsertGutterTestDao/existsSQLFile1.sql",
            "$packageName/UpdateGutterTestDao/existsSQLFile1.sql",
            "$packageName/DeleteGutterTestDao/existsSQLFile1.sql",
            "$packageName/BatchInsertGutterTestDao/existsSQLFile1.sql",
            "$packageName/BatchUpdateGutterTestDao/existsSQLFile1.sql",
            "$packageName/BatchDeleteGutterTestDao/existsSQLFile1.sql",
            "$packageName/ScriptGutterTestDao/existsSQLFile1.script",
            "$packageName/SqlProcessorGutterTestDao/existsSQLFile1.sql",
        )
        addResourceEmptySqlFile(
            "$packageName/SelectGutterTestDao/existsSQLFile2.sql",
            "$packageName/InsertGutterTestDao/existsSQLFile2.sql",
            "$packageName/UpdateGutterTestDao/existsSQLFile2.sql",
            "$packageName/DeleteGutterTestDao/existsSQLFile2.sql",
            "$packageName/BatchInsertGutterTestDao/existsSQLFile2.sql",
            "$packageName/BatchUpdateGutterTestDao/existsSQLFile2.sql",
            "$packageName/BatchDeleteGutterTestDao/existsSQLFile2.sql",
            "$packageName/ScriptGutterTestDao/existsSQLFile2.script",
            "$packageName/SqlProcessorGutterTestDao/existsSQLFile2.sql",
        )
    }

    fun testSelectJumpToSqlAction() {
        val daoName = "$packageName.SelectGutterTestDao"
        val sqlFileName = "existsSQLFile1.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testInsertJumpToSqlAction() {
        val daoName = "$packageName.InsertGutterTestDao"
        val sqlFileName = "existsSQLFile2.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testUpdateJumpToSqlAction() {
        val daoName = "$packageName.UpdateGutterTestDao"
        val sqlFileName = "existsSQLFile1.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testDeleteJumpToSqlAction() {
        val daoName = "$packageName.DeleteGutterTestDao"
        val sqlFileName = "existsSQLFile1.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testBatchInsertJumpToSqlAction() {
        val daoName = "$packageName.BatchInsertGutterTestDao"
        val sqlFileName = "existsSQLFile2.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testBatchUpdateJumpToSqlAction() {
        val daoName = "$packageName.BatchUpdateGutterTestDao"
        val sqlFileName = "existsSQLFile2.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testBatchDeleteJumpToSqlAction() {
        val daoName = "$packageName.BatchDeleteGutterTestDao"
        val sqlFileName = "existsSQLFile2.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testScriptJumpToSqlAction() {
        val daoName = "$packageName.ScriptGutterTestDao"
        val sqlFileName = "existsSQLFile2.script"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testSqlProcessorJumpToSqlAction() {
        val daoName = "$packageName.SqlProcessorGutterTestDao"
        val sqlFileName = "existsSQLFile1.sql"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        jumpToSqlTest(action, sqlFileName)
    }

    fun testSelectNotDisplayJumpToSql() {
        val daoName = "$packageName.SelectInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFile.sql")
    }

    fun testInsertNotDisplayJumpToSql() {
        val daoName = "$packageName.InsertInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFileAndTemplateIncludedList.sql")
    }

    fun testUpdateNotDisplayJumpToSql() {
        val daoName = "$packageName.UpdateInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonRequireSQLFile.sql")
    }

    fun testDeleteNotDisplayJumpToSql() {
        val daoName = "$packageName.DeleteInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonRequireSQLFile.sql")
    }

    fun testBatchInsertNotDisplayJumpToSql() {
        val daoName = "$packageName.BatchInsertInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFileError.sql")
    }

    fun testBatchUpdateNotDisplayJumpToSql() {
        val daoName = "$packageName.BatchUpdateInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFileAndTemplateIncludedList.sql")
    }

    fun testBatchDeleteNotDisplayJumpToSql() {
        val daoName = "$packageName.BatchDeleteInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonRequireSQLFile.sql")
    }

    fun testScriptNotDisplayJumpToSql() {
        val daoName = "$packageName.ScriptInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFileAndTemplateIncludedList.script")
    }

    fun testSqlProcessorNotDisplayJumpToSql() {
        val daoName = "$packageName.SqlProcessorInvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFile.sql")
    }

    fun testSqlNotDisplayJumpToSql() {
        val daoName = "$packageName.InvalidCaretTestDao"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canSqlTest(action, "nonExistSQLFile.sql")
    }

    private fun getActionTest(daoName: String): AnAction {
        val dao = findDaoClass(daoName)
        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)

        val action: AnAction = ActionManager.getInstance().getAction(actionId)
        assertNotNull("Not Found Action", action)
        return action
    }

    private fun isDisplayedActionTest(action: AnAction) {
        val event = createEventAction()

        action.update(event)
        val presentation = event.presentation
        assertTrue("Action should be visible in popup", presentation.isVisible)
        assertTrue("Action should be enabled in popup", presentation.isEnabled)
    }

    private fun isNotDisplayedActionTest(action: AnAction) {
        val event = createEventAction()

        action.update(event)
        val presentation = event.presentation
        assertFalse("Action should be not visible in popup", presentation.isVisible)
        assertFalse("Action should be disabled in popup", presentation.isEnabled)
    }

    private fun createEventAction(): AnActionEvent {
        val editor = myFixture.editor
        val file = myFixture.file
        val dataContext =
            DataContext { dataId: String? ->
                if (CommonDataKeys.EDITOR.`is`(dataId)) {
                    return@DataContext editor
                }
                if (CommonDataKeys.PSI_FILE.`is`(dataId)) {
                    return@DataContext file
                }
                if (CommonDataKeys.PROJECT.`is`(dataId)) {
                    return@DataContext myFixture.project
                }
                null
            }

        val mouseEvent =
            MouseEvent(
                editor.component,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                InputEvent.BUTTON3_DOWN_MASK,
                0,
                0,
                1,
                false,
            )

        val event =
            AnActionEvent.createEvent(
                dataContext,
                null,
                "EditorPopup",
                ActionUiKind.POPUP,
                mouseEvent,
            )
        return event
    }

    private fun jumpToSqlTest(
        action: AnAction,
        sqlFileName: String,
    ) {
        myFixture.testAction(action)
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertTrue("Ope File is Not $sqlFileName", openedEditor.any { it.file.name == sqlFileName })
    }

    private fun canSqlTest(
        action: AnAction,
        sqlFileName: String,
    ) {
        myFixture.testAction(action)
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertFalse("Ope File is $sqlFileName", openedEditor.any { it.file.name == sqlFileName })
    }
}
