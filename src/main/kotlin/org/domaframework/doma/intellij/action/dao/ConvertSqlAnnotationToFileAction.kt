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
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType

/**
 * Intention action to convert @Sql annotation to SQL file
 */
class ConvertSqlAnnotationToFileAction : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String = MessageBundle.message("convert.sql.annotation.to.file.family")

    override fun getText(): String = MessageBundle.message("convert.sql.annotation.to.file.text")

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return false
        val psiDaoMethod = PsiDaoMethod(project, method)

        // Check if method has @Sql annotation
        if (!psiDaoMethod.useSqlAnnotation()) {
            return false
        }

        // Check if method has @Insert, @Update, or @Delete annotation
        val supportedTypes =
            listOf(
                DomaAnnotationType.Select,
                DomaAnnotationType.Insert,
                DomaAnnotationType.Update,
                DomaAnnotationType.Delete,
                DomaAnnotationType.BatchInsert,
                DomaAnnotationType.BatchUpdate,
                DomaAnnotationType.BatchDelete,
            )

        return supportedTypes.any { it.getPsiAnnotation(method) != null }
    }

    override fun generatePreview(
        project: Project,
        editor: Editor,
        file: PsiFile,
    ): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        // Do nothing when previewing
        if (IntentionPreviewUtils.isIntentionPreviewActive()) return
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return
        val converter = SqlAnnotationConverter(project, method)
        WriteCommandAction.runWriteCommandAction(project) {
            converter.convertToSqlFile()
        }
    }
}
