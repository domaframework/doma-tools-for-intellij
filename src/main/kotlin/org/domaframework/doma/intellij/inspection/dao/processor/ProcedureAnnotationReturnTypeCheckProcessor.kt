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
package org.domaframework.doma.intellij.inspection.dao.processor

import com.intellij.psi.PsiTypes
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.validation.result.ValidationResult

class ProcedureAnnotationReturnTypeCheckProcessor(
    psiDaoMethod: PsiDaoMethod,
    shortName: String,
) : ReturnTypeCheckerProcessor(psiDaoMethod, shortName) {
    /**
     * Checks the return type of method annotated with `@Procedure`.
     * The expected return type is `void`.
     */
    override fun checkReturnType(): ValidationResult? {
        val methodOtherReturnType = PsiTypes.voidType()
        return generatePsiTypeReturnTypeResult(methodOtherReturnType)
    }
}
