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
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz

class SqlElFieldAccessorChildElementValidator(
    override val blocks: List<PsiElement>,
    private val file: PsiFile,
    override val shorName: String,
) : SqlElChildElementValidator(blocks, shorName) {
    override fun validateChildren(): ValidationResult? {
        val daoMethod = findDaoMethod(file) ?: return null
        val topElement: PsiElement = blocks.firstOrNull() ?: return null
        val validDaoParam =
            daoMethod.findParameter(topElement.text)
                ?: return ValidationDaoParamResult(
                    topElement,
                    daoMethod.name,
                    shorName,
                    topElement.textRange,
                )

        val parentClass =
            validDaoParam.getIterableClazz(daoMethod.getDomaAnnotationType())

        return validateFieldAccess(
            parentClass,
        )
    }
}
