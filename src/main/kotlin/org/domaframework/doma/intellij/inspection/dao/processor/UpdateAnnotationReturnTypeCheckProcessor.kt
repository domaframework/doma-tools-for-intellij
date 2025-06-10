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

import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiTypes
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeForReturningResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.isEntity

/**
 * Processor for checking the return type of update-related annotations in DAO methods.
 *
 * @property psiDaoMethod The target DAO method info to be checked.
 * @property shortName The short name of inspection.
 */
class UpdateAnnotationReturnTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the return type of the DAO method.
     *
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    override fun checkReturnType(): ValidationResult? {
        val methodOtherReturnType = PsiTypes.intType()
        if (psiDaoMethod.useSqlAnnotation() || psiDaoMethod.sqlFileOption) {
            return generatePsiTypeReturnTypeResult(methodOtherReturnType)
        }

        val parameters = method.parameterList.parameters
        val immutableEntityParam =
            parameters.firstOrNull() ?: return null

        // Check if the method is annotated with @Returning
        if (hasReturingOption()) {
            return checkReturnTypeWithReturning(immutableEntityParam)
        }

        // Check if it has an immutable entity parameter
        if (isImmutableEntity(immutableEntityParam.type.canonicalText)) {
            return checkReturnTypeImmutableEntity(immutableEntityParam)
        }

        // If the return type is not an int, return a validation result
        return generatePsiTypeReturnTypeResult(methodOtherReturnType)
    }

    /**
     * Checks the return type when the @Returning annotation is present.
     *
     * @param paramClass The method parameter to compare against.
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    private fun checkReturnTypeWithReturning(paramClass: PsiParameter): ValidationResult? {
        if (returnType == null) {
            return ValidationReturnTypeForReturningResult(
                paramClass.type.presentableText,
                method.nameIdentifier,
                shortName,
            )
        }

        val checkReturnType =
            PsiClassTypeUtil.convertOptionalType(returnType, project)
        val returnTypeClass = project.getJavaClazz(checkReturnType.canonicalText)

        return if (returnTypeClass?.isEntity() != true || returnType.canonicalText != paramClass.type.canonicalText) {
            ValidationReturnTypeForReturningResult(
                paramClass.type.presentableText,
                method.nameIdentifier,
                shortName,
            )
        } else {
            null
        }
    }

    /**
     * Checks the return type when an immutable entity parameter is present.
     *
     * @param immutableEntityParam The immutable entity parameter.
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    private fun checkReturnTypeImmutableEntity(immutableEntityParam: PsiParameter): ValidationResult? {
        val methodResultClassName = "org.seasar.doma.jdbc.Result"
        val resultTypeName = "Result"
        return generateResultReturnTypeImmutable(
            psiDaoMethod.daoType,
            immutableEntityParam.type,
            methodResultClassName,
            resultTypeName,
        )
    }
}
