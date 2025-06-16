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
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsCountResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.inspection.dao.processor.TypeCheckerProcessor

/**
 * Abstract base class for checking the parameter types of DAO methods.
 *
 * This class provides common functionality for validating the parameters of DAO methods
 * in various annotation processors. Subclasses should implement the `checkParams` method
 * to perform specific validation logic.
 *
 * @property psiDaoMethod The DAO method to be checked.
 * @property shortName The short name of the annotation being processed.
 */
abstract class ParamTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : TypeCheckerProcessor(psiDaoMethod) {
    /**
     * Abstract method for checking the parameters of the DAO method.
     *
     * Subclasses should implement this method to perform specific validation logic.
     *
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    abstract fun checkParams(holder: ProblemsHolder)

    /**
     * Checks if the method has the correct number of parameters.
     *
     * By default, this method checks if the method has exactly one parameter.
     * Subclasses can override this method to provide custom logic.
     *
     * @return A ValidationResult if the parameter count is invalid, or null if valid.
     */
    protected open fun checkMethodCount(): ValidationResult? =
        if (method.parameterList.parameters.size != 1) {
            ValidationMethodParamsCountResult(
                method.nameIdentifier,
                shortName,
            )
        } else {
            null
        }
}
