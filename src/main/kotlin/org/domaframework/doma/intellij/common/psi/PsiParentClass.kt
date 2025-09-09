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
package org.domaframework.doma.intellij.common.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.domaframework.doma.intellij.common.util.MethodMatcher
import org.domaframework.doma.intellij.extension.expr.extractParameterTypes

/**
 * When parsing a field access element with SQL,
 * manage the reference class information of the previous element
 */
open class PsiParentClass(
    val type: PsiType,
) {
    var clazz: PsiClass? = psiClass()

    private fun psiClass() = PsiTypesUtil.getPsiClass(type)

    private val fields: Array<PsiField>? = clazz?.allFields
    private val methods: Array<PsiMethod>? = clazz?.allMethods

    private fun getMethods(): List<PsiMethod>? =
        methods?.filter { m ->
            m.hasModifierProperty(PsiModifier.PUBLIC)
        }

    fun findField(fieldName: String): PsiField? =
        fields?.firstOrNull { f ->
            f.name == fieldName &&
                PropertyModifyUtil.filterPrivateField(f, type)
        }

    fun searchField(fieldName: String): List<PsiField>? =
        fields?.filter { f ->
            f.name.startsWith(fieldName) &&
                PropertyModifyUtil.filterPrivateField(f, type)
        }

    fun findMethod(methodName: String): PsiMethod? =
        getMethods()
            ?.filter { m ->
                m.hasModifierProperty(PsiModifier.PUBLIC)
            }?.firstOrNull { m ->
                m.name.substringBefore("(") == methodName.substringBefore("(")
            }

    fun findMethod(
        methodExpr: PsiElement,
        shortName: String = "",
    ): MethodMatcher.MatchResult {
        val context = MethodParamContext.of(methodExpr)
        val methods = findMethods(context.methodIdExp.text)
        if (context.methodParams == null) return MethodMatcher.MatchResult(validation = null)

        val actualCount = context.methodParams.elExprList.size
        val matchCountMethods =
            methods.filter { m ->
                val methodParams = m.parameterList.parameters
                return@filter actualCount == methodParams.size
            }
        val paramTypes = context.methodParams.extractParameterTypes(PsiManager.getInstance(methodExpr.project))
        val matchResult =
            MethodMatcher.findMatchingMethod(
                context.methodIdExp,
                matchCountMethods,
                paramTypes,
                actualCount,
                shortName,
            )

        return matchResult
    }

    fun findMethods(methodName: String): List<PsiMethod> =
        getMethods()
            ?.filter { m ->
                m.hasModifierProperty(PsiModifier.PUBLIC) && m.name.substringBefore("(") == methodName.substringBefore("(")
            } ?: emptyList()

    fun searchMethod(methodName: String): List<PsiMethod>? =
        getMethods()?.filter { m ->
            m.name.substringBefore("(").startsWith(methodName.substringBefore("(")) &&
                m.hasModifierProperty(PsiModifier.PUBLIC)
        }

    fun searchStaticField(fieldName: String): List<PsiField>? =
        fields
            ?.filter { f ->
                f.hasModifierProperty(PsiModifier.STATIC) &&
                    f.name.startsWith(fieldName) &&
                    PropertyModifyUtil.filterPrivateField(f, type)
            }

    fun searchStaticMethod(methodName: String): List<PsiMethod>? =
        methods?.filter { m ->
            m.hasModifierProperty(PsiModifier.STATIC) &&
                m.hasModifierProperty(PsiModifier.PUBLIC) &&
                m.name.substringBefore("(").startsWith(methodName.substringBefore("("))
        }

    fun findStaticMethods(methodName: String): List<PsiMethod> =
        getMethods()
            ?.filter { m ->
                m.hasModifierProperty(PsiModifier.STATIC) &&
                    m.hasModifierProperty(PsiModifier.PUBLIC) && m.name.substringBefore("(") == methodName.substringBefore("(")
            } ?: emptyList()
}
