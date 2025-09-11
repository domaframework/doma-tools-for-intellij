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
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil

/**
 * Intention action to convert SQL file to @Sql annotation
 */
class ConvertSqlFileToAnnotationAction : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String = MessageBundle.message("convert.sql.file.to.annotation.family")

    override fun getText(): String = MessageBundle.message("convert.sql.file.to.annotation.text")

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val file = element.containingFile ?: return false
        if (isJavaOrKotlinFileType(file) && getDaoClass(file) != null) {
            return checkOnMethod(element, project)
        }

        if (isSupportFileType(file)) {
            return checkOnSqlFile(element, project)
        }

        return false
    }

    private fun checkOnMethod(
        element: PsiElement,
        project: Project,
    ): Boolean {
        val daoMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return false
        return checkAvailable(project, daoMethod)
    }

    private fun checkOnSqlFile(
        element: PsiElement,
        project: Project,
    ): Boolean {
        val daoMethod = findDaoMethod(element.containingFile) ?: return false
        return checkAvailable(project, daoMethod)
    }

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

        val file = element.containingFile
        if (isJavaOrKotlinFileType(file)) {
            return processOnMethod(element, project)
        }

        // Process if the file type is SQL
        if (isSupportFileType(file)) {
            return processOnSqlFile(element, project)
        }
    }

    private fun processOnMethod(
        element: PsiElement,
        project: Project,
    ) {
        val daoMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return

        val startTime = System.nanoTime()
        val converter = SqlAnnotationConverter(project, daoMethod)
        WriteCommandAction.runWriteCommandAction(project) {
            converter.convertToSqlAnnotation()
        }

        PluginLoggerUtil.countLogging(
            className = this::class.java.simpleName,
            actionName = "convertSqlFileToAnnotationOnMethod",
            inputName = "IntentionAction",
            start = startTime,
        )
    }

    private fun processOnSqlFile(
        element: PsiElement,
        project: Project,
    ) {
        val daoMethod = findDaoMethod(element.containingFile) ?: return

        val startTime = System.nanoTime()
        val converter = SqlAnnotationConverter(project, daoMethod)
        WriteCommandAction.runWriteCommandAction(project) {
            converter.convertToSqlAnnotation()
        }

        PluginLoggerUtil.countLogging(
            className = this::class.java.simpleName,
            actionName = "convertSqlFileToAnnotationOnSQL",
            inputName = "IntentionAction",
            start = startTime,
        )
    }
}
