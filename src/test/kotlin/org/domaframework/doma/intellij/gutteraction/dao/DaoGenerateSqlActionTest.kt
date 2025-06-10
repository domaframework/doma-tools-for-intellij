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
 * Action test to generate SQL file from DAO method
 */
class DaoGenerateSqlActionTest : DomaSqlTest() {
    private val packageName = "quickfix"
    private val actionId = "org.domaframework.doma.intellij.GenerateSqlAction"

    override fun setUp() {
        super.setUp()
        val gutterActionPackageName = "gutteraction"
        addDaoJavaFile(
            "$packageName/SelectQuickFixTestDao.java",
            "$packageName/InsertQuickFixTestDao.java",
            "$packageName/UpdateQuickFixTestDao.java",
            "$packageName/DeleteQuickFixTestDao.java",
            "$packageName/BatchInsertQuickFixTestDao.java",
            "$packageName/BatchUpdateQuickFixTestDao.java",
            "$packageName/BatchDeleteQuickFixTestDao.java",
            "$packageName/ScriptQuickFixTestDao.java",
            "$packageName/SqlProcessorQuickFixTestDao.java",
        )
        addDaoJavaFile(
            "$gutterActionPackageName/SelectGutterTestDao.java",
            "$gutterActionPackageName/InsertGutterTestDao.java",
            "$gutterActionPackageName/notdisplayed/UpdateInvalidCaretTestDao.java",
            "$gutterActionPackageName/notdisplayed/DeleteInvalidCaretTestDao.java",
            "$gutterActionPackageName/BatchInsertGutterTestDao.java",
            "$gutterActionPackageName/BatchUpdateGutterTestDao.java",
            "$gutterActionPackageName/notdisplayed/BatchDeleteInvalidCaretTestDao.java",
            "$gutterActionPackageName/ScriptGutterTestDao.java",
            "$gutterActionPackageName/SqlProcessorGutterTestDao.java",
        )

        addResourceEmptySqlFile(
            "$packageName/SelectQuickFixTestDao/existsSQLFile.sql",
            "$packageName/InsertQuickFixTestDao/existsSQLFile.sql",
            "$packageName/UpdateQuickFixTestDao/existsSQLFile.sql",
            "$packageName/DeleteQuickFixTestDao/existsSQLFile.sql",
            "$packageName/BatchInsertQuickFixTestDao/existsSQLFile.sql",
            "$packageName/BatchUpdateQuickFixTestDao/existsSQLFile.sql",
            "$packageName/BatchDeleteQuickFixTestDao/existsSQLFile.sql",
            "$packageName/ScriptQuickFixTestDao/existsSQLFile.script",
            "$packageName/SqlProcessorQuickFixTestDao/existsSQLFile.sql",
        )
        addResourceEmptySqlFile(
            "$gutterActionPackageName/SelectGutterTestDao/existsSQLFile1.sql",
            "$gutterActionPackageName/InsertGutterTestDao/existsSQLFile2.sql",
            "$gutterActionPackageName/UpdateGutterTestDao/existsSQLFile1.sql",
            "$gutterActionPackageName/DeleteGutterTestDao/existsSQLFile1.sql",
            "$gutterActionPackageName/BatchInsertGutterTestDao/existsSQLFile2.sql",
            "$gutterActionPackageName/BatchUpdateGutterTestDao/existsSQLFile2.sql",
            "$gutterActionPackageName/BatchDeleteGutterTestDao/existsSQLFile1.sql",
            "$gutterActionPackageName/ScriptGutterTestDao/existsSQLFile2.script",
            "$gutterActionPackageName/SqlProcessorGutterTestDao/existsSQLFile1.sql",
        )
    }

    fun testGenerateSQLFileSelect() {
        val testDaoName = "SelectQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileInsert() {
        val testDaoName = "InsertQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileUpdate() {
        val testDaoName = "UpdateQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileDelete() {
        val testDaoName = "DeleteQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileBatchInsert() {
        val testDaoName = "BatchInsertQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileBatchUpdate() {
        val testDaoName = "BatchUpdateQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileBatchDelete() {
        val testDaoName = "BatchDeleteQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testGenerateSQLFileScript() {
        val testDaoName = "ScriptQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", true)
    }

    fun testGenerateSQLFileSqlProcessor() {
        val testDaoName = "SqlProcessorQuickFixTestDao"
        val daoName = "$packageName.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isDisplayedActionTest(action)
        generateSqlTest(action, "$testDaoName/generateSQLFile", false)
    }

    fun testNonGenerateSQLFileSelect() {
        val testDaoName = "SelectGutterTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/existsSQLFile1.sql", false)
    }

    fun testNonGenerateSQLFileInsert() {
        val testDaoName = "InsertGutterTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/existsSQLFile2", false)
    }

    fun testNonGenerateSQLFileUpdate() {
        val testDaoName = "UpdateInvalidCaretTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/nonRequireSQLFile.sql", false)
    }

    fun testNonGenerateSQLFileDelete() {
        val testDaoName = "DeleteInvalidCaretTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/NoSQLFileRequired", false)
    }

    fun testNonGenerateSQLFileBatchInsert() {
        val testDaoName = "BatchInsertGutterTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/existsSQLFile2", false)
    }

    fun testNonGenerateSQLFileBatchUpdate() {
        val testDaoName = "BatchUpdateGutterTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/existsSQLFile2", false)
    }

    fun testNonGenerateSQLFileBatchDelete() {
        val testDaoName = "BatchDeleteInvalidCaretTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/NoSQLFileRequired", false)
    }

    fun testNonGenerateSQLFileScript() {
        val testDaoName = "ScriptGutterTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/existsSQLFile2", true)
    }

    fun testNonGenerateSQLFileSqlProcessor() {
        val testDaoName = "SqlProcessorGutterTestDao"
        val daoName = "gutteraction.$testDaoName"
        val action: AnAction = getActionTest(daoName)
        isNotDisplayedActionTest(action)
        canNotGenerateSqlFileTest(action, "$testDaoName/existsSQLFile2", false)
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

    private fun generateSqlTest(
        action: AnAction,
        sqlFileName: String,
        isScript: Boolean,
    ) {
        myFixture.testAction(action)

        val sqlFile = "$sqlFileName.${if (isScript) "script" else "sql"}"
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertTrue(
            "Open File is Not $sqlFileName",
            openedEditor.any { it.file.name == sqlFile.substringAfter("/") },
        )

        val generatedSql = findSqlFile("$packageName/$sqlFile")
        assertTrue("Not Found SQL File [$sqlFile]", generatedSql != null)
    }

    private fun canNotGenerateSqlFileTest(
        action: AnAction,
        sqlFileName: String,
        isScript: Boolean,
    ) {
        myFixture.testAction(action)

        val sqlFile = "$sqlFileName.${if (isScript) "script" else "sql"}"
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertFalse(
            "Open File is [$sqlFile]",
            openedEditor.any { it.file.name == sqlFile.substringAfter("/") },
        )

        val generatedSql = findSqlFile("$packageName/$sqlFile")
        assertFalse("Not subject to action", generatedSql != null)
    }
}
