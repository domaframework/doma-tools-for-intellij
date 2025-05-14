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
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElIdExprReference(
    element: PsiElement,
) : SqlElExprReference(element) {
    override fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val targetElements = getBlockCommentElements(element, SqlElFieldAccessExpr::class.java)
        if (targetElements.isEmpty()) return null
        val topElm = targetElements.firstOrNull() as? PsiElement ?: return null
        if (topElm.prevSibling.elementType == SqlTypes.AT_SIGN) return null

        // Refers to an element defined in the for directive
        val isSelfSkip = isSelfSkip(topElm)
        val forDirectiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(element, isSelfSkip)
        val forItem = ForDirectiveUtil.findForItem(topElm, forDirectives = forDirectiveBlocks)
        if (forItem != null && element.textOffset == topElm.textOffset) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceForDirectiveItem",
                "Reference",
                startTime,
            )
            return forItem
        }

        val fieldAccessExpr = PsiTreeUtil.getParentOfType(element, SqlElFieldAccessExpr::class.java)
        if (fieldAccessExpr == null || element.textOffset == topElm.textOffset) {
            val daoMethod = findDaoMethod(file) ?: return null
            return getReferenceDaoMethodParameter(
                daoMethod,
                element,
                startTime,
            )
        }

        // Reference to field access elements
        var parentClass: PsiParentClass? = null
        var isBatchAnnotation = false
        if (forItem != null) {
            val project = topElm.project
            val forItemClassType =
                ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectiveBlocks, forItem)
                    ?: return null
            val specifiedClassType =
                ForDirectiveUtil.resolveForDirectiveItemClassTypeBySuffixElement(topElm.text)
            parentClass =
                if (specifiedClassType != null) {
                    PsiParentClass(specifiedClassType)
                } else {
                    forItemClassType
                }
        } else {
            val daoMethod = findDaoMethod(file) ?: return null
            val param = daoMethod.findParameter(topElm.text) ?: return null
            parentClass = PsiParentClass(param.type)
            isBatchAnnotation = PsiDaoMethod(topElm.project, daoMethod).daoType.isBatchAnnotation()
        }

        val result =
            ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                targetElements,
                topElm.project,
                parentClass,
                isBatchAnnotation = isBatchAnnotation,
                shortName = "",
                dropLastIndex = 1,
            )

        if (result is ValidationCompleteResult) {
            val lastType = result.parentClass
            return getReferenceEntity(lastType, element, startTime)
        }

        return null
    }

    private fun isSelfSkip(targetElement: PsiElement): Boolean {
        val forItem = ForItem(targetElement)
        val forDirectiveExpr = forItem.getParentForDirectiveExpr()
        return !(forDirectiveExpr != null && forDirectiveExpr.getForItem()?.textOffset == targetElement.textOffset)
    }

    private fun getReferenceDaoMethodParameter(
        daoMethod: PsiMethod,
        bindElement: PsiElement,
        startTime: Long,
    ): PsiElement? {
        daoMethod.findParameter(bindElement.text)?.let { originalElm ->
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceDaoMethodParameter",
                "Reference",
                startTime,
            )
            psiClassType = originalElm.type
            return originalElm.originalElement
        } ?: return null
    }

    private fun getReferenceEntity(
        topParentClass: PsiParentClass,
        targetElement: PsiElement,
        startTime: Long,
    ): PsiElement? {
        val searchText = cleanString(targetElement.text)
        val reference =
            topParentClass.findField(searchText) ?: topParentClass.findMethod(searchText)
        if (reference != null) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceEntityProperty",
                "Reference",
                startTime,
            )
        }
        return reference
    }
}
