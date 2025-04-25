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
package org.domaframework.doma.intellij.inspection.sql

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.sql.validator.SqlElFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.SqlElForItemFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.SqlElStaticFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.isFirstElement
import org.domaframework.doma.intellij.inspection.ForDirectiveInspection
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElNewExpr
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.psi.SqlVisitor

class SqlInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : SqlVisitor() {
    var file: PsiFile? = null

    private fun setFile(element: PsiElement): Boolean {
        if (file == null) {
            file = element.containingFile
        }
        return false
    }

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

        val daoMethod = findDaoMethod(visitFile) ?: return

        val forDirectiveInspection =
            ForDirectiveInspection(daoMethod, this.shortName)

        if (PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java) != null) {
            val forDirectivesSize = forDirectiveInspection.getForDirectiveBlockSize(element)
            if (forDirectivesSize == 0) return
        }

        // Element names defined in the For directory are not checked.
        val forItem = forDirectiveInspection.getForItem(element)
        if (forItem != null) {
            return
        }

        val validDaoParam = daoMethod.findParameter(element.text)
        if (validDaoParam == null) {
            val errorElement =
                ValidationDaoParamResult(
                    element,
                    daoMethod.name,
                    shortName,
                )
            errorElement.highlightElement(holder)
        }
    }

    /**
     * For processing inside Sql annotations, get it as an injected custom language
     */
    private fun initInjectionElement(
        basePsiFile: PsiFile,
        project: Project,
        literal: PsiLiteralExpression,
    ): PsiFile? =
        when (isJavaOrKotlinFileType(basePsiFile)) {
            true -> {
                val injectedLanguageManager =
                    InjectedLanguageManager.getInstance(project)
                injectedLanguageManager
                    .getInjectedPsiFiles(literal)
                    ?.firstOrNull()
                    ?.first as? PsiFile
            }

            false -> null
        }

    private fun isLiteralOrStatic(targetElement: PsiElement): Boolean =
        (
            targetElement.firstChild?.elementType == SqlTypes.EL_STRING ||
                targetElement.firstChild?.elementType == SqlTypes.EL_CHAR ||
                targetElement.firstChild?.elementType == SqlTypes.EL_NUMBER ||
                targetElement.firstChild?.elementType == SqlTypes.EL_NULL ||
                targetElement.firstChild?.elementType == SqlTypes.BOOLEAN ||
                targetElement.firstChild is SqlElNewExpr ||
                targetElement.text.startsWith("@")
        )

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
        val daoMethod = findDaoMethod(file) ?: return
        val forDirectiveInspection = ForDirectiveInspection(daoMethod, this.shortName)
        var errorElement: ValidationResult? =
            forDirectiveInspection.validateFieldAccessByForItem(blockElement.toList())
        if (errorElement is ValidationCompleteResult) {
            val currentFieldAccessValidator =
                SqlElForItemFieldAccessorChildElementValidator(
                    blockElement,
                    errorElement.parentClass,
                    this.shortName,
                )
            errorElement = currentFieldAccessValidator.validateChildren()
            if (errorElement is ValidationCompleteResult) return
        }
        if (errorElement is ValidationPropertyResult) {
            errorElement.highlightElement(holder)
            return
        }

        val validator =
            SqlElFieldAccessorChildElementValidator(
                blockElement,
                file,
                this.shortName,
            )
        errorElement = validator.validateChildren()
        errorElement?.highlightElement(holder)
    }

    /**
     * Check for existence of static field
     */
    private fun checkStaticFieldAndMethodAccess(
        staticAccuser: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        val blockElements = staticAccuser.accessElements
        val validator =
            SqlElStaticFieldAccessorChildElementValidator(
                blockElements,
                staticAccuser,
                this.shortName,
            )
        val errorElement: ValidationResult? = validator.validateChildren()
        errorElement?.highlightElement(holder)
    }
}
