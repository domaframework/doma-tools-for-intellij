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
import com.intellij.psi.PsiArrayType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsCountResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsEmptyResult
import org.domaframework.doma.intellij.inspection.dao.processor.FactoryAnnotationType

class FactoryParamTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    override fun checkParams(holder: ProblemsHolder) {
        val daoType = psiDaoMethod.daoType
        val factoryAnnotationType =
            FactoryAnnotationType.entries.find { annotation ->
                annotation.matchFactoryAnnotation(daoType.fqdn)
            } ?: return

        val params = method.parameterList.parameters
        if (factoryAnnotationType.paramCount != params.size) {
            val paramCountResult =
                when (factoryAnnotationType) {
                    FactoryAnnotationType.ArrayFactory ->
                        ValidationMethodParamsCountResult(
                            method.nameIdentifier,
                            shortName,
                        )

                    else ->
                        ValidationMethodParamsEmptyResult(
                            method.nameIdentifier,
                            shortName,
                        )
                }
            paramCountResult.highlightElement(holder)
            return
        }

        if (factoryAnnotationType == FactoryAnnotationType.ArrayFactory) {
            val arrayParam = params.firstOrNull() ?: return
            if (arrayParam.type !is PsiArrayType) {
                ValidationMethodParamTypeResult(
                    arrayParam.nameIdentifier ?: return,
                    shortName,
                    "an array type",
                ).highlightElement(holder)
            }
        }
    }
}
