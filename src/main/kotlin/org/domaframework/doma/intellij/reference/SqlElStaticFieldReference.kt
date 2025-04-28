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
package org.domaframework.doma.intellij.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.sql.validator.SqlElStaticFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class SqlElStaticFieldReference(
    element: PsiElement,
) : SqlElExprReference(element) {
    override fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val staticAccessParent =
            PsiTreeUtil.getParentOfType(element, SqlElStaticFieldAccessExpr::class.java)
        if (staticAccessParent == null) return null

        val targetElements = getBlockCommentElements(element, SqlElStaticFieldAccessExpr::class.java)
        val validator =
            SqlElStaticFieldAccessorChildElementValidator(
                targetElements,
                staticAccessParent,
            )

        val initialPsiParentClass = validator.getClassType() ?: return null
        val project = file.project
        val fieldAccessLastParentResult =
            if (targetElements.size == 1) {
                validator.validateFieldAccess(project, initialPsiParentClass, dropLastIndex = 1)
            } else if (targetElements.size >= 2) {
                validator.validateChildren(1)
            } else {
                null
            }
        if (fieldAccessLastParentResult is ValidationCompleteResult) {
            val searchText = element.text ?: ""
            val parent = fieldAccessLastParentResult.parentClass
            return parent.findField(searchText) ?: parent.findMethod(searchText)
        }

        return null
    }
}
