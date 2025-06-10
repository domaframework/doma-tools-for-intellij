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
package org.domaframework.doma.intellij.inspection.dao.processor.cheker

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodProcedureParamTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodProcedureParamsSupportGenericParamResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.isDomain
import org.domaframework.doma.intellij.extension.psi.isEntity

class ProcedureFunctionResultSetParamAnnotationTypeChecker : ProcedureFunctionParamAnnotationTypeChecker() {
    override fun checkParamType(
        paramType: PsiType,
        project: Project,
    ): Boolean {
        if (PsiTypeChecker.isBaseClassType(paramType)) return true

        if (DomaClassName.isOptionalType(paramType.canonicalText)) {
            return true
        }

        if (DomaClassName.OPTIONAL.isTargetClassNameStartsWith(paramType.canonicalText)) {
            val paramClassType = paramType as? PsiClassType ?: return false
            val optionalParam = paramClassType.parameters.firstOrNull()
            return optionalParam?.let {
                val optionalParamClass = project.getJavaClazz(it.canonicalText)
                optionalParamClass?.isDomain() == true ||
                    optionalParamClass?.isEntity() == true ||
                    PsiTypeChecker.isBaseClassType(
                        it,
                    )
            } == true
        }

        val paramClass = project.getJavaClazz(paramType.canonicalText)
        return paramClass?.isDomain() == true
    }

    override fun checkParam(
        identifier: PsiElement,
        paramType: PsiType,
        project: Project,
        shortName: String,
        holder: ProblemsHolder,
    ) {
        val annotationType = ProcedureFunctionParamAnnotationType.ResultSet
        // Check if the parameter type is a valid List type
        if (!DomaClassName.LIST.isTargetClassNameStartsWith(paramType.canonicalText)) {
            ValidationMethodProcedureParamTypeResult(
                identifier,
                shortName,
                annotationType,
            ).highlightElement(holder)
            return
        }

        // Check if the parameter type is a valid List type with generic parameters
        val listParamType = (paramType as? PsiClassType)?.parameters?.firstOrNull()
        if (listParamType == null) {
            ValidationMethodProcedureParamsSupportGenericParamResult(
                identifier,
                shortName,
                "Unknown",
                annotationType.requireType,
            ).highlightElement(holder)
            return
        }

        val listCanonicalText = listParamType.canonicalText
        val result =
            ValidationMethodProcedureParamsSupportGenericParamResult(
                identifier,
                shortName,
                listCanonicalText,
                annotationType.requireType,
            )
        if (DomaClassName.MAP.isTargetClassNameStartsWith(listCanonicalText)) {
            val mapClassName = listCanonicalText.replace(" ", "")
            val mapExpectedType =
                DomaClassName.MAP
                    .getGenericParamCanonicalText(
                        DomaClassName.STRING.className,
                        DomaClassName.OBJECT.className,
                    ).replace(" ", "")
            if (mapClassName != mapExpectedType) {
                result.highlightElement(holder)
            }
            return
        }

        val paramClass = project.getJavaClazz(listParamType.canonicalText)

        if (checkParamType(listParamType, project) || paramClass?.isEntity() == true) return
        result.highlightElement(holder)
    }
}
