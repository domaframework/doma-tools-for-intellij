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

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.setting.state.DomaToolsCustomFunctionSettings
import kotlin.collections.mutableSetOf

class StaticBuildFunctionCollector(
    private val project: Project,
    private val bind: String,
) : StaticDirectiveHandlerCollector() {
    public override fun collect(): List<LookupElement>? {
        var functions = mutableSetOf<PsiMethod>()
        val setting = DomaToolsCustomFunctionSettings.getInstance(project)
        val state = setting.state
        val customFunctions = state.customFunctionClassNames

        val expressionFunctionInterface =
            ExpressionFunctionsHelper.setExpressionFunctionsInterface(project)
                ?: return null

        customFunctions.forEach { function ->
            val expressionClazz = project.getJavaClazz(function)
            if (expressionClazz != null &&
                ExpressionFunctionsHelper.isInheritor(expressionClazz)
            ) {
                val psiParent = PsiParentClass(expressionClazz.psiClassType)
                psiParent.searchMethod("")?.let { methods ->
                    functions.addAll(
                        methods.filter {
                            isPublicFunction(it)
                        },
                    )
                }
            }
        }

        if (functions.isEmpty()) {
            functions.addAll(
                expressionFunctionInterface.allMethods.filter {
                    isPublicFunction(it)
                },
            )
        }

        return functions
            .filter {
                it.name.startsWith(bind.substringAfter("@"))
            }.map {
                val parameters = it.parameterList.parameters.toList()
                LookupElementBuilder
                    .create("${it.name}()")
                    .withPresentableText(it.name)
                    .withTailText(
                        "(${
                            parameters.joinToString(",") { "${it.type.presentableText} ${it.name}" }
                        })",
                        true,
                    ).withTypeText(it.returnType?.presentableText ?: "void")
            }
    }

    private fun isPublicFunction(method: PsiMethod): Boolean =
        !method.isConstructor &&
            method.hasModifierProperty(
                PsiModifier.PUBLIC,
            )
}
