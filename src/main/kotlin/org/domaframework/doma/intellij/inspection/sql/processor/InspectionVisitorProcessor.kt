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
package org.domaframework.doma.intellij.inspection.sql.processor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.validation.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElNewExpr
import org.domaframework.doma.intellij.psi.SqlTypes

abstract class InspectionVisitorProcessor(
    private val shortName: String,
) {
    protected fun errorHighlight(
        topElement: SqlElIdExpr,
        daoMethod: PsiMethod,
        holder: ProblemsHolder,
    ) {
        ValidationDaoParamResult(
            topElement,
            daoMethod.name,
            this.shortName,
        ).highlightElement(holder)
    }

    protected fun isLiteralOrStatic(targetElement: PsiElement): Boolean =
        (
            targetElement.firstChild?.elementType == SqlTypes.EL_STRING ||
                targetElement.firstChild?.elementType == SqlTypes.EL_CHAR ||
                targetElement.firstChild?.elementType == SqlTypes.EL_NUMBER ||
                targetElement.firstChild?.elementType == SqlTypes.EL_NULL ||
                targetElement.firstChild?.elementType == SqlTypes.BOOLEAN ||
                targetElement.firstChild is SqlElNewExpr ||
                targetElement.text.startsWith("@")
        )
}
