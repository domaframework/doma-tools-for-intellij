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
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationForDirectiveItemTypeResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class InspectionPrimaryVisitorProcessor(
    val shortName: String,
    private val element: SqlElPrimaryExpr,
) : InspectionVisitorProcessor(shortName) {
    fun check(
        holder: ProblemsHolder,
        file: PsiFile,
    ) {
        if (isLiteralOrStatic(element)) return
        PsiTreeUtil.getParentOfType(element, SqlElStaticFieldAccessExpr::class.java)?.let { return }

        val forDirectiveExp = PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java)
        val isSkip = forDirectiveExp != null && forDirectiveExp.getForItem() != element

        var forDirectiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(element, skipSelf = isSkip)
        val forItem =
            ForDirectiveUtil.findForItem(
                element,
                skipSelf = isSkip,
                forDirectives = forDirectiveBlocks,
            )
        if (forDirectiveExp?.getForItem() == element) return

        if (forItem != null) {
            val forDeclarationType =
                ForDirectiveUtil.getForDirectiveItemClassType(
                    element.project,
                    forDirectiveBlocks,
                    forItem,
                )
            if (forDeclarationType == null) {
                ValidationForDirectiveItemTypeResult(
                    element,
                    this.shortName,
                ).highlightElement(holder)
            }
            return
        }

        val daoMethod = findDaoMethod(file) ?: return
        val param = daoMethod.findParameter(cleanString(element.text))
        if (param != null) return

        ValidationDaoParamResult(
            element,
            daoMethod.name,
            this.shortName,
        ).highlightElement(holder)
    }
}
