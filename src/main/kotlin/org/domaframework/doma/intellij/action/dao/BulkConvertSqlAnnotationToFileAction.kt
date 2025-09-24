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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil

/**
 * Intention action to convert @Sql annotation to SQL file
 */
class BulkConvertSqlAnnotationToFileAction : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String = MessageBundle.message("bulk.convert.sql.annotation.to.file.family")

    override fun getText(): String = MessageBundle.message("bulk.convert.sql.annotation.to.file.text")

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val daoClass = findDaoClassElement(element) ?: return false
        if (getDaoClass(daoClass.containingFile) == null) return false

        val methods = getTargetMethods(daoClass, project)

        return methods.isNotEmpty()
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
        val startTime = System.nanoTime()
        val daoClass = findDaoClassElement(element) ?: return
        // Already checked in isAvailable, should not be null here
        check(getDaoClass(daoClass.containingFile) != null) { "DAO class should be available" }

        val methods = getTargetMethods(daoClass, project)

        if (methods.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(project) {
            methods.reversed().forEach { method ->
                val converter = SqlAnnotationConverter(project, method)
                converter.convertToSqlFile()
            }
        }

        PluginLoggerUtil.countLogging(
            className = this::class.java.simpleName,
            actionName = "convertSqlAnnotationToFileBatch",
            inputName = "IntentionAction",
            start = startTime,
        )
    }

    private fun getTargetMethods(
        daoClass: PsiClass,
        project: Project,
    ): List<PsiMethod> =
        daoClass.methods.filter { method ->
            val isSupportedDaoMethod = SqlAnnotationConverter.supportedTypes.any { it.getPsiAnnotation(method) != null }
            val psiDaoMethod = PsiDaoMethod(project, method)
            val isSqlAnnotationUsed = psiDaoMethod.useSqlAnnotation()
            val hasSqlFileWithParent = psiDaoMethod.sqlFile?.parent != null

            isSqlAnnotationUsed && !hasSqlFileWithParent && isSupportedDaoMethod
        }

    private fun findDaoClassElement(element: PsiElement): PsiClass? = element as? PsiClass ?: element.parent as? PsiClass
}
