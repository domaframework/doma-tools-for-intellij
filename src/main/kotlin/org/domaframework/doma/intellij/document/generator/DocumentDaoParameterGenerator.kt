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
package org.domaframework.doma.intellij.document.generator

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.expr.accessElementsPrevOriginalElement
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective

class DocumentDaoParameterGenerator(
    val originalElement: PsiElement,
    override val project: Project,
    val result: MutableList<String?>,
) : DocumentGenerator(project) {
    override fun generateDocument() {
        var topParentType: PsiParentClass? = null
        val selfSkip = isSelfSkip(originalElement)
        val forDirectives = ForDirectiveUtil.getForDirectiveBlocks(originalElement, selfSkip)
        val fieldAccessExpr =
            PsiTreeUtil.getParentOfType(
                originalElement,
                SqlElFieldAccessExpr::class.java,
            )
        val fieldAccessBlocks =
            fieldAccessExpr?.accessElementsPrevOriginalElement(originalElement.textOffset)
        val searchElement = fieldAccessBlocks?.firstOrNull() ?: originalElement

        var isBatchAnnotation = false
        val forItem = ForDirectiveUtil.findForItem(searchElement, forDirectives = forDirectives)
        if (forItem != null) {
            val forItemClassType = ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectives, forItem)
            val specifiedClassType =
                ForDirectiveUtil.resolveForDirectiveItemClassTypeBySuffixElement(
                    searchElement.text,
                )
            topParentType =
                if (specifiedClassType != null) {
                    PsiParentClass(specifiedClassType)
                } else {
                    forItemClassType
                }
        } else {
            val forDirectiveExpr =
                PsiTreeUtil.getParentOfType(
                    searchElement,
                    SqlElForDirective::class.java,
                )
            if (forDirectiveExpr != null && forDirectiveExpr.getForItem() == searchElement) {
                // For elements defined with the for directive, DAO parameters are not searched.
                return
            }
            val daoMethod = findDaoMethod(originalElement.containingFile) ?: return
            val param = daoMethod.findParameter(originalElement.text) ?: return
            isBatchAnnotation = PsiDaoMethod(project, daoMethod).daoType.isBatchAnnotation()
            topParentType = PsiParentClass(param.type)
        }

        if (fieldAccessExpr != null && fieldAccessBlocks != null) {
            topParentType?.let {
                ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                    fieldAccessBlocks,
                    project,
                    it,
                    isBatchAnnotation = isBatchAnnotation,
                    complete = { lastType ->
                        result.add("${generateTypeLink(lastType)} ${originalElement.text}")
                    },
                )
            }
            return
        }
        result.add("${generateTypeLink(topParentType)} ${originalElement.text}")
    }
}
