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
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationCompleteResult
import org.domaframework.doma.intellij.extension.psi.psiClassType
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

        val targetElements =
            getBlockCommentElements(element, SqlElStaticFieldAccessExpr::class.java)

        val psiStaticClass = PsiStaticElement(staticAccessParent.elClass.elIdExprList, staticAccessParent.containingFile)
        val referenceClass = psiStaticClass.getRefClazz() ?: return null
        val referenceParentClass = PsiParentClass(referenceClass.psiClassType)
        if (targetElements.size == 1) {
            val searchText = element.text ?: ""
            val reference = referenceParentClass.findField(searchText) ?: referenceParentClass.findMethod(searchText)
            if (reference != null) {
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "ReferenceStaticProperty",
                    "Reference",
                    startTime,
                )
            }
            return reference
        }
        val topFieldClassType = ForDirectiveUtil.getStaticFieldAccessTopElementClassType(staticAccessParent, referenceClass)
        val result =
            topFieldClassType?.let {
                ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                    targetElements,
                    staticAccessParent.project,
                    it.parent,
                    dropLastIndex = 1,
                )
            }
        if (result is ValidationCompleteResult) {
            val parent = result.parentClass
            val searchText = element.text ?: ""
            val reference = parent.findField(searchText) ?: parent.findMethod(searchText)
            if (reference != null) {
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "ReferenceStaticProperty",
                    "Reference",
                    startTime,
                )
            }
            return reference
        }
        return null
    }
}
