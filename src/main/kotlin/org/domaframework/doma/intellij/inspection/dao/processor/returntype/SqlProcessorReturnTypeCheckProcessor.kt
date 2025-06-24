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
import com.intellij.psi.PsiTypes
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationSqlProcessorReturnResult

/**
 * Processor for checking the return type of SqlProcessor annotation.
 *
 * @param psiDaoMethod The target DAO method info to be checked.
 * @param shortName The short name of the inspection to check.
 */
class SqlProcessorReturnTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    private val biFunctionClass = DomaClassName.BI_FUNCTION

    /**
     * Checks the return type of the DAO method.
     *
     * @return [ValidationResult] if the return type is invalid, otherwise null.
     */
    override fun checkReturnType(): ValidationResult? = checkParamTypeResult(biFunctionClass, 2)

    override fun checkParamTypeResult(
        targetType: DomaClassName,
        resultIndex: Int,
    ): ValidationResult? {
        val resultParam =
            getMethodParamTargetArgByIndex(targetType, resultIndex)
                ?: return null
        val optionalNestClass: PsiType = PsiClassTypeUtil.convertOptionalType(resultParam, project)

        if (optionalNestClass.canonicalText != DomaClassName.VOID.className) {
            val methodReturnType = method.returnType
            val returnTypeCheckResult =
                (optionalNestClass.canonicalText == "?" && methodReturnType?.canonicalText == "R") ||
                    methodReturnType?.canonicalText == optionalNestClass.canonicalText

            return if (!returnTypeCheckResult) {
                ValidationSqlProcessorReturnResult(
                    returnType?.canonicalText ?: "void",
                    optionalNestClass.canonicalText,
                    method.nameIdentifier,
                    shortName,
                )
            } else {
                null
            }
        }

        val methodOtherReturnType = PsiTypes.voidType()
        return generatePsiTypeReturnTypeResult(methodOtherReturnType)
    }
}
