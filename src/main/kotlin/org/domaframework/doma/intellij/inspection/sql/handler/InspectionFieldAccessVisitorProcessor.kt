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
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationForDirectiveItemTypeResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr

class InspectionFieldAccessVisitorProcessor(
    val shortName: String,
    private val element: SqlElFieldAccessExpr,
) : InspectionVisitorProcessor(shortName) {
    fun check(
        holder: ProblemsHolder,
        file: PsiFile,
    ) {
        // Get element inside block comment
        val blockElement = getFieldAccessBlocks(element)
        val topElm = blockElement.firstOrNull() as SqlElPrimaryExpr

        // Exclude fixed Literal
        if (isLiteralOrStatic(topElm)) return

        val topElement = blockElement.firstOrNull() ?: return
        val daoMethod = findDaoMethod(file) ?: return
        val project = topElement.project
        val forDirectiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(topElement)
        val forItem = ForDirectiveUtil.findForItem(topElement, forDirectives = forDirectiveBlocks)
        var isBatchAnnotation = false
        val topElementParentClass =
            if (forItem != null) {
                val result = ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectiveBlocks, forItem)
                if (result == null) {
                    ValidationForDirectiveItemTypeResult(
                        topElement,
                        this.shortName,
                    ).highlightElement(holder)
                    return
                }
                val specifiedClassType =
                    ForDirectiveUtil.resolveForDirectiveItemClassTypeBySuffixElement(topElement.text)
                if (specifiedClassType != null) {
                    PsiParentClass(specifiedClassType)
                } else {
                    result
                }
            } else {
                val paramType = daoMethod.findParameter(cleanString(topElement.text))?.type
                if (paramType == null) {
                    errorHighlight(topElement, daoMethod, holder)
                    return
                }
                isBatchAnnotation = PsiDaoMethod(project, daoMethod).daoType.isBatchAnnotation()
                PsiParentClass(paramType)
            }

        val result =
            ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                blockElement,
                project,
                topElementParentClass,
                shortName = this.shortName,
                isBatchAnnotation = isBatchAnnotation,
            )

        result?.highlightElement(holder)
    }

    fun getFieldAccessBlocks(element: SqlElFieldAccessExpr): List<SqlElIdExpr> {
        val blockElements = element.accessElements
        (blockElements.firstOrNull() as? SqlElPrimaryExpr)
            ?.let { if (isLiteralOrStatic(it)) return emptyList() }
            ?: return emptyList()

        return blockElements.mapNotNull { it as SqlElIdExpr }
    }
}
