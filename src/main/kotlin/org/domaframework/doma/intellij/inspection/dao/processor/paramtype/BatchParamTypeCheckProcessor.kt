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
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsCountResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsIterableEntityResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType

/**
 * Processor for checking the parameter types of DAO methods annotated with batch annotations.
 *
 * This class validates the parameters of DAO methods to ensure they meet the requirements
 * for methods annotated with batch annotations. It performs checks such as:
 * - Ensuring the method has exactly one parameter.
 * - Verifying that the parameter is an iterable of entity types.
 *
 * @property psiDaoMethod The DAO method to be checked.
 * @property shortName The short name of the annotation being processed.
 */
class BatchParamTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the parameters of the DAO method.
     *
     * This method validates the parameters of the DAO method to ensure compliance
     * with the requirements for methods annotated with batch annotations.
     *
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    override fun checkParams(holder: ProblemsHolder) {
        // Check if the method has exactly one parameter
        checkMethodCount()?.let { result ->
            result.highlightElement(holder)
            return
        }

        // Check if the parameter is an entity
        val param = method.parameterList.parameters.firstOrNull()
        if (param == null) {
            ValidationMethodParamsCountResult(
                method.nameIdentifier ?: return,
                shortName,
            ).highlightElement(holder)
            return
        }

        val identifier = param.nameIdentifier ?: return
        val resultParamType =
            ValidationMethodParamsIterableEntityResult(
                identifier,
                shortName,
            )

        project.getJavaClazz(param.type.canonicalText)?.let { paramClass ->
            val paramClassType = paramClass.psiClassType
            if (!PsiClassTypeUtil.isIterableType(
                    paramClassType,
                    project,
                )
            ) {
                resultParamType.highlightElement(holder)
                return
            }
        }

        val iterableClassType = param.type as? PsiClassType
        iterableClassType?.parameters?.firstOrNull()?.let { iterableParam ->
            if (!TypeUtil.isEntity(iterableParam, project)) {
                resultParamType.highlightElement(holder)
            }
            return
        }
        resultParamType.highlightElement(holder)
    }
}
