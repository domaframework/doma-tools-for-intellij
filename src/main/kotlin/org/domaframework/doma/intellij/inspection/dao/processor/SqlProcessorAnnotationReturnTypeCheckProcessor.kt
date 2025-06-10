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
import org.domaframework.doma.intellij.common.validation.result.ValidationSqlProcessorReturnResult

/**
 * Processor for checking the return type of SqlProcessor annotation.
 *
 * @param psiDaoMethod The target DAO method info to be checked.
 * @param shortName The short name of the inspection to check.
 */
class SqlProcessorAnnotationReturnTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the return type of the DAO method.
     *
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    override fun checkReturnType(): ValidationResult? {
        val parameters = method.parameterList.parameters
        val biFunctionParam = parameters.firstOrNull() ?: return null
        val convertOptional = PsiClassTypeUtil.convertOptionalType(biFunctionParam.type, project)
        val parameterType = convertOptional as PsiClassReferenceType
        return checkReturnTypeBiFunctionParam(parameterType)
    }

    /**
     * Checks the return type when a BiFunction parameter is present.
     *
     * @param parameterType The BiFunction parameter type to check.
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    private fun checkReturnTypeBiFunctionParam(parameterType: PsiClassReferenceType): ValidationResult? {
        if (!parameterType.canonicalText.startsWith("java.util.function.BiFunction")) return null

        val parameterTypeParams = parameterType.reference.typeParameters
        if (parameterTypeParams.size < 3) return null

        val nestPsiType = parameterType.reference.typeParameters[2]
        if (nestPsiType == null) return null

        val nestClass: PsiType = PsiClassTypeUtil.convertOptionalType(nestPsiType, project)

        if (nestClass.canonicalText != "java.lang.Void") {
            val methodReturnType = method.returnType
            val returnTypeCheckResult =
                (nestClass.canonicalText == "?" && methodReturnType?.canonicalText == "R") ||
                    methodReturnType?.canonicalText == nestClass.canonicalText

            return if (!returnTypeCheckResult) {
                ValidationSqlProcessorReturnResult(
                    returnType?.canonicalText ?: "void",
                    nestClass.canonicalText,
                    method.nameIdentifier,
                    shortName,
                )
            } else {
                null
            }
        }
        // If the return type is not void, return a validation result
        val methodOtherReturnType = PsiTypes.voidType()
        return generatePsiTypeReturnTypeResult(methodOtherReturnType)
    }
}
