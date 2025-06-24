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
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodInvalidReturnTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodSelectStrategyReturnTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.isDomain
import org.domaframework.doma.intellij.extension.psi.isEntity
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
            (checkTypeClass.isDomain() || checkTypeClass.isEntity())
        ) {
            return null
        }

        return result
    }

    private fun checkStream(): ValidationResult? {
        val function =
            getMethodParamTargetType(DomaClassName.JAVA_FUNCTION.className) ?: return null

        val functionClassType = (function.type as? PsiClassType)
        val functionParams = functionClassType?.parameters ?: return null
        if (functionParams.size < 2) return null
        val functionResultParam = functionParams[1] ?: return null

        if (functionResultParam == method.returnType) return null
        return ValidationMethodSelectStrategyReturnTypeResult(
            method.nameIdentifier,
            shortName,
            DomaClassName.JAVA_FUNCTION.className,
        )
    }

    private fun checkCollect(): ValidationResult? {
        val collection = getMethodParamTargetType(DomaClassName.JAVA_COLLECTOR.className) ?: return null
        val collectorParamClassType = (collection.type as? PsiClassType)
        val collectorParams = collectorParamClassType?.parameters ?: return null
        if (collectorParams.size < 3) return null

        val collectorTargetParam = collectorParams[2]
        if (collectorTargetParam == method.returnType) return null

        return ValidationMethodSelectStrategyReturnTypeResult(
            method.nameIdentifier,
            shortName,
            DomaClassName.JAVA_COLLECTOR.className,
        )
    }
}
