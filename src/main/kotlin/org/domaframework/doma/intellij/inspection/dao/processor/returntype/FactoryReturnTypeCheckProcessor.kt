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

import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeResult
import org.domaframework.doma.intellij.inspection.dao.processor.FactoryAnnotationType

/**
 * Processor for checking the return type of SqlProcessor annotation.
 *
 * @param psiDaoMethod The target DAO method info to be checked.
 * @param shortName The short name of the inspection to check.
 */
class FactoryReturnTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    override fun checkReturnType(): ValidationResult? {
        val daoType = psiDaoMethod.daoType
        val factoryAnnotationType =
            FactoryAnnotationType.entries.find { annotation ->
                annotation.matchFactoryAnnotation(daoType.fqdn)
            } ?: return null

        val returnTypeResult =
            ValidationReturnTypeResult(
                method.nameIdentifier,
                shortName,
                factoryAnnotationType.returnType,
            )

        val returnType = method.returnType
        return if (returnType?.canonicalText != factoryAnnotationType.returnType) {
            returnTypeResult
        } else {
            null
        }
    }
}
