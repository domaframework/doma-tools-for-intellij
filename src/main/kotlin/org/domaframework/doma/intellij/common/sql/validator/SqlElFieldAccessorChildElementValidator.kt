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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz

class SqlElFieldAccessorChildElementValidator(
    override val blocks: List<PsiElement>,
    private val file: PsiFile,
    override val shorName: String = "",
    private val topDaoParameter: PsiParameter? = null,
) : SqlElChildElementValidator(blocks, shorName) {
    override fun validateChildren(
        dropIndex: Int,
        findFieldMethod: (PsiType) -> PsiParentClass,
        complete: (PsiParentClass) -> Unit,
    ): ValidationResult? {
        val parentClassResult = getParentClass()
        val parentClass = parentClassResult?.parentClass
        if (parentClassResult is ValidationCompleteResult && parentClass != null) {
            return validateFieldAccess(
                parentClass,
                complete = complete,
            )
        }

        return parentClassResult
    }

    override fun validateChildren(dropIndex: Int): ValidationResult? {
        val parentClassResult = getParentClass()
        val parentClass = parentClassResult?.parentClass
        if (parentClassResult is ValidationCompleteResult && parentClass != null) {
            return validateFieldAccess(parentClass)
        }

        return parentClassResult
    }

    private fun getParentClass(): ValidationResult? {
        val daoMethod = findDaoMethod(file) ?: return null
        val topElement: PsiElement = blocks.firstOrNull() ?: return null
        val validDaoParam =
            topDaoParameter
                ?: daoMethod.findParameter(topElement.text)

        if (validDaoParam == null) {
            return ValidationDaoParamResult(
                topElement,
                daoMethod.name,
                shorName,
            )
        }

        return ValidationCompleteResult(
            topElement,
            validDaoParam.getIterableClazz(daoMethod.getDomaAnnotationType()),
        )
    }
}
