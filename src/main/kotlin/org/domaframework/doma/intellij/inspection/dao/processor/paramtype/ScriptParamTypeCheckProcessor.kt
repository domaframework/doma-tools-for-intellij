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
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsEmptyResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult

/**
 * Processor for checking the parameter types of DAO methods annotated with @Script.
 *
 * This class validates the parameters of DAO methods to ensure they meet the requirements
 * for methods annotated with @Script. It performs checks such as:
 * - Ensuring the method has no parameters.
 *
 * @property psiDaoMethod The DAO method to be checked.
 * @property shortName The short name of the annotation being processed.
 */
class ScriptParamTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the parameters of the DAO method.
     *
     * This method validates the parameters of the DAO method to ensure compliance
     * with the requirements for methods annotated with @Script.
     *
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    override fun checkParams(holder: ProblemsHolder) {
        checkMethodCount()?.highlightElement(holder)
    }

    /**
     * Checks if the method has the correct number of parameters.
     *
     * This method ensures that the method has no parameters.
     *
     * @return A ValidationResult if the parameter count is invalid, or null if valid.
     */
    override fun checkMethodCount(): ValidationResult? {
        // Script methods should not have any parameters
        return if (method.parameterList.parameters.isNotEmpty()) {
            ValidationMethodParamsEmptyResult(
                method.nameIdentifier,
                shortName,
            )
        } else {
            null
        }
    }
}
