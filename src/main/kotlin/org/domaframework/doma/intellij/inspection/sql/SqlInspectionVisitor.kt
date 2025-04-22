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

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationDaoBaseItem
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.expr.fqdn
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz
import org.domaframework.doma.intellij.extension.psi.getMethodReturnType
import org.domaframework.doma.intellij.extension.psi.isFirstElement
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.inspection.ForDirectiveInspection
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElNewExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.psi.SqlVisitor
import org.jetbrains.kotlin.idea.base.util.module

class SqlInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : SqlVisitor() {
    private val forDirectiveInspection =
        ForDirectiveInspection()

    override fun visitElement(element: PsiElement) {
        val file = element.containingFile ?: return
        if (isJavaOrKotlinFileType(file) && element is PsiLiteralExpression) {
            val injectionFile = initInjectionElement(file, element.project, element) ?: return
            injectionFile.accept(this)
            super.visitElement(element)
        }
        if (isInjectionSqlFile(file)) {
            element.acceptChildren(this)
        }
    }

    override fun visitElStaticFieldAccessExpr(element: SqlElStaticFieldAccessExpr) {
        super.visitElStaticFieldAccessExpr(element)
        checkStaticFieldAndMethodAccess(element, holder)
    }

    override fun visitElFieldAccessExpr(element: SqlElFieldAccessExpr) {
        super.visitElFieldAccessExpr(element)
        val file = element.containingFile ?: return

        // Get element inside block comment
        val blockElement = getFieldAccessBlocks(element)
        val topElm = blockElement.firstOrNull() as SqlElPrimaryExpr

        // Exclude fixed Literal
        if (isLiteralOrStatic(topElm)) return

        checkAccessFieldAndMethod(holder, blockElement, file)
    }

    override fun visitElPrimaryExpr(element: SqlElPrimaryExpr) {
        super.visitElPrimaryExpr(element)
        if (!element.isFirstElement() || element.prevSibling?.elementType == SqlTypes.AT_SIGN) return
        val file = element.containingFile ?: return
        val project = element.project

        // Exclude fixed Literal
        if (isLiteralOrStatic(element)) return

        // For static property references, match against properties in the class definition
        val parentStaticFieldAccessExpr =
            PsiTreeUtil.getParentOfType(element, SqlElStaticFieldAccessExpr::class.java)
        if (parentStaticFieldAccessExpr != null) {
            return
        }

        val daoMethod = findDaoMethod(file) ?: return

        // Element names defined in the For directory are not checked.
        val forItem = forDirectiveInspection.getForItemInForDirectiveBlock(element)
        if (forItem != null) {
            return
        }

        val params = daoMethod.methodParameters
        val validDaoParam =
            params.firstOrNull { p ->
                p.name == element.text
            }

        if (validDaoParam == null) {
            val highlightElm = element.originalElement ?: return
            val highlightRange =
                TextRange(0, element.textRange?.length ?: 0)
            setHighlightDaoMethodBind(
                highlightRange,
                highlightElm,
                holder,
                daoMethod,
                project,
            )
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

    private fun getFieldAccessBlocks(element: SqlElFieldAccessExpr): Array<SqlElIdExpr> {
        val blockElements =
            PsiTreeUtil
                .getChildrenOfTypeAsList(element, SqlElIdExpr::class.java)
                .toTypedArray()
        val topElm = blockElements.firstOrNull() as SqlElPrimaryExpr

        // Exclude fixed Literal
        if (isLiteralOrStatic(topElm)) return emptyArray()
        return blockElements
    }

    private fun checkAccessFieldAndMethod(
        holder: ProblemsHolder,
        blockElement: Array<out PsiElement>,
        file: PsiFile,
    ) {
        val topElement: PsiElement = blockElement.firstOrNull() ?: return

        // If the field is defined, get the type of the field that defines it.
        val forItem = forDirectiveInspection.getForItemInForDirectiveBlock(topElement)

        var errorElement: HighlightingElementData? = HighlightingElementData(topElement, null)
        if (forItem != null) {
            val declarationItem =
                forDirectiveInspection.getDeclarationItem(forItem, file)

            if (declarationItem != null) {
                if (declarationItem is ForDeclarationDaoBaseItem) {
                    val forItemElementsTopType = declarationItem.getPsiParentClass()
                    if (forItemElementsTopType != null) {
                        errorElement =
                            inspectForElements(
                                blockElement.toList(),
                                forItemElementsTopType,
                                forItemElementsTopType.type,
                            )
                        if (errorElement == null) return
                    }
                }
            }
        }

        val daoMethod = findDaoMethod(file) ?: return
        val params = daoMethod.methodParameters
        val validDaoParam =
            params.firstOrNull { p ->
                p.name == topElement.text
            }

        // Check the match with the Dao method argument of the top element
        // Do not perform subsequent processing if it is a fixed character or non-existent variable name

        if (validDaoParam != null) {
            val parentClass =
                validDaoParam.getIterableClazz(daoMethod.getDomaAnnotationType())
            errorElement =
                inspectElements(
                    blockElement.toList(),
                    parentClass,
                    parentClass.type,
                )
        }

        if (errorElement != null) {
            highlightElement(holder, errorElement)
        }
    }

    /**
     * Check for existence of static field
     */
    private fun checkStaticFieldAndMethodAccess(
        staticAccuser: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        val blockElements = staticAccuser.accessElements
        val staticTopElement = blockElements.firstOrNull() ?: return
        val module = staticAccuser.module ?: return
        val fqdn = staticAccuser.fqdn
        val clazz = module.getJavaClazz(false, fqdn) ?: return

        var parent = PsiParentClass(clazz.psiClassType)
        val topField =
            if (staticTopElement.nextSibling !is SqlElParameters) {
                parent.findStaticField(staticTopElement.text)
            } else {
                null
            }
        val topMethod =
            if (staticTopElement.nextSibling is SqlElParameters) {
                parent.findStaticMethod(staticTopElement.text)
            } else {
                null
            }
        if (topField == null && topMethod == null) {
            val staticErrorElement =
                HighlightingElementData(
                    staticTopElement,
                    parent,
                )
            highlightElement(holder, staticErrorElement)
            return
        }

        (topField?.type ?: topMethod?.returnType)
            ?.let { parent = PsiParentClass(it) }
            ?: return

        val topElementType = parent.type
        val errorElement = inspectElements(blockElements, parent, topElementType)
        if (errorElement != null) highlightElement(holder, errorElement)
    }

    private fun highlightElement(
        holder: ProblemsHolder,
        highlight: HighlightingElementData,
    ) {
        val element = highlight.identify
        val parent = highlight.parent
        if (element is PsiErrorElement) return
        val project = element.project
        val highlightElm = element.originalElement
        val highlightRange =
            TextRange(0, element.textRange.length)

        if (parent != null) {
            setHighlightParameterBind(
                highlightRange,
                highlightElm,
                holder,
                parent.clazz?.name ?: (parent.type as? PsiClassType)?.name,
                project,
            )
        } else {
            setHighlightDaoMethodBind(
                highlightRange,
                highlightElm,
                holder,
                findDaoMethod(element.containingFile) ?: return,
                project,
            )
        }
    }

    private fun inspectElements(
        blockElements: List<PsiElement>,
        topParent: PsiParentClass,
        topElementType: PsiType,
    ): HighlightingElementData? {
        var parent = topParent
        var listTypeSearchIndex = 0
        for (eml in blockElements.drop(1)) {
            var isExist = false
            parent
                .findField(eml.text)
                ?.let { match ->
                    isExist = true
                    parent = PsiParentClass(match.type)
                }
            if (isExist) continue
            parent
                .findMethod(eml.text)
                ?.let { match ->
                    val returnType =
                        match.getMethodReturnType(topElementType, listTypeSearchIndex)
                            ?: return null
                    isExist = true
                    parent = PsiParentClass(returnType)
                }
            listTypeSearchIndex++
            if (!isExist) {
                return HighlightingElementData(eml, parent)
            }
        }
        return null
    }

    private fun inspectForElements(
        blockElements: List<PsiElement>,
        topParent: PsiParentClass,
        topElementType: PsiType,
    ): HighlightingElementData? {
        var parent = topParent
        var listTypeSearchIndex = 0
        var isExist = false
        for (eml in blockElements.drop(1)) {
            parent
                .findField(eml.text)
                ?.let { match ->
                    isExist = true
                    parent = PsiParentClass(match.type)
                }
            if (isExist) continue
            parent
                .findMethod(eml.text)
                ?.let { match ->
                    val returnType =
                        match.getMethodReturnType(topElementType, listTypeSearchIndex)
                            ?: return null
                    isExist = true
                    parent = PsiParentClass(returnType)
                }
            listTypeSearchIndex++
            if (!isExist) {
                return HighlightingElementData(eml, parent)
            }
        }
        return null
    }

    private fun setHighlightDaoMethodBind(
        highlightRange: TextRange,
        identify: PsiElement,
        holder: ProblemsHolder,
        daoMethod: PsiMethod,
        project: Project,
    ) {
        holder.registerProblem(
            identify,
            MessageBundle.message(
                "inspector.invalid.dao.parameter",
                daoMethod.name,
                identify.text ?: "",
            ),
            problemHighlightType(project, shortName),
            highlightRange,
        )
    }

    private fun setHighlightParameterBind(
        highlightRange: TextRange,
        identify: PsiElement,
        holder: ProblemsHolder,
        parentClassName: String?,
        project: Project,
    ) {
        holder.registerProblem(
            identify,
            MessageBundle.message(
                "inspector.invalid.class.property",
                parentClassName ?: "",
                identify.text ?: "",
            ),
            problemHighlightType(project, shortName),
            highlightRange,
        )
    }

    private fun getInspectionErrorLevel(
        project: Project,
        inspectionShortName: String,
    ): HighlightDisplayLevel? {
        val profileManager = InspectionProfileManager.getInstance(project)
        val currentProfile = profileManager.currentProfile
        val toolState: ToolsImpl? = currentProfile.getToolsOrNull(inspectionShortName, project)
        return toolState?.level
    }

    private fun problemHighlightType(
        project: Project,
        shortName: String,
    ) = when (
        getInspectionErrorLevel(
            project,
            shortName,
        )
    ) {
        HighlightDisplayLevel.Companion.ERROR -> ProblemHighlightType.ERROR
        HighlightDisplayLevel.Companion.WARNING -> ProblemHighlightType.WARNING
        else -> ProblemHighlightType.WARNING
    }
}
