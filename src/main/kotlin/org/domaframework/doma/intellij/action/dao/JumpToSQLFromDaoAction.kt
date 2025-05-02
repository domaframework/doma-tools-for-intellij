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

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.dao.jumpSqlFromDao
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil

class JumpToSQLFromDaoAction : AnAction() {
    private var currentFile: PsiFile? = null

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        currentFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val file: PsiFile = currentFile ?: return

        if (!isJavaOrKotlinFileType(file) || getDaoClass(file) == null) return

        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val project = e.project ?: return
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return
        val psiDaoMethod = PsiDaoMethod(project, method)

        e.presentation.isEnabledAndVisible =
            psiDaoMethod.isUseSqlFileMethod() &&
            psiDaoMethod.sqlFile != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val startTime = System.nanoTime()
        val inputEvent = e.inputEvent
        PluginLoggerUtil.countLoggingByAction(
            this::class.java.simpleName,
            "CallJumpToSql",
            inputEvent,
            startTime,
        )
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val element = currentFile?.findElementAt(editor.caretModel.offset) ?: return
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return
        val project = e.project ?: return
        val psiDaoMethod = PsiDaoMethod(project, method)

        PluginLoggerUtil.countLoggingByAction(
            this::class.java.simpleName,
            "JumpToSql",
            inputEvent,
            startTime,
        )
        val sqlFile = psiDaoMethod.sqlFile ?: return
        jumpSqlFromDao(project, sqlFile)
    }
}
