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
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.validator.SqlElForItemFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.inspection.ForDirectiveInspection
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
        val daoMethod = findDaoMethod(file) ?: return null
        val forDirectiveInspection = ForDirectiveInspection(daoMethod)
        val forItem = forDirectiveInspection.getForItem(topElm)
        if (forItem != null && element.textOffset == topElm.textOffset) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceForDirectiveItem",
                "Reference",
                startTime,
            )
            return forItem.element
        }

        val validateResult = forDirectiveInspection.validateFieldAccessByForItem(targetElements)
        var parentClass = (validateResult as? ValidationCompleteResult)?.parentClass
        if (validateResult is ValidationCompleteResult && parentClass != null) {
            val validator =
                SqlElForItemFieldAccessorChildElementValidator(
                    targetElements,
                    parentClass,
                )
            val targetReferenceClass = validator.validateChildren(1)
            if (targetReferenceClass is ValidationCompleteResult) {
                val searchText = targetElements.lastOrNull()?.let { cleanString(it.text) } ?: ""
                val targetParent = targetReferenceClass.parentClass
                val reference =
                    targetParent.findField(searchText) ?: targetParent.findMethod(searchText)
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "ReferenceEntityProperty",
                    "Reference",
                    startTime,
                )
                return reference
            }
        }

        val topParam = daoMethod.findParameter(topElm.text) ?: return null
        parentClass = topParam.getIterableClazz(daoMethod.getDomaAnnotationType())

        val symbolElement =
            when (element.textOffset) {
                targetElements.first().textOffset ->
                    getReferenceDaoMethodParameter(
                        daoMethod,
                        element,
                        startTime,
                    )

                else -> getReferenceEntity(parentClass, targetElements, startTime)
            }

        return symbolElement
    }

    private fun getReferenceDaoMethodParameter(
        daoMethod: PsiMethod,
        bindElement: PsiElement,
        startTime: Long,
    ): PsiElement? {
        daoMethod
            .let { method ->
                method.methodParameters.firstOrNull { param ->
                    param.name == bindElement.text
                }
            }?.let { originalElm ->
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
        targetElement: List<PsiElement>,
        startTime: Long,
    ): PsiElement? {
        if (targetElement.size <= 2) {
            val searchText = targetElement.lastOrNull()?.text ?: ""
            return topParentClass.findField(searchText) ?: topParentClass.findMethod(searchText)
        }

        val validator =
            SqlElForItemFieldAccessorChildElementValidator(
                targetElement.dropLast(1),
                topParentClass,
            )
        val validateResult = validator.validateChildren()
        if (validateResult != null) {
            val targetClass = validateResult.parentClass ?: return null
            val searchText = targetElement.lastOrNull()?.text ?: ""
            val reference = targetClass.findField(searchText) ?: targetClass.findMethod(searchText)
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceEntityProperty",
                "Reference",
                startTime,
            )
            return reference
        }
        return null
    }
}
