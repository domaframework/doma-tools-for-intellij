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

import androidx.compose.compiler.plugins.kotlin.lower.fastForEachIndexed
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodBiFunctionParamResult
import org.domaframework.doma.intellij.common.validation.result.ValidationMethodHasRequireClassParamResult
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType

/**
 * Processor for checking the parameter types of DAO methods annotated with @SqlProcessor.
 *
 * This class validates the parameters of DAO methods to ensure they meet the requirements
 * for methods annotated with @SqlProcessor. It performs checks such as:
 * - Ensuring the method has a parameter of type BiFunction.
 * - Validating the types of the BiFunction parameters.
 *
 * @property psiDaoMethod The DAO method to be checked.
 * @property shortName The short name of the annotation being processed.
 */
class SqlProcessorParamTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : ParamTypeCheckProcessor(psiDaoMethod, shortName) {
    private val biFunctionClassName = DomaClassName.BI_FUNCTION.className

    /**
     * Checks the parameters of the DAO method.
     *
     * This method validates the parameters of the DAO method to ensure compliance
     * with the requirements for methods annotated with @SqlProcessor.
     *
     * @param holder The ProblemsHolder instance used to report validation issues.
     */
    override fun checkParams(holder: ProblemsHolder) {
        val biFunctionParam = getMethodParamTargetType(biFunctionClassName)
        if (biFunctionParam == null) {
            ValidationMethodHasRequireClassParamResult(
                method.nameIdentifier,
                shortName,
                DomaAnnotationType.SqlProcessor,
                "BiFunction",
            ).highlightElement(holder)
            return
        }

        val biFunctionClassType = (biFunctionParam.type as? PsiClassType)
        val identifier = biFunctionParam.nameIdentifier ?: return
        biFunctionClassType?.parameters?.fastForEachIndexed { index, param ->
            if (param == null || !checkBiFunctionParam(index, param)) {
                ValidationMethodBiFunctionParamResult(
                    identifier,
                    shortName,
                    index,
                ).highlightElement(holder)
            }
        }
    }

    /**
     * Validates the type of BiFunction parameter.
     *
     * @param index The index of the BiFunction parameter.
     * @param paramType The type of the BiFunction parameter.
     * @return True if the parameter type is valid, false otherwise.
     */
    private fun checkBiFunctionParam(
        index: Int,
        paramType: PsiType,
    ): Boolean =
        when (index) {
            0 -> paramType.canonicalText == DomaClassName.CONFIG.className
            1 -> paramType.canonicalText == DomaClassName.PREPARED_SQL.className
            2 -> true
            else -> false
        }
}
