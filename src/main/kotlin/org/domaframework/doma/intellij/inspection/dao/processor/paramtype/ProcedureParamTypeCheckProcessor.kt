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
import com.intellij.psi.PsiParameter
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodParamsProcedureAnnotationResult
import org.domaframework.doma.intellij.inspection.dao.processor.cheker.ProcedureFunctionParamAnnotationType

/**
 * Processor for checking the parameter types of DAO methods annotated with @Procedure.
 *
 * This class validates the parameters of DAO methods to ensure they meet the requirements
 * for methods annotated with @Procedure. It performs checks such as:
 * - Ensuring each parameter has a valid annotation.
 * - Validating the parameter types based on the annotation.
 *
 * @property psiDaoMethod The DAO method to be checked.
 * @property shortName The short name of the annotation being processed.
 */
class ProcedureParamTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the parameters of the DAO method.
     *
     * This method validates the parameters of the DAO method to ensure compliance
     * with the requirements for methods annotated with @Procedure.
     *
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    override fun checkParams(holder: ProblemsHolder) {
        val params = method.parameterList.parameters
        var paramAnnotationType: ProcedureFunctionParamAnnotationType? = null
        val psiDaoMethod = PsiDaoMethod(method.project, method)
        params.forEach { param: PsiParameter ->
            val paramAnnotation =
                param.annotations.firstOrNull { annotation ->
                    paramAnnotationType =
                        ProcedureFunctionParamAnnotationType.entries.find { it.fqdn == annotation.qualifiedName }
                    paramAnnotationType != null
                }
            if (paramAnnotation == null || paramAnnotationType == null) {
                ValidationMethodParamsProcedureAnnotationResult(
                    param.nameIdentifier ?: return,
                    shortName,
                ).highlightElement(holder)
                return
            }
            checkParamType(psiDaoMethod, paramAnnotationType, param, holder)
        }
    }

    /**
     * Validates the type of parameter based on its annotation.
     *
     * Checks if the parameter type is valid for the given annotation type in the context of the DAO method.
     *
     * @param psiDaoMethod The DAO method containing the parameter.
     * @param paramAnnotationType The type of the annotation on the parameter.
     * @param param The parameter to be validated.
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    private fun checkParamType(
        psiDaoMethod: PsiDaoMethod,
        paramAnnotationType: ProcedureFunctionParamAnnotationType,
        param: PsiParameter,
        holder: ProblemsHolder,
    ) {
        val identifier = param.nameIdentifier ?: return
        paramAnnotationType.checkParamType(psiDaoMethod, identifier, param.type, param.project, shortName, holder)
    }
}
