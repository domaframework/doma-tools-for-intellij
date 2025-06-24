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
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamTypeResult

/**
 * Processor for checking the parameter types of DAO methods annotated with @Insert,@Update,@Delete.
 *
 * This class validates the parameters of DAO methods to ensure they meet the requirements
 * for methods annotated with @Insert,@Update,@Delete. It performs checks such as:
 * - Ensuring the method has exactly one parameter.
 * - Verifying that the parameter is of an entity type.
 *
 * @property psiDaoMethod The DAO method to be checked.
 * @property shortName The short name of the annotation being processed.
 */
class UpdateParamTypeCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the parameters of the DAO method.
     *
     * This method validates the parameters of the DAO method to ensure compliance
     * with the requirements for methods annotated with @Insert,@Update,@Delete. If the method uses
     * SQL annotations or SQL file options, the validation is skipped.
     *
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    override fun checkParams(holder: ProblemsHolder) {
        if (psiDaoMethod.useSqlAnnotation() || psiDaoMethod.sqlFileOption) {
            return
        }

        // Check if the method has exactly one parameter
        checkMethodCount()?.let { result ->
            result.highlightElement(holder)
            return
        }

        // Check if the method has a parameter of type entity
        val param = method.parameterList.parameters.firstOrNull()
        if (!TypeUtil.isEntity(param?.type, project)) {
            ValidationMethodParamTypeResult(
                param?.nameIdentifier,
                shortName,
                "an entity",
            ).highlightElement(holder)
        }
    }
}
