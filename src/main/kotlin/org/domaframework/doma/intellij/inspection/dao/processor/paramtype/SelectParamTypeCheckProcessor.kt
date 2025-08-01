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
package org.domaframework.doma.intellij.inspection.dao.processor.paramtype

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodNotSelectStreamParamResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsSupportGenericParamResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodSelectStrategyParamResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.getSuperClassType
import org.domaframework.doma.intellij.extension.psi.isDataType
import org.domaframework.doma.intellij.extension.psi.isDomain
import org.domaframework.doma.intellij.extension.psi.isEntity
import org.domaframework.doma.intellij.inspection.dao.processor.StrategyParam

class SelectParamTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    override fun checkParamType(paramType: PsiType): Boolean {
        if (PsiTypeChecker.isBaseClassType(paramType)) return true

        if (DomaClassName.isOptionalWrapperType(paramType.canonicalText)) {
            return true
        }

        if (DomaClassName.OPTIONAL.isTargetClassNameStartsWith(paramType.canonicalText)) {
            val paramClassType = paramType as? PsiClassType ?: return false
            val optionalParam = paramClassType.parameters.firstOrNull()
            return optionalParam?.let {
                val optionalParamClass = project.getJavaClazz(it.canonicalText)
                optionalParamClass?.isDomain() == true ||
                    PsiTypeChecker.isBaseClassType(it) ||
                    optionalParamClass?.isEntity() == true ||
                    optionalParamClass?.isDataType() == true
            } == true
        }

        val paramClass = project.getJavaClazz(paramType.canonicalText)
        return paramClass?.isDomain() == true || paramClass?.isEntity() == true || paramClass?.isDataType() == true
    }

    override fun checkParams(holder: ProblemsHolder) {
        val methodAnnotationFqdn = psiDaoMethod.daoType.fqdn
        val methodAnnotation = getAnnotation(methodAnnotationFqdn) ?: return
        val strategyOption = getDaoPsiReferenceOption(methodAnnotation, "strategy")
        val strategyParam =
            if (strategyOption == null) {
                null
            } else {
                StrategyParam(
                    strategyOption.name,
                    strategyOption.type.canonicalText,
                )
            }
        if (strategyParam == null) {
            checkStrategyOption(holder)
            return
        }

        if (strategyParam.isStream()) {
            checkStream(holder)
        }

        if (strategyParam.isCollect()) {
            checkCollect(holder)
        }
    }

    private fun checkStream(holder: ProblemsHolder) {
        val stream = DomaClassName.JAVA_STREAM.className
        val result =
            ValidationMethodSelectStrategyParamResult(
                method.nameIdentifier,
                shortName,
                "STREAM",
                DomaClassName.JAVA_FUNCTION.className,
            )

        val function = getMethodParamTargetType(DomaClassName.JAVA_FUNCTION.className)
        if (function == null) {
            result.highlightElement(holder)
            return
        }

        val identifier = function.nameIdentifier ?: return

        // Check if the first parameter of the function is a stream type
        val targetType = DomaClassName.JAVA_FUNCTION
        var superCollection: PsiClassType? = function.getSuperClassType(targetType)

        val functionFirstParam = superCollection?.parameters?.firstOrNull()
        if (functionFirstParam == null ||
            !DomaClassName.JAVA_STREAM.isTargetClassNameStartsWith(functionFirstParam.canonicalText)
        ) {
            result.highlightElement(holder)
            return
        }

        // Check if the first parameter of the stream is a valid type
        val streamParamClassType = functionFirstParam as? PsiClassType
        val strategyParamType = streamParamClassType?.parameters?.firstOrNull()
        if (strategyParamType == null) {
            generateTargetTypeResult(identifier, "Unknown", stream).highlightElement(holder)
            return
        }

        val strategyParamTypeName = strategyParamType.canonicalText
        if (DomaClassName.MAP.isTargetClassNameStartsWith(strategyParamTypeName)) {
            if (!checkMapType(strategyParamTypeName)) {
                generateTargetTypeResult(identifier, strategyParamTypeName, stream).highlightElement(holder)
            }
            return
        }

        if (!checkParamType(strategyParamType)) {
            generateTargetTypeResult(identifier, strategyParamTypeName, stream).highlightElement(holder)
        }
    }

    private fun checkCollect(holder: ProblemsHolder) {
        val result =
            ValidationMethodSelectStrategyParamResult(
                method.nameIdentifier,
                shortName,
                "COLLECT",
                DomaClassName.JAVA_COLLECTOR.className,
            )
        val collector = DomaClassName.JAVA_COLLECTOR.className
        val collection = getMethodParamTargetType(DomaClassName.JAVA_COLLECTOR.className)
        if (collection == null) {
            result.highlightElement(holder)
            return
        }

        val identifier = collection.nameIdentifier ?: return

        val targetType = DomaClassName.JAVA_COLLECTOR
        var superCollection: PsiClassType? = collection.getSuperClassType(targetType)
        val collectorTargetParam = superCollection?.parameters?.firstOrNull()
        if (collectorTargetParam == null) {
            generateTargetTypeResult(identifier, "Unknown", collector).highlightElement(holder)
            return
        }

        val collectorTargetTypeCanonicalText = collectorTargetParam.canonicalText
        if (DomaClassName.MAP.isTargetClassNameStartsWith(collectorTargetTypeCanonicalText)) {
            if (!checkMapType(collectorTargetTypeCanonicalText)) {
                generateTargetTypeResult(identifier, collectorTargetTypeCanonicalText, collector).highlightElement(holder)
            }
            return
        }

        if (!checkParamType(collectorTargetParam)) {
            generateTargetTypeResult(identifier, collectorTargetTypeCanonicalText, collector).highlightElement(holder)
        }
    }

    private fun checkStrategyOption(holder: ProblemsHolder) {
        val function = getMethodParamTargetType(DomaClassName.JAVA_FUNCTION.className)
        if (function != null) {
            ValidationMethodNotSelectStreamParamResult(
                method.nameIdentifier,
                shortName,
            ).highlightElement(holder)
        }
    }

    fun generateTargetTypeResult(
        target: PsiElement,
        paramTypeName: String,
        genericType: String,
    ): ValidationMethodParamsSupportGenericParamResult =
        ValidationMethodParamsSupportGenericParamResult(
            target,
            shortName,
            paramTypeName,
            genericType,
        )
}
