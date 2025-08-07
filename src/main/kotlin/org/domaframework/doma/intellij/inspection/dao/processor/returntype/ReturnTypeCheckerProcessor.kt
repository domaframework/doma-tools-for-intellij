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
package org.domaframework.doma.intellij.inspection.dao.processor.returntype

import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeImmutableResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeResult
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getSuperClassType
import org.domaframework.doma.intellij.inspection.dao.processor.TypeCheckerProcessor

abstract class ReturnTypeCheckerProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : TypeCheckerProcessor(psiDaoMethod) {
    protected val returnType = psiDaoMethod.psiMethod.returnType

    abstract fun checkReturnType(): ValidationResult?

    protected fun generatePsiTypeReturnTypeResult(methodOtherReturnType: PsiType): ValidationResult? =
        if (returnType != methodOtherReturnType) {
            ValidationReturnTypeResult(
                method.nameIdentifier,
                shortName,
                methodOtherReturnType.presentableText,
            )
        } else {
            null
        }

    protected fun generateResultReturnTypeImmutable(
        annotation: DomaAnnotationType,
        immutableParamClassType: PsiType,
        methodResultClassName: String,
        typeName: String,
    ): ValidationResult? {
        val actualResultTypeName =
            "$methodResultClassName<${immutableParamClassType.canonicalText}>"
        return if (returnType?.canonicalText != actualResultTypeName) {
            ValidationReturnTypeImmutableResult(
                method.nameIdentifier,
                shortName,
                annotation.name,
                typeName,
                immutableParamClassType.presentableText,
            )
        } else {
            null
        }
    }

    /**
     * Retrieve parameters with a specified type from the method parameters, and obtain the element type at the specified index.
     * @param targetType The target type to check against the method parameters.
     * @param resultIndex The index of the element type to retrieve from the target type's parameters.
     * @return [ValidationResult] if the parameter type is invalid, otherwise null.
     */
    protected open fun checkParamTypeResult(
        targetType: DomaClassName,
        resultIndex: Int,
    ): ValidationResult? = null

    /**
     * Retrieve the method parameter type that matches the target type.
     * @param targetType The target type to check against the method parameters.
     * @param resultIndex The index of the element type to retrieve from the target type's parameters.
     * @return The method parameter type if found, otherwise null.
     */
    protected fun getMethodParamTargetArgByIndex(
        targetType: DomaClassName,
        resultIndex: Int,
    ): PsiType? {
        val methodParameter = getMethodParamTargetType(targetType.className) ?: return null
        val targetParamClassType = methodParameter.getSuperClassType(targetType)
        val targetClassTypeParams = targetParamClassType?.parameters ?: return null
        if (targetClassTypeParams.size < resultIndex + 1) return null

        return targetClassTypeParams[resultIndex]
    }
}
