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
package org.domaframework.doma.intellij.common.sql.validator

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlTypes

abstract class SqlElChildElementValidator(
    open val blocks: List<PsiElement>,
    open val shorName: String = "",
) {
    abstract fun validateChildren(dropIndex: Int = 0): ValidationResult?

    open fun validateChildren(
        dropIndex: Int = 0,
        findFieldMethod: (PsiType) -> PsiParentClass = { type: PsiType -> PsiParentClass(type) },
        complete: (PsiParentClass) -> Unit,
    ): ValidationResult? = null

    fun validateFieldAccess(
        topParent: PsiParentClass,
        dropLastIndex: Int = 0,
        findFieldMethod: ((PsiType) -> PsiParentClass)? = { type -> PsiParentClass(type) },
        complete: ((PsiParentClass) -> Unit) = { parent: PsiParentClass? -> },
    ): ValidationResult? =
        getFieldAccessParentClass(
            topParent,
            dropLastIndex,
            findFieldMethod = findFieldMethod,
            complete = complete,
        )

    protected fun getFieldAccessParentClass(
        topParent: PsiParentClass,
        dropLastIndex: Int = 0,
        findFieldMethod: ((PsiType) -> PsiParentClass)? = { type -> PsiParentClass(type) },
        complete: ((PsiParentClass) -> Unit) = { parent: PsiParentClass? -> },
    ): ValidationResult? {
        val project = blocks.firstOrNull()?.project ?: return null

        var parent = topParent
        val parentType = parent.type
        val classType = parentType as? PsiClassType ?: return null

        var competeResult: ValidationCompleteResult? = null

        if (dropLastIndex > 0 && blocks.drop(1).dropLast(dropLastIndex).isEmpty()) {
            return ValidationCompleteResult(
                blocks.last(),
                parent,
            )
        }

        var parentListBaseType: PsiType? =
            if (PsiClassTypeUtil.isIterableType(classType, project)) {
                parentType
            } else {
                null
            }
        var listParamIndex = 0
        for (element in blocks.drop(1).dropLast(dropLastIndex)) {
            val searchElm = cleanString(getSearchElementText(element))
            if (searchElm.isEmpty()) {
                complete.invoke(parent)
                return ValidationCompleteResult(
                    element,
                    parent,
                )
            }

            val field =
                parent
                    .findField(searchElm)
                    ?.let { match ->
                        val type =
                            parentListBaseType?.let { PsiClassTypeUtil.getParameterType(project, match.type, it, listParamIndex) }
                                ?: match.type
                        val classType = type as? PsiClassType
                        if (classType != null && PsiClassTypeUtil.isIterableType(classType, element.project)) {
                            parentListBaseType = type
                            listParamIndex = 0
                        }
                        findFieldMethod?.invoke(type)
                    }
            val method =
                parent
                    .findMethod(searchElm)
                    ?.let { match ->
                        val returnType = match.returnType ?: return null
                        val methodReturnType =
                            parentListBaseType?.let {
                                PsiClassTypeUtil.getParameterType(
                                    project,
                                    returnType,
                                    it,
                                    listParamIndex,
                                )
                            }
                                ?: returnType
                        val classType = methodReturnType as? PsiClassType
                        if (classType != null &&
                            PsiClassTypeUtil.isIterableType(
                                classType,
                                element.project,
                            )
                        ) {
                            parentListBaseType = methodReturnType
                            listParamIndex = 0
                        }
                        findFieldMethod?.invoke(methodReturnType)
                    }
            listParamIndex++
            if (field == null && method == null) {
                return ValidationPropertyResult(
                    element,
                    parent,
                    shorName,
                )
            } else {
                if (field != null && element.nextSibling !is SqlElParameters) {
                    parent = field
                    competeResult =
                        ValidationCompleteResult(
                            element,
                            parent,
                        )
                } else if (method != null) {
                    parent = method
                    competeResult =
                        ValidationCompleteResult(
                            element,
                            parent,
                        )
                }
            }
        }
        complete.invoke(parent)
        return competeResult
    }

    private fun getSearchElementText(elm: PsiElement): String =
        if (elm is SqlElIdExpr || elm.elementType == SqlTypes.EL_IDENTIFIER) {
            elm.text
        } else {
            ""
        }
}
