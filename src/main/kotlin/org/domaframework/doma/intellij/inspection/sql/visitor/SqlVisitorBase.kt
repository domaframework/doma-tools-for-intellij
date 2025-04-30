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
package org.domaframework.doma.intellij.inspection.sql.visitor

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.psi.SqlElNewExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.psi.SqlVisitor

open class SqlVisitorBase : SqlVisitor() {
    var file: PsiFile? = null

    protected fun setFile(element: PsiElement): Boolean {
        if (file == null) {
            file = element.containingFile
        }
        return false
    }

    /**
     * For processing inside Sql annotations, get it as an injected custom language
     */
    protected fun initInjectionElement(
        basePsiFile: PsiFile,
        project: Project,
        literal: PsiLiteralExpression,
    ): PsiFile? =
        when (isJavaOrKotlinFileType(basePsiFile)) {
            true -> {
                val injectedLanguageManager =
                    InjectedLanguageManager.getInstance(project)
                injectedLanguageManager
                    .getInjectedPsiFiles(literal)
                    ?.firstOrNull()
                    ?.first as? PsiFile
            }

            false -> null
        }

    protected fun isLiteralOrStatic(targetElement: PsiElement): Boolean =
        (
            targetElement.firstChild?.elementType == SqlTypes.EL_STRING ||
                targetElement.firstChild?.elementType == SqlTypes.EL_CHAR ||
                targetElement.firstChild?.elementType == SqlTypes.EL_NUMBER ||
                targetElement.firstChild?.elementType == SqlTypes.EL_NULL ||
                targetElement.firstChild?.elementType == SqlTypes.BOOLEAN ||
                targetElement.firstChild is SqlElNewExpr ||
                targetElement.text.startsWith("@")
        )
}
