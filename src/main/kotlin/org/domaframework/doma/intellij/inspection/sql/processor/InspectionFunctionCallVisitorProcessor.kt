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
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationInvalidFunctionCallResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.setting.state.DomaToolsCustomFunctionSettings

class InspectionFunctionCallVisitorProcessor(
    val shortName: String,
    private val element: SqlElFunctionCallExpr,
) : InspectionVisitorProcessor(shortName) {
    fun check(holder: ProblemsHolder) {
        val project = element.project
        val expressionHelper = ExpressionFunctionsHelper
        val expressionFunctionalInterface = expressionHelper.setExpressionFunctionsInterface(project)

        val functionName = element.elIdExpr
        val expressionFunctionSetting = DomaToolsCustomFunctionSettings.getInstance(project)
        val customFunctionClassNames = expressionFunctionSetting.state.customFunctionClassNames

        var methods: Array<out PsiMethod?> = emptyArray()
        customFunctionClassNames.takeWhile { clazz ->
            val expressionClazz = project.getJavaClazz(clazz)
            if (expressionClazz != null && expressionHelper.isInheritor(expressionClazz)) {
                methods = expressionClazz.findMethodsByName(functionName.text, true)
            }
            methods.isEmpty()
        }

        if (methods.isEmpty()) {
            methods = expressionFunctionalInterface?.findMethodsByName(functionName.text, true) ?: emptyArray()
        }

        if (methods.isEmpty()) {
            ValidationInvalidFunctionCallResult(
                functionName,
                shortName,
            ).highlightElement(holder)
        }
    }
}
