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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil.EXPRESSION_FUNCTIONS_NAME
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.validation.result.ValidationInvalidExpressionFunctionsResult
import org.domaframework.doma.intellij.common.validation.result.ValidationInvalidFunctionCallResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResultFunctionCallParameterCount
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.jetbrains.kotlin.idea.util.projectStructure.module

class InspectionFunctionCallVisitorProcessor(
    val shortName: String,
    private val element: SqlElFunctionCallExpr,
) : InspectionVisitorProcessor(shortName) {
    fun check(holder: ProblemsHolder) {
        val project = element.project
        val module = element.module ?: return
        val expressionHelper = ExpressionFunctionsHelper
        val expressionFunctionalInterface = expressionHelper.setExpressionFunctionsInterface(project)
        val functionName = element.elIdExpr

        val isTest = CommonPathParameterUtil.isTest(module, element.containingFile.virtualFile)
        val customFunctionClassName = DomaCompileConfigUtil.getConfigValue(module, isTest, "doma.expr.functions")

        var result: ValidationResult? = null
        var methods: Array<out PsiMethod?>
        val expressionClazz = customFunctionClassName?.let { project.getJavaClazz(it) }
        val parentPsiClass = expressionClazz ?: expressionFunctionalInterface
        if (parentPsiClass != null) {
            if (expressionHelper.isInheritor(parentPsiClass)) {
                methods = parentPsiClass.findMethodsByName(functionName.text, true)
                if (methods.isEmpty()) {
                    errorHighlight(holder, functionName, customFunctionClassName ?: EXPRESSION_FUNCTIONS_NAME)
                    return
                }
                result = checkMethodParamCount(functionName, methods)
                result = checkParamTypeMatch(parentPsiClass, result)

            } else {
                // Not inheriting from ExpressionFunctions.
                result =
                    ValidationInvalidExpressionFunctionsResult(
                        functionName,
                        shortName,
                    )
            }
        }
        result?.highlightElement(holder)
    }

    private fun checkParamTypeMatch(
        parentPsiClass: PsiClass,
        result: ValidationResult?
    ): ValidationResult? {
        var result1 = result
        val psiParentClassExpressionClazz = PsiParentClass(parentPsiClass.psiClassType)
        val methodResult = psiParentClassExpressionClazz.findMethod(element, shortName)
        if (methodResult.method == null) {
            result1 = methodResult.validation
        }
        return result1
    }

    private fun checkMethodParamCount(
        functionName: SqlElIdExpr,
        methods: Array<out PsiMethod?>,
    ): ValidationResult? {
        val paramExpr = PsiTreeUtil.nextLeaf(functionName)?.parent as? SqlElParameters ?: return null
        val actualCount = paramExpr.elExprList.size
        methods.mapNotNull { it }.filter { m ->
            val exactCount = m.parameterList.parametersCount
            exactCount == actualCount
        }
        if (methods.isEmpty()) {
            return ValidationResultFunctionCallParameterCount(
                element,
                shortName,
                actualCount,
            )
        }
        return null
    }

    private fun errorHighlight(holder: ProblemsHolder, functionName: PsiElement, customFunctionClassName:String) {
        ValidationInvalidFunctionCallResult(
            functionName,
            customFunctionClassName,
            shortName,
        ).highlightElement(holder)
    }
}
