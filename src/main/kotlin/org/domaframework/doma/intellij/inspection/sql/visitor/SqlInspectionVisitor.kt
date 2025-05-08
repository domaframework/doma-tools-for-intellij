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
package org.domaframework.doma.intellij.inspection.sql.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationClassPathResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.isFirstElement
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : SqlVisitorBase() {
    override fun visitElement(element: PsiElement) {
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return
        if (isJavaOrKotlinFileType(visitFile) && element is PsiLiteralExpression) {
            val injectionFile = initInjectionElement(visitFile, element.project, element) ?: return
            injectionFile.accept(this)
            super.visitElement(element)
        }
        if (isInjectionSqlFile(visitFile)) {
            element.acceptChildren(this)
        }
    }

    override fun visitElStaticFieldAccessExpr(element: SqlElStaticFieldAccessExpr) {
        super.visitElStaticFieldAccessExpr(element)
        checkStaticFieldAndMethodAccess(element, holder)
    }

    override fun visitElFieldAccessExpr(element: SqlElFieldAccessExpr) {
        super.visitElFieldAccessExpr(element)
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return

        // Get element inside block comment
        val blockElement = getFieldAccessBlocks(element)
        val topElm = blockElement.firstOrNull() as SqlElPrimaryExpr

        // Exclude fixed Literal
        if (isLiteralOrStatic(topElm)) return

        checkAccessFieldAndMethod(holder, blockElement, visitFile)
    }

    override fun visitElPrimaryExpr(element: SqlElPrimaryExpr) {
        super.visitElPrimaryExpr(element)
        if (!element.isFirstElement() || element.prevSibling?.elementType == SqlTypes.AT_SIGN) return
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return

        if (isLiteralOrStatic(element)) return
        PsiTreeUtil.getParentOfType(element, SqlElStaticFieldAccessExpr::class.java)?.let { return }

        val forDirectiveExp = PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java)
        if (forDirectiveExp != null && forDirectiveExp.getForItem() == element) return

        val forItem = ForDirectiveUtil.findForItem(element)
        if (forItem != null) return

        val daoMethod = findDaoMethod(visitFile) ?: return
        val param = daoMethod.findParameter(cleanString(element.text))
        if (param != null) return

        ValidationDaoParamResult(
            element,
            daoMethod.name,
            this.shortName,
        ).highlightElement(holder)
    }

    private fun getFieldAccessBlocks(element: SqlElFieldAccessExpr): List<SqlElIdExpr> {
        val blockElements = element.accessElements
        (blockElements.firstOrNull() as? SqlElPrimaryExpr)
            ?.let { if (isLiteralOrStatic(it)) return emptyList() }
            ?: return emptyList()

        return blockElements.mapNotNull { it as SqlElIdExpr }
    }

    private fun checkAccessFieldAndMethod(
        holder: ProblemsHolder,
        blockElement: List<SqlElIdExpr>,
        file: PsiFile,
    ) {
        val topElement = blockElement.firstOrNull() ?: return
        val daoMethod = findDaoMethod(file) ?: return
        val project = topElement.project
        val forDirectiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(topElement)
        val forItem = ForDirectiveUtil.findForItem(topElement, forDirectives = forDirectiveBlocks)
        var isBatchAnnotation = false
        val topElementParentClass =
            if (forItem != null) {
                val result = ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectiveBlocks)
                if (result == null) {
                    errorHighlight(topElement, daoMethod, holder)
                    return
                }
                result
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

    /**
     * Check for existence of static field
     */
    private fun checkStaticFieldAndMethodAccess(
        staticAccuser: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        val blockElements = staticAccuser.accessElements
        val psiStaticClass = PsiStaticElement(staticAccuser.elClass.elIdExprList, staticAccuser.containingFile)
        val referenceClass = psiStaticClass.getRefClazz()
        if (referenceClass == null) {
            ValidationClassPathResult(
                staticAccuser.elClass,
                this.shortName,
            ).highlightElement(holder)
            return
        }

        val topParentClass = ForDirectiveUtil.getStaticFieldAccessTopElementClassType(staticAccuser, referenceClass)
        if (topParentClass == null) {
            blockElements.firstOrNull()?.let {
                ValidationPropertyResult(
                    it,
                    PsiParentClass(referenceClass.psiClassType),
                    this.shortName,
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
                    shortName = this.shortName,
                )
            }
        result?.highlightElement(holder)
    }
}
