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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.DummyPsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr

class InspectionFieldAccessVisitorProcessor(
    val shortName: String,
    private val element: SqlElFieldAccessExpr,
) : InspectionVisitorProcessor() {
    private val project = element.project
    private var targetFile: PsiFile = element.containingFile
    private var blockElements: List<SqlElIdExpr> = emptyList()
    private var topElement: SqlElIdExpr? = null
    private var daoMethod: PsiMethod? = findDaoMethod(targetFile)
    private var isBatchAnnotation = false

    /**
     * Check that the source of the bind variable in the SQL exists
     */
    fun checkBindVariableDefine(holder: ProblemsHolder) {
        when (val topElementClass = resolveTopElementType(targetFile)) {
            is DummyPsiParentClass -> return
            null -> {
                handleNullTopElementClass(holder)
                return
            }

            else -> checkFieldAccess(topElementClass, holder)
        }
    }

    /**
     * Get the final class type of the field access element
     */
    fun getFieldAccessLastPropertyClassType(): PsiType? =
        when (val topElementClass = resolveTopElementType(targetFile)) {
            is DummyPsiParentClass, null -> null

            else -> getLastFieldAccess(topElementClass).type
        }

    private fun resolveTopElementType(file: PsiFile): PsiParentClass? {
        targetFile = file
        blockElements = extractFieldAccessBlocks(element)

        if (!validateBlockElements()) {
            return DummyPsiParentClass()
        }

        val topElm = topElement ?: return DummyPsiParentClass()
        val method = daoMethod ?: return DummyPsiParentClass()

        return resolveTypeFromContext(topElm, method)
    }

    private fun validateBlockElements(): Boolean {
        val primaryElement = blockElements.firstOrNull() as? SqlElPrimaryExpr
        if (primaryElement != null && isLiteralOrStatic(primaryElement)) {
            return false
        }

        topElement = blockElements.firstOrNull()
        return daoMethod != null && topElement != null
    }

    private fun resolveTypeFromContext(
        topElm: SqlElIdExpr,
        method: PsiMethod,
    ): PsiParentClass? {
        val forDirectiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(topElm)
        val forItem = ForDirectiveUtil.findForItem(topElm, forDirectives = forDirectiveBlocks)

        return if (forItem != null) {
            resolveForDirectiveType(topElm, forDirectiveBlocks, forItem)
        } else {
            resolveParameterType(topElm, method)
        }
    }

    private fun resolveForDirectiveType(
        topElm: SqlElIdExpr,
        forDirectiveBlocks: List<ForDirectiveUtil.BlockToken>,
        forItem: PsiElement,
    ): PsiParentClass? {
        val baseType =
            ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectiveBlocks, forItem)
                ?: return DummyPsiParentClass()

        val specifiedType = ForDirectiveUtil.resolveForDirectiveItemClassTypeBySuffixElement(topElm.text)
        return if (specifiedType != null) {
            PsiParentClass(specifiedType)
        } else {
            baseType
        }
    }

    private fun resolveParameterType(
        topElm: SqlElIdExpr,
        method: PsiMethod,
    ): PsiParentClass? {
        val paramType = method.findParameter(cleanString(topElm.text))?.type ?: return null
        isBatchAnnotation = PsiDaoMethod(project, method).daoType.isBatchAnnotation()
        return PsiParentClass(paramType)
    }

    private fun handleNullTopElementClass(holder: ProblemsHolder) {
        val topElm = topElement ?: return
        val method = daoMethod ?: return
        errorHighlight(topElm, method, holder)
    }

    private fun checkFieldAccess(
        topElementClass: PsiParentClass,
        holder: ProblemsHolder,
    ) {
        val result = getFieldAccess(topElementClass)
        result?.highlightElement(holder)
    }

    private fun getFieldAccess(topElementClass: PsiParentClass) =
        ForDirectiveUtil.getFieldAccessLastPropertyClassType(
            blockElements,
            project,
            topElementClass,
            shortName = this.shortName,
            isBatchAnnotation = isBatchAnnotation,
        )

    private fun getLastFieldAccess(topElementClass: PsiParentClass): PsiParentClass {
        var findLastTypeParent = topElementClass
        ForDirectiveUtil.getFieldAccessLastPropertyClassType(
            blockElements,
            project,
            topElementClass,
            shortName = this.shortName,
            isBatchAnnotation = isBatchAnnotation,
            findFieldMethod = { type ->
                findLastTypeParent = PsiParentClass(type)
                findLastTypeParent
            },
        )
        return findLastTypeParent
    }

    private fun extractFieldAccessBlocks(element: SqlElFieldAccessExpr): List<SqlElIdExpr> {
        val accessElements = element.accessElements
        val primaryElement = accessElements.firstOrNull() as? SqlElPrimaryExpr ?: return emptyList()

        if (isLiteralOrStatic(primaryElement)) {
            return emptyList()
        }

        return accessElements.filterIsInstance<SqlElIdExpr>()
    }

    private fun errorHighlight(
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
}
