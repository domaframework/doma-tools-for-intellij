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
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeForMultiInsertReturningResult
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType

/**
 * Processor for checking the return type of MultiInsert annotation.
 *
 * @property psiDaoMethod The target DAO method info to be checked.
 * @property shortName The short name of the inspection to check.
 */
class MultiInsertAnnotationReturnTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the return type of the DAO method.
     *
     * @return ValidationResult if the return type is invalid, otherwise null.
     */
    override fun checkReturnType(): ValidationResult? {
        val parameters = method.parameterList.parameters
        val immutableEntityParam = parameters.firstOrNull() ?: return null

        val convertOptional =
            PsiClassTypeUtil.convertOptionalType(immutableEntityParam.type, project)
        val parameterType = convertOptional as? PsiClassReferenceType ?: return null
        val nestPsiType = parameterType.reference.typeParameters.firstOrNull() ?: return null
        var nestClass: PsiType? = PsiClassTypeUtil.convertOptionalType(nestPsiType, project)

        // Check if the method is annotated with @Returning
        if (hasReturingOption()) {
            return checkReturnTypeWithReturning(nestClass)
        }

        // Check if it has an immutable entity parameter
        if (nestClass != null && isImmutableEntity(nestClass.canonicalText)) {
            return checkReturnTypeImmutableEntity(nestClass)
        }

        // If the return type is not an int, return a validation result
        val methodOtherReturnType = PsiTypes.intType()
        return generatePsiTypeReturnTypeResult(methodOtherReturnType)
    }

    /**
     * Checks the return type when the @Returning annotation is present.
     *
     * @param paramPsiType The parameter type to check.
     * @return ValidationResult if the return type is invalid, otherwise null.
     */
    private fun checkReturnTypeWithReturning(paramPsiType: PsiType?): ValidationResult? {
        if (paramPsiType == null) return null

        val returnTypeName = returnType?.canonicalText ?: ""
        val methodResultClassName = DomaClassName.LIST
        val actualResultTypeText = methodResultClassName.getGenericParamCanonicalText(paramPsiType.canonicalText)

        return if (actualResultTypeText != returnTypeName) {
            ValidationReturnTypeForMultiInsertReturningResult(
                paramPsiType.presentableText,
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
     * @param immutableEntityParam The immutable entity parameter type.
     * @return ValidationResult if the return type is invalid, otherwise null.
     */
    private fun checkReturnTypeImmutableEntity(immutableEntityParam: PsiType): ValidationResult? {
        val methodResultClassName = "org.seasar.doma.jdbc.MultiResult"
        val resultTypeName = "MultiResult"
        return generateResultReturnTypeImmutable(
            DomaAnnotationType.MultiInsert,
            immutableEntityParam,
            methodResultClassName,
            resultTypeName,
        )
    }
}
