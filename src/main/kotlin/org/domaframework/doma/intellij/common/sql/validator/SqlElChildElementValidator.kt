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

import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.psi.getMethodReturnType

abstract class SqlElChildElementValidator(
    open val blocks: List<PsiElement>,
    open val shorName: String,
) {
    abstract fun validateChildren(): ValidationResult?

    protected fun validateFieldAccess(topParent: PsiParentClass): ValidationResult? {
        var parent = topParent
        val topElementType = topParent.type
        var listTypeSearchIndex = 0
        for (eml in blocks.drop(1)) {
            var isExist = false
            parent
                .findField(eml.text)
                ?.let { match ->
                    isExist = true
                    parent = PsiParentClass(match.type)
                }
            if (isExist) continue
            parent
                .findMethod(eml.text)
                ?.let { match ->
                    val returnType =
                        match.getMethodReturnType(topElementType, listTypeSearchIndex)
                            ?: return null
                    isExist = true
                    parent = PsiParentClass(returnType)
                }
            listTypeSearchIndex++
            if (!isExist) {
                return ValidationPropertyResult(
                    eml,
                    parent,
                    shorName,
                    eml.textRange,
                )
            }
        }
        return null
    }
}
