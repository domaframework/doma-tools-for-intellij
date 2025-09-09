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
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil.EXPRESSION_FUNCTIONS_NAME
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.validation.result.ValidationInvalidExpressionFunctionsResult
import org.domaframework.doma.intellij.common.validation.result.ValidationInvalidFunctionCallResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.jetbrains.kotlin.idea.util.projectStructure.module

class InspectionFunctionCallVisitorProcessor(
    val shortName: String,
    private val element: SqlElFunctionCallExpr,
) : InspectionVisitorProcessor() {
    fun check(holder: ProblemsHolder) {
        val project = element.project
        val module = element.module ?: return
        val expressionHelper = ExpressionFunctionsHelper
        val expressionFunctionalInterface = expressionHelper.setExpressionFunctionsInterface(project)
        val functionName = element.elIdExpr
        val isTest = CommonPathParameterUtil.isTest(module, element.containingFile.virtualFile)
        val customFunctionClassName = DomaCompileConfigUtil.getConfigValue(module, isTest, "doma.expr.functions")

        if(customFunctionClassName?.isEmpty() == true){
            ValidationInvalidExpressionFunctionsResult(
                functionName,
                shortName,
            ).highlightElement(holder)
            return
        }

        var methods: List<PsiMethod?>
        val expressionClazz = customFunctionClassName?.let { project.getJavaClazz(it) }
        val parentClass = expressionClazz ?: expressionFunctionalInterface
        var result: ValidationResult? =
            ValidationInvalidFunctionCallResult(
                functionName,
                parentClass?.psiClassType?.canonicalText ?: EXPRESSION_FUNCTIONS_NAME,
                shortName,
            )
        if (parentClass != null) {
            if (expressionHelper.isInheritor(parentClass) || parentClass == expressionFunctionalInterface) {
                methods = parentClass.findMethodsByName(functionName.text, true).mapNotNull { it }
                if (methods.isEmpty()) {
                    handleNotFoundMethodError(
                        result,
                        parentClass,
                        expressionFunctionalInterface,
                        expressionHelper,
                        functionName,
                    )?.highlightElement(holder)
                    return
                } else {
                    result = null
                }
                result = checkParamTypeMatch(parentClass, result)
            }
        }
        result =
            handleNotFoundMethodError(
                result,
                parentClass,
                expressionFunctionalInterface,
                expressionHelper,
                functionName,
            )
        result?.highlightElement(holder)
    }

    /**
     * Not inheriting from ExpressionFunctions.
     */
    private fun handleNotFoundMethodError(
        result: ValidationResult?,
        parentClass: PsiClass?,
        expressionFunctionalInterface: PsiClass?,
        expressionHelper: ExpressionFunctionsHelper.Companion,
        functionName: SqlElIdExpr,
    ): ValidationResult? {
        var result1 = result
        if (result1 != null && parentClass != expressionFunctionalInterface && !expressionHelper.isInheritor(parentClass)) {
            result1 =
                ValidationInvalidExpressionFunctionsResult(
                    functionName,
                    shortName,
                )
        }
        return result1
    }

    private fun checkParamTypeMatch(
        parentPsiClass: PsiClass,
        result: ValidationResult?,
    ): ValidationResult? {
        var result1 = result
        val psiParentClassExpressionClazz = PsiParentClass(parentPsiClass.psiClassType)
        val methodResult = psiParentClassExpressionClazz.findMethod(element, shortName)
        if (methodResult.method == null) {
            result1 = methodResult.validation
        }
        return result1
    }
}
