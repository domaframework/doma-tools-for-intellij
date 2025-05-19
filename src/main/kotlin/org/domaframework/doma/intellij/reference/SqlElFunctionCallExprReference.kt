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
package org.domaframework.doma.intellij.reference

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.setting.state.DomaToolsCustomFunctionSettings

class SqlElFunctionCallExprReference(
    element: PsiElement,
) : SqlElExprReference(element) {
    override fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val functionCallExpr = element.parent as? SqlElFunctionCallExpr ?: return null
        val variableName = functionCallExpr.elIdExpr.text ?: ""

        val project = element.project
        val expressionFunctionsInterface =
            ExpressionFunctionsHelper.setExpressionFunctionsInterface(project)
                ?: return null

        val setting = DomaToolsCustomFunctionSettings.getInstance(element.project)
        val customFunctionClassNames = setting.state.customFunctionClassNames
        val implementsClasses: MutableList<PsiClass> =
            customFunctionClassNames.mapNotNull { className ->
                val expressionFunction = project.getJavaClazz(className)
                if (ExpressionFunctionsHelper.isInheritor(expressionFunction)) {
                    expressionFunction
                } else {
                    null
                }
            } as MutableList<PsiClass>

        if (implementsClasses.isEmpty()) {
            implementsClasses.add(expressionFunctionsInterface)
        }

        var reference: PsiMethod? = null
        implementsClasses.forEach { clazz ->
            // TODO Type checking in parameters
            val methods = clazz.findMethodsByName(variableName, true).firstOrNull()
            if (methods != null) {
                reference = methods
            }
        }

        if (reference == null) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceCustomFunctions",
                "Reference",
                startTime,
            )
        }
        return reference
    }
}
