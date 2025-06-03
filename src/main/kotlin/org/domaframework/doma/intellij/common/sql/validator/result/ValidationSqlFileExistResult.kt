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
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.inspection.dao.quickfix.GenerateSQLFileQuickFixFactory

class ValidationSqlFileExistResult(
    private val psiDaoMethod: PsiDaoMethod,
    override val identify: PsiElement?,
    override val shortName: String = "",
) : ValidationResult(identify, null, shortName) {
    override fun setHighlight(
        highlightRange: TextRange,
        identify: PsiElement,
        holder: ProblemsHolder,
        parent: PsiParentClass?,
    ) {
        val project = psiDaoMethod.psiMethod.project
        holder.registerProblem(
            identify,
            MessageBundle.message("inspection.invalid.dao.notExistSql"),
            problemHighlightType(project, shortName),
            GenerateSQLFileQuickFixFactory.createSql(psiDaoMethod),
        )
    }
}
