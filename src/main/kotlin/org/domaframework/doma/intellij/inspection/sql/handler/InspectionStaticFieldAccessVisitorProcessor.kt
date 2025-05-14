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
package org.domaframework.doma.intellij.inspection.sql.handler

import com.intellij.codeInspection.ProblemsHolder
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationClassPathResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationNotFoundStaticPropertyResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class InspectionStaticFieldAccessVisitorProcessor(
    val shortName: String,
) : InspectionVisitorProcessor(shortName) {
    /**
     * Check for existence of static field
     */
    fun check(
        staticAccuser: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        val blockElements = staticAccuser.accessElements
        val psiStaticClass = PsiStaticElement(staticAccuser.elClass.elIdExprList, staticAccuser.containingFile)
        val referenceClass = psiStaticClass.getRefClazz()
        if (referenceClass == null) {
            ValidationClassPathResult(
                staticAccuser.elClass,
                shortName,
            ).highlightElement(holder)
            return
        }

        val topParentClass = ForDirectiveUtil.getStaticFieldAccessTopElementClassType(staticAccuser, referenceClass)
        if (topParentClass == null) {
            blockElements.firstOrNull()?.let {
                ValidationNotFoundStaticPropertyResult(
                    it,
                    staticAccuser.elClass,
                    shortName,
                ).highlightElement(holder)
            }
            return
        }
        val result =
            topParentClass.let {
                ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                    blockElements,
                    staticAccuser.project,
                    it,
                    shortName = shortName,
                )
            }
        result?.highlightElement(holder)
    }
}
