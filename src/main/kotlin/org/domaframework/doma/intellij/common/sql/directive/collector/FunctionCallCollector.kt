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
package org.domaframework.doma.intellij.common.sql.directive.collector

import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.util.SqlCompletionUtil.createMethodLookupElement
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.jetbrains.kotlin.idea.base.util.module

class FunctionCallCollector(
    private val file: PsiFile?,
    private val caretNextText: String,
    private val bind: String,
) : StaticDirectiveHandlerCollector() {
    public override fun collect(): List<LookupElement>? {
        val functions = mutableSetOf<PsiMethod>()
        val project = file?.project
        val module = file?.module ?: return null
        val isTest = CommonPathParameterUtil.isTest(module, file.virtualFile)

        val customFunctionClassName =
            DomaCompileConfigUtil.getConfigValue(
                module,
                isTest,
                "doma.expr.functions",
            )

        val expressionFunctionInterface =
            project?.let { ExpressionFunctionsHelper.setExpressionFunctionsInterface(it) }
                ?: return null

        val expressionClazz =
            customFunctionClassName?.let {
                if (it.isNotEmpty()) project.getJavaClazz(it) else null
            }
        if (expressionClazz != null &&
            ExpressionFunctionsHelper.isInheritor(expressionClazz)
        ) {
            val methods = expressionClazz.allMethods
            functions.addAll(
                methods.filter {
                    isPublicFunction(it)
                },
            )
        }

        functions.addAll(
            expressionFunctionInterface.allMethods.filter {
                isPublicFunction(it) && !functions.contains(it)
            },
        )

        return functions
            .filter {
                it.name.startsWith(bind.substringAfter("@"))
            }.map { m ->
                val parameters = m.parameterList.parameters.toList()
                LookupElementBuilder
                    .create(createMethodLookupElement(caretNextText, m))
                    .withPresentableText(m.name)
                    .withTailText(
                        "(${
                            parameters.joinToString(",") { p -> "${p.type.presentableText} ${p.name}" }
                        })",
                        true,
                    ).withTypeText(m.returnType?.presentableText ?: "void")
                    .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
            }
    }

    private fun isPublicFunction(method: PsiMethod): Boolean =
        !method.isConstructor &&
            method.hasModifierProperty(
                PsiModifier.PUBLIC,
            )
}
