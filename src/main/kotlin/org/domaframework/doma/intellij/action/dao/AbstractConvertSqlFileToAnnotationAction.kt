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

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil

/**
 * Abstract base class for converting SQL file to @Sql annotation
 */
abstract class AbstractConvertSqlFileToAnnotationAction : PsiElementBaseIntentionAction() {
    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        if (!isTargetFile(element)) {
            return false
        }

        val daoMethod = getDaoMethod(element) ?: return false
        return checkAvailable(project, daoMethod)
    }

    /**
     * Check if the element is in a target file type for this action
     */
    protected abstract fun isTargetFile(element: PsiElement): Boolean

    /**
     * Get the DAO method from the element
     */
    protected abstract fun getDaoMethod(element: PsiElement): PsiMethod?

    /**
     * Get the action name for logging
     */
    protected abstract fun getActionName(): String

    private fun checkAvailable(
        project: Project,
        daoMethod: PsiMethod,
    ): Boolean {
        val psiDaoMethod = PsiDaoMethod(project, daoMethod)

        // Check if method doesn't have @Sql annotation
        if (psiDaoMethod.sqlFile == null || psiDaoMethod.useSqlAnnotation()) {
            return false
        }

        val hasAnnotation =
            SqlAnnotationConverter.supportedTypes.any { type ->
                val annotation = type.getPsiAnnotation(daoMethod)
                annotation != null
            }

        // Must have annotation with sqlFile=true and SQL file must exist
        return hasAnnotation && psiDaoMethod.sqlFile != null
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        // Do nothing when previewing
        if (IntentionPreviewUtils.isIntentionPreviewActive()) return

        val daoMethod = getDaoMethod(element) ?: return

        val startTime = System.nanoTime()
        val converter = SqlAnnotationConverter(project, daoMethod)
        WriteCommandAction.runWriteCommandAction(project) {
            converter.convertToSqlAnnotation()
        }

        PluginLoggerUtil.countLogging(
            className = this::class.java.simpleName,
            actionName = getActionName(),
            inputName = "IntentionAction",
            start = startTime,
        )
    }
}
