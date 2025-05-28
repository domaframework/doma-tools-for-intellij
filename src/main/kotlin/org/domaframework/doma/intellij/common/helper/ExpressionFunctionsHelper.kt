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
package org.domaframework.doma.intellij.common.helper

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.domaframework.doma.intellij.common.config.DomaCompileConfigUtil.EXPRESSION_FUNCTIONS_NAME
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType

class ExpressionFunctionsHelper {
    companion object {
        var expressionFunction: PsiClass? = null

        fun setExpressionFunctionsInterface(project: Project): PsiClass? {
            val expressionFunctionsClass =
                project.getJavaClazz(EXPRESSION_FUNCTIONS_NAME)
            if (expressionFunctionsClass != null) {
                expressionFunction = expressionFunctionsClass
            }
            return expressionFunction
        }

        fun isInheritor(expressionClazz: PsiClass?): Boolean {
            val project = expressionClazz?.project

            expressionFunction?.let { functionInterface ->
                expressionClazz?.let {
                    if (expressionClazz.isInheritor(functionInterface, true)) return true

                    val parentType =
                        expressionClazz.superTypes.firstOrNull()?.canonicalText
                            ?: expressionClazz.psiClassType.canonicalText
                    return project?.let {
                        expressionClazz.psiClassType.canonicalText
                        project
                            .getJavaClazz(parentType)
                            ?.isInheritor(functionInterface, true) == true
                    } == true
                }
            }
            return false
        }
    }
}
