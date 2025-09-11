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
package org.domaframework.doma.intellij.inspection.sql.processor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.util.MethodMatcher
import org.domaframework.doma.intellij.common.validation.result.ValidationInvalidExpressionFunctionsResult
import org.domaframework.doma.intellij.common.validation.result.ValidationInvalidFunctionCallResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.jetbrains.kotlin.idea.util.projectStructure.module

class InspectionFunctionCallVisitorProcessor(
    val shortName: String,
    private val element: SqlElFunctionCallExpr,
) : InspectionVisitorProcessor() {
    fun check(holder: ProblemsHolder) {
        val result: ValidationResult? = getFunctionCallValidationResult()
        result?.highlightElement(holder)
    }

    fun getFunctionCallType(): PsiMethod? {
        val result = getFunctionCall()
        return result
    }

    private fun getFunctionCallValidationResult(): ValidationResult? {
        val project = element.project
        val module = element.module ?: return null
        val expressionHelper = ExpressionFunctionsHelper
        val expressionFunctionalInterface = expressionHelper.setExpressionFunctionsInterface(project)
        val functionName = element.elIdExpr
        val isTest = CommonPathParameterUtil.isTest(module, element.containingFile.virtualFile)
        val customFunctionClassName = DomaCompileConfigUtil.getConfigValue(module, isTest, "doma.expr.functions")

        var methods: List<PsiMethod?>
        val expressionClazz = customFunctionClassName?.let { project.getJavaClazz(it) }

        if (customFunctionClassName?.isEmpty() == true) {
            return ValidationInvalidExpressionFunctionsResult(
                functionName,
                shortName,
                customFunctionClassName,
            )
        }

        val parentClass = expressionClazz ?: expressionFunctionalInterface
        var result: ValidationResult? = null
        if (parentClass != null) {
            if (expressionHelper.isInheritor(parentClass) || parentClass == expressionFunctionalInterface) {
                methods = parentClass.findMethodsByName(functionName.text, true).mapNotNull { it }
                if (methods.isEmpty()) {
                    return ValidationInvalidFunctionCallResult(
                        element.elIdExpr,
                        parentClass.psiClassType.canonicalText,
                        shortName,
                    )
                }
                result = null
                val matchResult = checkParamTypeMatch(parentClass)
                if (matchResult.method == null) {
                    result = matchResult.validation
                }
            } else {
                result =
                    ValidationInvalidExpressionFunctionsResult(
                        functionName,
                        shortName,
                        customFunctionClassName ?: "",
                    )
            }
        }
        if (result != null &&
            isImplementExpressionFunction(
                parentClass,
                expressionFunctionalInterface,
                expressionHelper,
            )
        ) {
            result =
                ValidationInvalidExpressionFunctionsResult(
                    functionName,
                    shortName,
                    customFunctionClassName ?: "",
                )
        }
        return result
    }

    private fun getFunctionCall(): PsiMethod? {
        val project = element.project
        val module = element.module ?: return null
        val expressionHelper = ExpressionFunctionsHelper
        val expressionFunctionalInterface = expressionHelper.setExpressionFunctionsInterface(project)
        val functionName = element.elIdExpr
        val isTest = CommonPathParameterUtil.isTest(module, element.containingFile.virtualFile)
        val customFunctionClassName = DomaCompileConfigUtil.getConfigValue(module, isTest, "doma.expr.functions")

        if (customFunctionClassName?.isEmpty() == true) {
            return null
        }

        var methods: List<PsiMethod?>
        val expressionClazz = customFunctionClassName?.let { project.getJavaClazz(it) }
        val parentClass = expressionClazz ?: expressionFunctionalInterface
        var result: PsiMethod? = null
        if (parentClass != null) {
            if (expressionHelper.isInheritor(parentClass) || parentClass == expressionFunctionalInterface) {
                methods = parentClass.findMethodsByName(functionName.text, true).mapNotNull { it }
                if (methods.isEmpty()) {
                    return null
                }
                result = getParamTypeMatch(parentClass)
            }
        }
        if (isImplementExpressionFunction(
                parentClass,
                expressionFunctionalInterface,
                expressionHelper,
            )
        ) {
            return null
        }

        return result
    }

    /**
     * Not inheriting from ExpressionFunctions.
     */
    private fun isImplementExpressionFunction(
        parentClass: PsiClass?,
        expressionFunctionalInterface: PsiClass?,
        expressionHelper: ExpressionFunctionsHelper.Companion,
    ): Boolean = parentClass != expressionFunctionalInterface && !expressionHelper.isInheritor(parentClass)

    private fun checkParamTypeMatch(parentPsiClass: PsiClass): MethodMatcher.MatchResult {
        val psiParentClassExpressionClazz = PsiParentClass(parentPsiClass.psiClassType)
        return psiParentClassExpressionClazz.findMethod(element, shortName)
    }

    private fun getParamTypeMatch(parentPsiClass: PsiClass): PsiMethod? {
        val psiParentClassExpressionClazz = PsiParentClass(parentPsiClass.psiClassType)
        val methodResult = psiParentClassExpressionClazz.findMethod(element, shortName)
        return methodResult.method
    }
}
