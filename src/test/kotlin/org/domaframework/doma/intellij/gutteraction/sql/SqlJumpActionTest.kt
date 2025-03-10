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

package org.domaframework.doma.intellij.gutteraction.sql

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import org.domaframework.doma.intellij.DomaSqlTest
import java.awt.event.InputEvent
import java.awt.event.MouseEvent

/**
 * Action test for jumping from SQL file to Dao method
 */
class SqlJumpActionTest : DomaSqlTest() {
    private val packageName = "gutteraction"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile("$packageName/JumpActionTestDao.java")
        addDaoJavaFile("$packageName/JumpDeclarationActionSqlAnnotationTestDao.java")
        addSqlFile(
            "$packageName/JumpActionTestDao/jumpToDaoFile.sql",
            "$packageName/JumpActionTestDao/notDisplayGutterWithNonExistentDaoMethod.sql",
            "$packageName/JumpActionTestDao/jumpToDaoMethodArgumentDefinition.sql",
            "$packageName/JumpActionTestDao/jumpToClassFieldDefinition.sql",
            "$packageName/JumpActionTestDao/jumpsToClassMethodDefinition.sql",
            "$packageName/JumpActionTestDao/jumpToStaticFieldDefinition.sql",
            "$packageName/JumpActionTestDao/jumpToStaticMethodDefinition.sql",
        )
    }

    fun testJumpActionFromSQLFileToDaoMethodCanBePerformed() {
        val sqlName = "$packageName/JumpActionTestDao/jumpToDaoFile.sql"
        val action: AnAction? = getJumDaoActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "JumpActionTestDao.java")
    }

    fun testNotDisplayedJumpMethodActionInSQLForNonExistentDaoMethod() {
        val sqlName = "$packageName/JumpActionTestDao/notDisplayGutterWithNonExistentDaoMethod.sql"
        val action: AnAction? = getJumDaoActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isNotDisplayedActionTest(action)
        canNotJumpToDaoTest(action, "JumpActionTestDao.java")
    }

    fun testNotDisplayedJumpDeclarationActionInSQLForNonExistentDaoMethod() {
        val sqlName = "$packageName/JumpActionTestDao/notDisplayGutterWithNonExistentDaoMethod.sql"
        val action: AnAction? = getJumDeclarationActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isNotDisplayedActionTest(action)
        canNotJumpToDaoTest(action, "JumpActionTestDao.java")
    }

    fun testJumpToDaoMethodArgumentDefinition() {
        val sqlName = "$packageName/JumpActionTestDao/jumpToDaoMethodArgumentDefinition.sql"
        val action: AnAction? = getJumpVariableActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "JumpActionTestDao.java")
    }

    fun testJumpToClassFieldDefinition() {
        val sqlName = "$packageName/JumpActionTestDao/jumpToClassFieldDefinition.sql"
        val action: AnAction? = getJumpVariableActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "Project.java")
    }

    fun testJumpsToClassMethodDefinition() {
        val sqlName = "$packageName/JumpActionTestDao/jumpsToClassMethodDefinition.sql"
        val action: AnAction? = getJumpVariableActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "Employee.java")
    }

    fun testJumpToStaticFieldDefinition() {
        val sqlName = "$packageName/JumpActionTestDao/jumpToStaticFieldDefinition.sql"
        val action: AnAction? = getJumpVariableActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "ProjectDetail.java")
    }

    fun testJumpToStaticMethodDefinition() {
        val sqlName = "$packageName/JumpActionTestDao/jumpToStaticMethodDefinition.sql"
        val action: AnAction? = getJumpVariableActionTest(sqlName)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "ProjectDetail.java")
    }

    fun testJumpToDeclarationSqlAnnotation() {
        val dao = findDaoClass("$packageName.JumpDeclarationActionSqlAnnotationTestDao")
        val action: AnAction? = getJumpVariableActionTestOnSqlAnnotation(dao.containingFile.virtualFile)
        assertNotNull("Not Found Action", action)
        if (action == null) return

        isDisplayedActionTest(action)
        jumpToDaoTest(action, "Employee.java")
    }

    private fun getJumDaoActionTest(sqlName: String): AnAction? {
        val sql = findSqlFile(sqlName)
        assertNotNull("Not Found SQL File", sql)
        if (sql == null) return null

        myFixture.configureFromExistingVirtualFile(sql)

        val actionId = "org.domaframework.doma.intellij.JumpToDaoFromSQL"
        val action: AnAction = ActionManager.getInstance().getAction(actionId)
        assertNotNull("Not Found Action", action)
        return action
    }

    private fun getJumDeclarationActionTest(sqlName: String): AnAction? {
        val sql = findSqlFile(sqlName)
        assertNotNull("Not Found SQL File", sql)
        if (sql == null) return null

        myFixture.configureFromExistingVirtualFile(sql)

        val actionId = "org.domaframework.doma.intellij.JumpToDeclarationFromSql"
        val action: AnAction = ActionManager.getInstance().getAction(actionId)
        assertNotNull("Not Found Action", action)
        return action
    }

    private fun getJumpVariableActionTestOnSqlAnnotation(daoFile: VirtualFile): AnAction {
        myFixture.configureFromExistingVirtualFile(daoFile)
        val actionId = "org.domaframework.doma.intellij.JumpToDeclarationFromSql"
        val action: AnAction = ActionManager.getInstance().getAction(actionId)
        assertNotNull("Not Found Action", action)
        return action
    }

    private fun getJumpVariableActionTest(sqlName: String): AnAction? {
        val sql = findSqlFile(sqlName)
        assertNotNull("Not Found SQL File", sql)
        if (sql == null) return null

        myFixture.configureFromExistingVirtualFile(sql)

        val actionId = "org.domaframework.doma.intellij.JumpToDeclarationFromSql"
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

    private fun jumpToDaoTest(
        action: AnAction,
        openFileName: String,
    ) {
        myFixture.testAction(action)

        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertTrue(
            "Open File is Not [${openedEditor.map { it.file.name }}] $openFileName",
            openedEditor.any { it.file.name == openFileName },
        )
    }

    @Suppress("SameParameterValue")
    private fun canNotJumpToDaoTest(
        action: AnAction,
        daoFileName: String,
    ) {
        myFixture.testAction(action)
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        assertFalse("Ope File is $daoFileName", openedEditor.any { it.file.name == daoFileName })
    }
}
