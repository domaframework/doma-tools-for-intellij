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
package org.domaframework.doma.intellij.inspection.sql.inspector

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeafs
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.expr.fqdn
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.findNodeParent
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.getIterableClazz
import org.domaframework.doma.intellij.extension.psi.getMethodReturnType
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElNewExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.psi.SqlVisitor
import org.jetbrains.kotlin.idea.base.util.module

/**
 * Code inspection for SQL bind variables
 */
class SqlBindVariableValidInspector : LocalInspectionTool() {
    override fun getDisplayName(): String = "Match checking between SQL bind variables and Declaration"

    override fun getShortName(): String = "org.domaframework.doma.intellij.validBindVariable"

    override fun getGroupDisplayName(): String = "DomaTools"

    override fun isEnabledByDefault(): Boolean = true

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.Companion.ERROR

    data class BlockToken(
        val type: BlockType,
        val item: String,
        val position: Int,
    )

    enum class BlockType {
        FOR,
        IF,
        END,
    }

    override fun runForWholeFile(): Boolean = true

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): SqlVisitor {
        val topElm = holder.file.firstChild ?: return object : SqlVisitor() {}
        val directiveBlocks =
            topElm.nextLeafs
                .filter { elm ->
                    elm.elementType == SqlTypes.EL_FOR ||
                        elm.elementType == SqlTypes.EL_IF ||
                        elm.elementType == SqlTypes.EL_END
                }.map {
                    when (it.elementType) {
                        SqlTypes.EL_FOR -> {
                            val item = (it.parent as? SqlElForDirective)?.getForItem()
                            BlockToken(BlockType.FOR, item?.text ?: "for", item?.textOffset ?: 0)
                        }

                        SqlTypes.EL_IF -> BlockToken(BlockType.IF, "if", it.textOffset)
                        else -> BlockToken(BlockType.END, "end", it.textOffset)
                    }
                }

        return object : SqlVisitor() {
            override fun visitElStaticFieldAccessExpr(o: SqlElStaticFieldAccessExpr) {
                super.visitElStaticFieldAccessExpr(o)
                var file = o.containingFile ?: return
                var targetElement = o
                initInjectionElement(file, o.project, o.textOffset)
                    ?.let {
                        targetElement = it as SqlElStaticFieldAccessExpr
                        file = it.containingFile
                    }
                checkStaticFieldAndMethodAccess(targetElement, holder)
            }

            override fun visitElFieldAccessExpr(o: SqlElFieldAccessExpr) {
                super.visitElFieldAccessExpr(o)
                var file = o.containingFile ?: return
                var targetElement = o

                initInjectionElement(file, o.project, o.textOffset)
                    ?.let {
                        targetElement = it as SqlElFieldAccessExpr
                        file = it.containingFile
                    }

                // Get element inside block comment
                val blockElement =
                    PsiTreeUtil
                        .getChildrenOfTypeAsList(targetElement, PsiElement::class.java)
                        .filter {
                            it.elementType != SqlTypes.EL_DOT &&
                                it.elementType != SqlTypes.EL_LEFT_PAREN &&
                                it.elementType != SqlTypes.EL_RIGHT_PAREN &&
                                it.elementType != SqlTypes.EL_PARAMETERS
                        }.toTypedArray()
                val topElm = blockElement.firstOrNull() as SqlElPrimaryExpr

                // Exclude fixed Literal
                if (isLiteralOrStatic(topElm)) return
                // If inside a For block, search for IDENTIFY elements matching itself inside %for
                if (checkInForDirectiveBlock(topElm)) return
                checkAccessFieldAndMethod(holder, blockElement, file)
            }

            override fun visitElPrimaryExpr(o: SqlElPrimaryExpr) {
                super.visitElPrimaryExpr(o)
                var file = o.containingFile ?: return
                var targetElement = o
                val project = o.project
                initInjectionElement(file, o.project, o.textOffset)
                    ?.let {
                        targetElement = it as SqlElPrimaryExpr
                        file = it.containingFile
                    }

                // Exclude fixed Literal
                if (isLiteralOrStatic(targetElement)) return

                // TODO Check function parameters in the same way as bind variables.
                if (targetElement.findNodeParent(SqlTypes.EL_PARAMETERS) != null) return

                // For static property references, match against properties in the class definition
                if (targetElement.parent is SqlElStaticFieldAccessExpr) {
                    checkStaticFieldAndMethodAccess(
                        targetElement.parent as SqlElStaticFieldAccessExpr,
                        holder,
                    )
                    return
                }
                if (checkInForDirectiveBlock(targetElement)) return
                val identify =
                    PsiTreeUtil
                        .getChildrenOfType(targetElement, PsiElement::class.java)
                        ?.firstOrNull() ?: return
                val daoMethod = findDaoMethod(file) ?: return
                val params = daoMethod.methodParameters
                val validDaoParam =
                    params.firstOrNull { p ->
                        p.name == identify.text
                    }

                if (validDaoParam == null) {
                    val highlightElm = identify.originalElement ?: return
                    val highlightRange =
                        TextRange(0, identify.textRange?.length ?: 0)
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
                caretOffset: Int,
            ): PsiElement? =
                when (isJavaOrKotlinFileType(basePsiFile)) {
                    true ->
                        {
                            val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
                            injectedLanguageManager.findInjectedElementAt(basePsiFile, caretOffset)
                        }

                    false -> null
                }

            private fun checkInForDirectiveBlock(targetElement: PsiElement): Boolean {
                val forBlocks = getForDirectiveBlock(targetElement)
                val forItemNames = forBlocks.map { it.item }
                val targetName =
                    targetElement.text
                        .replace("_has_next", "")
                        .replace("_index", "")
                return forItemNames.contains(targetName)
            }

            /**
             * Count the `for`, `if`, and `end` elements from the beginning
             * to the target element (`targetElement`)
             * and obtain the `for` block information to which the `targetElement` belongs.
             */
            fun getForDirectiveBlock(targetElement: PsiElement): List<BlockToken> {
                val preBlocks =
                    directiveBlocks
                        .filter { it.position <= targetElement.textOffset }
                val stack = mutableListOf<BlockToken>()
                preBlocks.forEach { block ->
                    when (block.type) {
                        BlockType.FOR, BlockType.IF -> stack.add(block)
                        BlockType.END -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                    }
                }

                return stack.filter { it.type == BlockType.FOR }
            }

            private fun isLiteralOrStatic(targetElement: PsiElement): Boolean =
                (
                    targetElement.firstChild?.elementType == SqlTypes.EL_STRING ||
                        targetElement.firstChild?.elementType == SqlTypes.EL_NUMBER ||
                        targetElement.firstChild?.elementType == SqlTypes.EL_NULL ||
                        targetElement.firstChild?.elementType == SqlTypes.EL_BOOLEAN ||
                        targetElement.firstChild is SqlElNewExpr ||
                        targetElement.text.startsWith("@")
                )

            private fun checkAccessFieldAndMethod(
                holder: ProblemsHolder,
                blockElement: Array<out PsiElement>,
                file: PsiFile,
            ) {
                val topElement: PsiElement = blockElement.firstOrNull() ?: return

                val daoMethod = findDaoMethod(file) ?: return
                val params = daoMethod.methodParameters
                val validDaoParam =
                    params.firstOrNull { p ->
                        p.name == topElement.text
                    }

                // Check the match with the Dao method argument of the top element
                // Do not perform subsequent processing if it is a fixed character or non-existent variable name
                val validTopElement =
                    validateDaoParam(topElement, validDaoParam, holder, daoMethod)
                if (validTopElement && validDaoParam != null) {
                    val parentClass =
                        validDaoParam.getIterableClazz(daoMethod.getDomaAnnotationType())
                    inspectElements(
                        blockElement.toList(),
                        parentClass,
                        parentClass.type,
                        holder,
                    )
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
                    highlightElement(staticTopElement, holder, parent)
                    return
                }

                (topField?.type ?: topMethod?.returnType)
                    ?.let { parent = PsiParentClass(it) }
                    ?: return

                val topElementType = parent.type
                inspectElements(blockElements, parent, topElementType, holder)
            }

            private fun highlightElement(
                element: PsiElement,
                holder: ProblemsHolder,
                parent: PsiParentClass,
            ) {
                if (element is PsiErrorElement) return
                val project = element.project
                val highlightElm = element.originalElement
                val highlightRange =
                    TextRange(0, element.textRange.length)
                setHighlightParameterBind(
                    highlightRange,
                    highlightElm,
                    holder,
                    parent.clazz?.name ?: (parent.type as? PsiClassReferenceType)?.name,
                    project,
                )
            }

            fun inspectElements(
                blockElements: List<PsiElement>,
                topParent: PsiParentClass,
                topElementType: PsiType,
                holder: ProblemsHolder,
            ) {
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
                                    ?: return
                            isExist = true
                            parent = PsiParentClass(returnType)
                        }
                    listTypeSearchIndex++
                    if (!isExist) {
                        highlightElement(eml, holder, parent)
                        return
                    }
                }
            }

            /**
             * Verify element top matches Dao method argument
             * Even if the name is not in the argument, a specific string will not cause an error.
             * @return present in the Dao argument or declared as a for block item
             */
            private fun validateDaoParam(
                element: PsiElement,
                validDaoParam: PsiParameter?,
                holder: ProblemsHolder,
                daoMethod: PsiMethod,
            ): Boolean {
                if (validDaoParam == null) {
                    if (checkInForDirectiveBlock(element)) return true
                    val highlightRange = TextRange(0, element.textRange.length)
                    setHighlightDaoMethodBind(highlightRange, element, holder, daoMethod, element.project)
                    return false
                }
                return true
            }
        }
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
            problemHighlightType(project, this.shortName),
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
            problemHighlightType(project, this.shortName),
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
