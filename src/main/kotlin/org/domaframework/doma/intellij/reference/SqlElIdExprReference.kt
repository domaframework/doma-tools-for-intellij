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
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isSupportFileType
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
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElIdExprReference(
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    var psiClassType: PsiType? = null

    private val cachedResolve: CachedValue<PsiElement?> by lazy {
        CachedValuesManager.getManager(element.project).createCachedValue {
            val result = doResolve()
            CachedValueProvider.Result(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    val file: PsiFile? = element.containingFile

    override fun resolve(): PsiElement? = cachedResolve.value

    private fun doResolve(): PsiElement? {
        if (file == null || !isSupportFileType(file)) return null
        val startTime = System.nanoTime()
        return superResolveLogic(startTime, file)
    }

    private fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val targetElements = getBlockCommentElements(element)
        if (targetElements.isEmpty()) return null

        val topElm = targetElements.firstOrNull() as? PsiElement ?: return null

        if (topElm.prevSibling.elementType == SqlTypes.AT_SIGN) return null

        val forDirectiveInspection = ForDirectiveInspection("")
        val forItem = forDirectiveInspection.getForItem(topElm)
        if (forItem != null && element.textOffset == topElm.textOffset) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceForDirective",
                "Reference",
                startTime,
            )
            return forItem.element
        }

        val errorElement = forDirectiveInspection.getFieldAccessParentClass(targetElements)
        var parentClass = (errorElement as? ValidationCompleteResult)?.parentClass
        if (errorElement is ValidationCompleteResult && parentClass != null) {
            val searchText = targetElements.lastOrNull()?.let { cleanString(it.text) } ?: ""
            val reference = parentClass.findField(searchText) ?: parentClass.findMethod(searchText)
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceEntityProperty",
                "Reference",
                startTime,
            )
            return reference
        }

        val daoMethod = findDaoMethod(file)
        if (daoMethod != null) {
            val topParam = daoMethod.findParameter(topElm.text) ?: return null
            parentClass = topParam.getIterableClazz(daoMethod.getDomaAnnotationType())
        }

        val symbolElement =
            when (element.textOffset) {
                targetElements.first().textOffset ->
                    daoMethod?.let {
                        getReferenceDaoMethodParameter(
                            it,
                            element,
                            startTime,
                        )
                    }

                else -> parentClass?.let { getReferenceEntity(it, targetElements, startTime) }
            }

        return symbolElement
    }

    private fun getBlockCommentElements(element: PsiElement): List<PsiElement> {
        val fieldAccessExpr = PsiTreeUtil.getParentOfType(element, SqlElFieldAccessExpr::class.java)
        val nodeElm =
            if (fieldAccessExpr != null) {
                PsiTreeUtil
                    .getChildrenOfType(
                        fieldAccessExpr,
                        SqlElIdExpr::class.java,
                    )?.filter { it.textOffset <= element.textOffset }
            } else {
                listOf(element)
            }
        return nodeElm
            ?.toList()
            ?.sortedBy { it.textOffset } ?: emptyList()
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
        val errorElement = validator.validateChildren()
        if (errorElement != null) {
            val targetClass = errorElement.parentClass ?: return null
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
