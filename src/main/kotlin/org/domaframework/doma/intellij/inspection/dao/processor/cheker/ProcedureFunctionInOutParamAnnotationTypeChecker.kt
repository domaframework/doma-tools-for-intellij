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
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodProcedureParamTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodProcedureParamsSupportGenericParamResult

class ProcedureFunctionInOutParamAnnotationTypeChecker(
    private val annotationType: ProcedureFunctionParamAnnotationType,
) : ProcedureFunctionParamAnnotationTypeChecker() {
    override fun checkParam(
        identifier: PsiElement,
        paramType: PsiType,
        project: Project,
        shortName: String,
        holder: ProblemsHolder,
    ) {
        // Check if the parameter type is a valid reference type
        if (!DomaClassName.REFERENCE.isTargetClassNameStartsWith(paramType.canonicalText)) {
            ValidationMethodProcedureParamTypeResult(
                identifier,
                shortName,
                annotationType,
            ).highlightElement(holder)
            return
        }

        // Check if the parameter type is a valid reference type with generic parameters
        val referenceParamType = (paramType as? PsiClassType)?.parameters?.firstOrNull()
        if (referenceParamType == null) {
            ValidationMethodProcedureParamsSupportGenericParamResult(
                identifier,
                shortName,
                "Unknown",
                annotationType.requireType,
            ).highlightElement(holder)
            return
        }

        if (checkParamType(referenceParamType, project)) return
        ValidationMethodProcedureParamsSupportGenericParamResult(
            identifier,
            shortName,
            referenceParamType.canonicalText,
            annotationType.requireType,
        ).highlightElement(holder)
    }
}
