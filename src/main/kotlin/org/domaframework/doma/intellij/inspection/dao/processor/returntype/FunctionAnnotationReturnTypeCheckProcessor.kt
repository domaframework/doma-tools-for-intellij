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
package org.domaframework.doma.intellij.inspection.dao.processor.returntype

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodInvalidReturnTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.isDomain
import org.domaframework.doma.intellij.extension.psi.isEntity

class FunctionAnnotationReturnTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    override fun checkReturnType(): ValidationResult? {
        val returnType = method.returnType
        if (returnType == null || returnType == PsiTypes.voidType()) return null

        val checkType =
            if (DomaClassName.LIST.isTargetClassNameStartsWith(returnType.canonicalText)) {
                val listClassType = returnType as? PsiClassType
                listClassType?.parameters?.firstOrNull()
            } else {
                returnType
            }

        val checkTypeCanonicalText = checkType?.canonicalText ?: "Unknown"
        if (DomaClassName.OPTIONAL.isTargetClassNameStartsWith(checkTypeCanonicalText)) {
            val optionalClassType = checkType as? PsiClassType
            val optionalParamType = optionalClassType?.parameters?.firstOrNull()
            return checkReturnTypeParam(optionalParamType)
        }

        return checkReturnTypeParam(checkType)
    }

    private fun checkReturnTypeParam(checkType: PsiType?): ValidationResult? {
        val identifier = method.nameIdentifier ?: return null
        val checkTypeCanonicalText = checkType?.canonicalText ?: "Unknown"
        val result =
            ValidationMethodInvalidReturnTypeResult(
                identifier,
                shortName,
                checkTypeCanonicalText,
            )
        if (checkType == null) return result

        if (DomaClassName.isOptionalWrapperType(checkTypeCanonicalText) ||
            PsiTypeChecker.isBaseClassType(checkType)
        ) {
            return null
        }

        if (DomaClassName.MAP.isTargetClassNameStartsWith(checkTypeCanonicalText)) {
            return if (!checkMapType(checkTypeCanonicalText)) {
                result
            } else {
                null
            }
        }

        val checkTypeClass = project.getJavaClazz(checkType.canonicalText)
        if (checkTypeClass != null &&
            (
                checkTypeClass.isDomain() ||
                    checkTypeClass.isEntity()
            )
        ) {
            return null
        }

        return result
    }
}
