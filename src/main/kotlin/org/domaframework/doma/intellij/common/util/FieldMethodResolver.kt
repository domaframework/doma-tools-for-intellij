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
package org.domaframework.doma.intellij.common.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.validation.result.ValidationNotFoundStaticPropertyResult
import org.domaframework.doma.intellij.common.validation.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.extractParameterTypes
import org.domaframework.doma.intellij.psi.SqlElParameters

class FieldMethodResolver {
    data class ResolveContext(
        var parent: PsiParentClass,
        var parentListBaseType: PsiType?,
        var nestIndex: Int,
        var completeResult: ValidationCompleteResult? = null,
        var validationResult: ValidationResult? = null,
    )

    data class ResolveResult(
        val type: PsiParentClass? = null,
        val validation: ValidationResult? = null,
    )

    companion object {
        fun resolveField(
            context: ResolveContext,
            fieldName: String,
            project: Project,
        ): PsiParentClass? {
            val field = context.parent.findField(fieldName) ?: return null
            val convertedType = PsiClassTypeUtil.convertOptionalType(field.type, project)

            val resolvedType =
                context.parentListBaseType?.let {
                    PsiClassTypeUtil.getParameterType(project, convertedType, it, context.nestIndex)
                } ?: convertedType

            updateContextForIterableType(context, resolvedType, project)
            return PsiParentClass(resolvedType)
        }

        /**
         * Retrieve, from the defined methods with that name, those whose parameter count and parameter types match.
         */
        fun resolveMethod(
            context: ResolveContext,
            element: PsiElement,
            methodName: String,
            project: Project,
            shortName: String = "",
        ): ResolveResult {
            val candidateMethods = context.parent.findMethods(methodName)
            if (candidateMethods.isEmpty()) {
                return ResolveResult(
                    validation = ValidationPropertyResult(element, context.parent, shortName),
                )
            }

            val paramExpr = PsiTreeUtil.nextLeaf(element)?.parent as? SqlElParameters ?: return ResolveResult()

            return resolveMethodWithParameters(
                context,
                element,
                candidateMethods,
                paramExpr,
                project,
                shortName,
            )
        }

        fun resolveStaticMethod(
            context: ResolveContext,
            element: PsiElement,
            parent: PsiParentClass,
            methodName: String,
            parametersExpr: SqlElParameters?,
            project: Project,
            shortName: String = "",
        ): ResolveResult {
            val candidateMethods = parent.findStaticMethods(methodName)

            if (parametersExpr == null) {
                return ResolveResult()
            }

            if (candidateMethods.isEmpty()) {
                return ResolveResult(
                    validation = ValidationNotFoundStaticPropertyResult(element, parent.type.canonicalText, shortName),
                )
            }

            val paramTypes =
                parametersExpr.extractParameterTypes(
                    PsiManager.getInstance(project),
                )

            val matchResult =
                MethodMatcher.findMatchingMethod(
                    element,
                    candidateMethods,
                    paramTypes,
                    parametersExpr.elExprList.size,
                    shortName,
                )

            fun result(): ResolveResult {
                if (matchResult.validation != null) {
                    context.validationResult = matchResult.validation
                    return ResolveResult(validation = matchResult.validation)
                }

                val returnType = matchResult.method?.returnType ?: return ResolveResult()

                val convertedType = PsiClassTypeUtil.convertOptionalType(returnType, project)
                val resolvedType =
                    context.parentListBaseType?.let {
                        PsiClassTypeUtil.getParameterType(project, convertedType, it, context.nestIndex)
                    } ?: convertedType

                updateContextForIterableType(context, resolvedType, project)
                return ResolveResult(type = PsiParentClass(resolvedType))
            }

            return result()
        }

        private fun resolveMethodWithParameters(
            context: ResolveContext,
            element: PsiElement,
            candidateMethods: List<PsiMethod>,
            paramExpr: SqlElParameters,
            project: Project,
            shortName: String,
        ): ResolveResult {
            val paramTypes =
                paramExpr.extractParameterTypes(
                    PsiManager.getInstance(project),
                )

            val matchResult =
                MethodMatcher.findMatchingMethod(
                    element,
                    candidateMethods,
                    paramTypes,
                    paramExpr.elExprList.size,
                    shortName,
                )

            fun result(): ResolveResult {
                if (matchResult.validation != null) {
                    context.validationResult = matchResult.validation
                    return ResolveResult(validation = matchResult.validation)
                }

                val returnType = matchResult.method?.returnType ?: return ResolveResult()

                val convertedType = PsiClassTypeUtil.convertOptionalType(returnType, project)
                val resolvedType =
                    context.parentListBaseType?.let {
                        PsiClassTypeUtil.getParameterType(project, convertedType, it, context.nestIndex)
                    } ?: convertedType

                updateContextForIterableType(context, resolvedType, project)
                return ResolveResult(type = PsiParentClass(resolvedType))
            }

            return result()
        }

        private fun updateContextForIterableType(
            context: ResolveContext,
            type: PsiType,
            project: Project,
        ) {
            val classType = type as? PsiClassType
            if (classType != null && PsiClassTypeUtil.isIterableType(classType, project)) {
                context.parentListBaseType = PsiClassTypeUtil.convertOptionalType(type, project)
                context.nestIndex = 0
            }
        }
    }
}
