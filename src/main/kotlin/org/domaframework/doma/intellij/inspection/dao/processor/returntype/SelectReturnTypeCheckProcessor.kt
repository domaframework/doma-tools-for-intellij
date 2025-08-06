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
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodInvalidReturnTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodSelectStrategyReturnTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.inspection.dao.processor.StrategyParam

class SelectReturnTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    override fun checkReturnType(): ValidationResult? {
        val returnType = method.returnType
        if (returnType == null) return null

        val methodAnnotation = getAnnotation(psiDaoMethod.daoType.fqdn) ?: return null
        val strategyOpt = getDaoPsiReferenceOption(methodAnnotation, "strategy")
        val strategyParam =
            if (strategyOpt == null) {
                null
            } else {
                StrategyParam(
                    strategyOpt.name,
                    strategyOpt.type.canonicalText,
                )
            }
        strategyParam?.let { strategy ->
            if (strategy.isStream()) {
                return checkStream()
            }

            if (strategy.isCollect()) {
                return checkCollect()
            }
        }

        if (DomaClassName.JAVA_STREAM.isTargetClassNameStartsWith(returnType.canonicalText)) return null

        val checkType =
            if (DomaClassName.LIST.isTargetClassNameStartsWith(returnType.canonicalText)) {
                val listClassType = returnType as? PsiClassType
                listClassType?.parameters?.firstOrNull()
            } else {
                returnType
            }

        val unwrappedType = TypeUtil.unwrapOptional(checkType)
        return checkReturnTypeParam(unwrappedType)
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
        if (checkType == null || checkType == PsiTypes.voidType()) return result

        if (TypeUtil.isBaseOrOptionalWrapper(checkType)) {
            return null
        }

        if (DomaClassName.MAP.isTargetClassNameStartsWith(checkTypeCanonicalText)) {
            return if (!TypeUtil.isValidMapType(checkType)) {
                result
            } else {
                null
            }
        }

        if (TypeUtil.isDomain(checkType, project) || TypeUtil.isEntity(checkType, project) || TypeUtil.isDataType(checkType, project)) {
            return null
        }

        return result
    }

    private fun checkStream(): ValidationResult? {
        val targetType = DomaClassName.JAVA_FUNCTION
        return checkParamTypeResult(targetType, 1)
    }

    private fun checkCollect(): ValidationResult? {
        val targetType = DomaClassName.JAVA_COLLECTOR
        return checkParamTypeResult(targetType, 2)
    }

    override fun checkParamTypeResult(
        targetType: DomaClassName,
        resultIndex: Int,
    ): ValidationResult? {
        val resultParam =
            getMethodParamTargetArgByIndex(targetType, resultIndex)
                ?: return null
        if (resultParam.canonicalText == method.returnType?.canonicalText) return null

        return ValidationMethodSelectStrategyReturnTypeResult(
            method.nameIdentifier,
            shortName,
            resultParam.canonicalText,
            targetType.className,
        )
    }
}
