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
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType

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
        if (!isSupportFileType(element.containingFile)) return false

        val daoMethod = findDaoMethod(element.containingFile) ?: return false
        val psiDaoMethod = PsiDaoMethod(project, daoMethod)

        // Check if method doesn't have @Sql annotation
        if (psiDaoMethod.useSqlAnnotation()) {
            return false
        }

        // Check if method has @Insert, @Update, or @Delete annotation with sqlFile=true
        val supportedTypes =
            listOf(
                DomaAnnotationType.Select,
                DomaAnnotationType.Script,
                DomaAnnotationType.SqlProcessor,
                DomaAnnotationType.Insert,
                DomaAnnotationType.Update,
                DomaAnnotationType.Delete,
                DomaAnnotationType.BatchInsert,
                DomaAnnotationType.BatchUpdate,
                DomaAnnotationType.BatchDelete,
            )

        val hasAnnotation =
            supportedTypes.any { type ->
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
        if (!isSupportFileType(element.containingFile)) return

        val daoMethod = findDaoMethod(element.containingFile) ?: return
        val converter = SqlAnnotationConverter(project, daoMethod)
        WriteCommandAction.runWriteCommandAction(project) {
            converter.convertToSqlAnnotation()
        }
    }
}
