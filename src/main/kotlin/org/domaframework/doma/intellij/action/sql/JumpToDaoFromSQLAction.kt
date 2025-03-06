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
package org.domaframework.doma.intellij.action.sql

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoFile
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.dao.jumpToDaoMethod
import org.domaframework.doma.intellij.common.isSupportFileType

/***
 * Action to jump from SQL file to corresponding Dao function
 */
class JumpToDaoFromSQLAction : AnAction() {
    private var currentFile: PsiFile? = null

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        currentFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        findDaoMethod(currentFile!!) ?: return
        e.presentation.isEnabledAndVisible =
            isSupportFileType(currentFile!!.virtualFile)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val startTime = System.nanoTime()
        val inputEvent = e.inputEvent
        PluginLoggerUtil.countLoggingByAction(
            this::class.java.simpleName,
            "CallJumpToDao",
            inputEvent,
            startTime,
        )

        val project = e.project ?: return
        val daoFile = findDaoFile(project, currentFile?.virtualFile!!) ?: return

        jumpToDaoMethod(project, currentFile?.virtualFile?.nameWithoutExtension!!, daoFile)
        PluginLoggerUtil.countLoggingByAction(
            this::class.java.simpleName,
            "JumpToDao",
            inputEvent,
            startTime,
        )
    }
}
