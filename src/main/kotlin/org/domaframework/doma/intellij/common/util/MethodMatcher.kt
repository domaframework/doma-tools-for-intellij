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
package org.domaframework.doma.intellij.common.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResultFunctionCallParameterCount
import org.domaframework.doma.intellij.common.validation.result.ValidationResultFunctionCallParameterTypeMismatch

class MethodMatcher {
    data class MatchResult(
        val method: PsiMethod? = null,
        val validation: ValidationResult? = null,
    )

    companion object {
        fun findMatchingMethod(
            element: PsiElement,
            candidateMethods: List<PsiMethod>,
            actualParameterTypes: List<PsiType?>,
            actualParameterCount: Int,
            shortName: String = "",
        ): MatchResult {
            if (candidateMethods.isEmpty()) {
                return MatchResult(validation = null)
            }

            val methodsWithCorrectParameterCount = filterByParameterCount(candidateMethods, actualParameterCount)

            if (methodsWithCorrectParameterCount.isEmpty()) {
                return MatchResult(
                    validation =
                        ValidationResultFunctionCallParameterCount(
                            element,
                            shortName,
                            actualParameterCount,
                        ),
                )
            }

            val matchedMethod = findMethodByParameterTypes(methodsWithCorrectParameterCount, actualParameterTypes)

            return if (matchedMethod != null) {
                MatchResult(method = matchedMethod)
            } else {
                MatchResult(
                    validation =
                        ValidationResultFunctionCallParameterTypeMismatch(
                            element,
                            shortName,
                            candidateMethods,
                            actualParameterTypes,
                        ),
                )
            }
        }

        private fun filterByParameterCount(
            methods: List<PsiMethod>,
            expectedCount: Int,
        ): List<PsiMethod> = methods.filter { it.parameterList.parameters.size == expectedCount }

        private fun findMethodByParameterTypes(
            methods: List<PsiMethod>,
            actualParameterTypes: List<PsiType?>,
        ): PsiMethod? =
            methods.firstOrNull { method ->
                val methodParams = method.parameterList.parameters
                methodParams.zip(actualParameterTypes).all { (definedParam, actualType) ->
                    areTypesCompatible(definedParam.type, actualType)
                }
            }

        private fun areTypesCompatible(
            expectedType: PsiType,
            actualType: PsiType?,
        ): Boolean =
            expectedType == actualType || TypeUtil.sameTypeIgnoringBoxing(expectedType, actualType) ||
                actualType?.superTypes?.any { it == expectedType } == true
    }
}
