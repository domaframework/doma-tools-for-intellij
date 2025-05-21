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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.jetbrains.kotlin.idea.base.util.module

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
        val module = file.module
        val expressionFunctionsInterface =
            ExpressionFunctionsHelper.setExpressionFunctionsInterface(project)
                ?: return null

        val resourcePaths =
            module?.let {
                CommonPathParameterUtil
                    .getResources(it, file.virtualFile)
            } ?: emptyList()

        val customFunctionClassName = DomaCompileConfigUtil.getConfigValue(project, resourcePaths, "doma.expr.functions")

        val implementsClass =
            if (customFunctionClassName != null) {
                val expressionFunction = project.getJavaClazz(customFunctionClassName)
                if (ExpressionFunctionsHelper.isInheritor(expressionFunction)) {
                    expressionFunction
                } else {
                    expressionFunctionsInterface
                }
            } else {
                expressionFunctionsInterface
            }

        var reference: PsiMethod? = null
        val methods = implementsClass?.findMethodsByName(variableName, true)?.firstOrNull()
        if (methods != null) {
            reference = methods
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
