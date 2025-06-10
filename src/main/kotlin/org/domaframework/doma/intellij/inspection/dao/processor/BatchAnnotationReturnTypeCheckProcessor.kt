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
package org.domaframework.doma.intellij.inspection.dao.processor

import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationResult

/**
 * Processor for checking the return type of batch annotations.
 *
 * @property psiDaoMethod The target DAO method info to be checked.
 * @property shortName The short name of inspection to check.
 */
class BatchAnnotationReturnTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the return type of the DAO method.
     *
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    override fun checkReturnType(): ValidationResult? {
        val methodOtherReturnType = PsiTypes.intType().createArrayType()
        if (psiDaoMethod.useSqlAnnotation() || psiDaoMethod.sqlFileOption) {
            return generatePsiTypeReturnTypeResult(methodOtherReturnType)
        }

        // Check if it has an immutable entity parameter
        val parameters = method.parameterList.parameters
        val immutableEntityParam = parameters.firstOrNull() ?: return null

        val convertOptional = PsiClassTypeUtil.convertOptionalType(immutableEntityParam.type, project)
        val parameterType = convertOptional as PsiClassReferenceType
        val nestPsiType = parameterType.reference.typeParameters.firstOrNull() ?: return null
        val nestClass: PsiType = PsiClassTypeUtil.convertOptionalType(nestPsiType, project)

        if (isImmutableEntity(nestClass.canonicalText)) {
            return checkReturnTypeImmutableEntity(nestClass)
        }

        // If the return type is not an int[], return a validation result
        return generatePsiTypeReturnTypeResult(methodOtherReturnType)
    }

    /**
     * Checks the return type when an immutable entity parameter is present.
     *
     * @param nestClass The immutable entity parameter type.
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    private fun checkReturnTypeImmutableEntity(nestClass: PsiType): ValidationResult? {
        val methodResultClassName = "org.seasar.doma.jdbc.BatchResult"
        val resultTypeName = "BatchResult"
        return generateResultReturnTypeImmutable(
            psiDaoMethod.daoType,
            nestClass,
            methodResultClassName,
            resultTypeName,
        )
    }
}
