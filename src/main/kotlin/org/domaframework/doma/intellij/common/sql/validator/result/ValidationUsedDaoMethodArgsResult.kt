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
package org.domaframework.doma.intellij.common.sql.validator.result

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiParameter
import com.intellij.psi.impl.source.PsiParameterImpl
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.inspection.dao.visitor.DaoMethodVariableInspectionVisitor.DaoMethodVariableVisitorResult

class ValidationUsedDaoMethodArgsResult(
    private val daoMethodVariableResult: DaoMethodVariableVisitorResult,
    override val identify: PsiElement?,
    override val shortName: String = "",
) : ValidationResult(identify, null, shortName) {
    override fun setHighlight(
        highlightRange: TextRange,
        identify: PsiElement,
        holder: ProblemsHolder,
        parent: PsiParentClass?,
    ) {
        val param = identify as? PsiParameter ?: return
        val project = identify.project
        val message =
            if (daoMethodVariableResult.deplicateForItemElements.contains(identify)) {
                MessageBundle.message("inspection.invalid.dao.duplicate")
            } else {
                MessageBundle.message(
                    "inspection.invalid.dao.paramUse",
                    param.name,
                )
            }
        holder.registerProblem(
            (param.originalElement as PsiParameterImpl).nameIdentifier,
            message,
            problemHighlightType(project, shortName),
        )
    }
}
