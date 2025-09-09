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
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.jetbrains.kotlin.idea.base.util.module

class SqlElFunctionCallExprReference(
    element: PsiElement,
) : SqlElExprReference(element) {
    override fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val project = element.project
        val module = file.module ?: return null
        val expressionFunctionsInterface =
            ExpressionFunctionsHelper.setExpressionFunctionsInterface(project)
                ?: return null

        val isTest =
            CommonPathParameterUtil
                .isTest(module, file.virtualFile)
        val customFunctionClassName = DomaCompileConfigUtil.getConfigValue(module, isTest, "doma.expr.functions")

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

        val reference: PsiMethod? = implementsClass?.let { imp ->
            val psiParentClass = PsiParentClass(imp.psiClassType)
            psiParentClass.findMethod(element).method
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
