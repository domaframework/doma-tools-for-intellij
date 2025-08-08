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
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsSupportGenericParamResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodProcedureParamTypeResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.isDataType
import org.domaframework.doma.intellij.extension.psi.isDomain
import org.domaframework.doma.intellij.extension.psi.isEntity

class ProcedureFunctionResultSetParamAnnotationTypeChecker(
    psiDaoMethod: PsiDaoMethod,
) : ProcedureFunctionParamAnnotationTypeChecker(psiDaoMethod) {
    override fun checkParamType(paramType: PsiType): Boolean {
        if (PsiTypeChecker.isBaseClassType(paramType)) return true

        if (DomaClassName.isOptionalWrapperType(paramType.canonicalText)) {
            return true
        }

        if (DomaClassName.OPTIONAL.isTargetClassNameStartsWith(paramType.canonicalText)) {
            val paramClassType = paramType as? PsiClassType ?: return false
            val optionalParam = paramClassType.parameters.firstOrNull()
            return optionalParam?.let {
                val optionalParamClass = project.getJavaClazz(it)
                optionalParamClass?.isDomain() == true ||
                    optionalParamClass?.isEntity() == true ||
                    optionalParamClass?.isDataType() == true ||
                    PsiTypeChecker.isBaseClassType(
                        it,
                    )
            } == true
        }

        val paramClass = project.getJavaClazz(paramType)
        return paramClass?.isDomain() == true || paramClass?.isDataType() == true
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
            ValidationMethodParamsSupportGenericParamResult(
                identifier,
                shortName,
                "Unknown",
                annotationType.requireType,
            ).highlightElement(holder)
            return
        }

        val listCanonicalText = listParamType.canonicalText
        val result =
            ValidationMethodParamsSupportGenericParamResult(
                identifier,
                shortName,
                listCanonicalText,
                annotationType.requireType,
            )
        if (DomaClassName.MAP.isTargetClassNameStartsWith(listCanonicalText)) {
            if (!checkMapType(listCanonicalText)) result.highlightElement(holder)
            return
        }

        val paramClass = project.getJavaClazz(listParamType)

        if (checkParamType(listParamType) || paramClass?.isEntity() == true || paramClass?.isDataType() == true) return
        result.highlightElement(holder)
    }
}
