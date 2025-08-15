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
package org.domaframework.doma.intellij.common.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationItem
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.validation.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.validation.result.ValidationNotFoundStaticPropertyResult
import org.domaframework.doma.intellij.common.validation.result.ValidationNotFoundTopTypeResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.findStaticField
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.getForItemDeclaration
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.toml.lang.psi.ext.elementType

private typealias FieldAccessContext = FieldMethodResolver.ResolveContext

class ForDirectiveUtil {
    data class BlockToken(
        val type: BlockType,
        val item: PsiElement,
        val position: Int,
    )

    enum class BlockType {
        FOR,
        IF,
        END,
    }

    companion object {
        private val cachedForDirectiveBlocks: MutableMap<PsiElement, CachedValue<List<BlockToken>>> =
            mutableMapOf()

        const val HAS_NEXT_PREFIX = "_has_next"
        const val INDEX_PREFIX = "_index"

        /**
         *  Get the parent for directive list to which this directive belongs
         * @param targetElement Element to search for definer
         * @param skipSelf Pass true if the for directory definition element name itself is to search for the definition source.
         * @return parent for directive list to which this directive belongs
         */
        fun getForDirectiveBlocks(
            targetElement: PsiElement,
            skipSelf: Boolean = true,
        ): List<BlockToken> {
            val cachedValue =
                cachedForDirectiveBlocks.getOrPut(targetElement) {
                    CachedValuesManager.getManager(targetElement.project).createCachedValue {
                        val file = targetElement.containingFile ?: return@createCachedValue null
                        val directiveBlocks = extractDirectiveBlocks(file)
                        val stack = buildDirectiveStack(directiveBlocks, targetElement, skipSelf)

                        CachedValueProvider.Result.create(
                            stack.filter { it.type == BlockType.FOR },
                            file,
                        )
                    }
                }
            return cachedValue.value
        }

        private fun extractDirectiveBlocks(file: PsiElement): List<BlockToken> =
            PsiTreeUtil
                .findChildrenOfType(file, PsiElement::class.java)
                .filter { isDirectiveElement(it) }
                .sortedBy { it.textOffset }
                .map { createBlockToken(it) }

        private fun isDirectiveElement(element: PsiElement): Boolean =
            element.elementType == SqlTypes.EL_FOR ||
                element.elementType == SqlTypes.EL_IF ||
                element.elementType == SqlTypes.EL_END

        private fun createBlockToken(element: PsiElement): BlockToken =
            when (element.elementType) {
                SqlTypes.EL_FOR -> {
                    val item = (element.parent as? SqlElForDirective)?.getForItem()
                    BlockToken(
                        BlockType.FOR,
                        item ?: element,
                        item?.textOffset ?: 0,
                    )
                }
                SqlTypes.EL_IF ->
                    BlockToken(
                        BlockType.IF,
                        element,
                        element.textOffset,
                    )
                else -> BlockToken(BlockType.END, element, element.textOffset)
            }

        private fun buildDirectiveStack(
            directiveBlocks: List<BlockToken>,
            targetElement: PsiElement,
            skipSelf: Boolean,
        ): List<BlockToken> {
            val stack = mutableListOf<BlockToken>()
            val positionThreshold = if (skipSelf) targetElement.textOffset else targetElement.textOffset + 1

            directiveBlocks
                .filter { it.position < positionThreshold }
                .forEach { block ->
                    when (block.type) {
                        BlockType.FOR, BlockType.IF -> stack.add(block)
                        BlockType.END -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                    }
                }

            return if (skipSelf) {
                stack.filter { !isSameForDirective(it.item, targetElement) }
            } else {
                stack
            }
        }

        private fun isSameForDirective(
            token: PsiElement,
            targetElement: PsiElement,
        ): Boolean =
            PsiTreeUtil.getParentOfType(
                token,
                SqlElForDirective::class.java,
            ) ==
                PsiTreeUtil.getParentOfType(
                    targetElement,
                    SqlElForDirective::class.java,
                )

        fun findForItem(
            targetElement: PsiElement,
            skipSelf: Boolean = true,
            forDirectives: List<BlockToken> = getForDirectiveBlocks(targetElement, skipSelf),
        ): PsiElement? {
            val searchText = targetElement.text.replace(HAS_NEXT_PREFIX, "").replace(INDEX_PREFIX, "")
            return forDirectives.firstOrNull { it.item.text == searchText }?.item
        }

        /**
         * for directive Loop through a list and get its own defined type
         * Used to get the type information of the top of the bind variable or the for item itself
         * @param project Project
         * @param forDirectiveBlocks List of for directive blocks
         */
        fun getForDirectiveItemClassType(
            project: Project,
            forDirectiveBlocks: List<BlockToken>,
            targetForItem: PsiElement? = null,
        ): PsiParentClass? {
            if (forDirectiveBlocks.isEmpty()) return null

            val topDirectiveItem = forDirectiveBlocks.first().item
            val file = topDirectiveItem.containingFile ?: return null
            val daoMethod = findDaoMethod(file)

            var parentClassType =
                getTopForDirectiveDeclarationClassType(
                    forDirectiveBlocks.first().item,
                    project,
                    daoMethod,
                )

            for (directive in forDirectiveBlocks.drop(1)) {
                val result =
                    processDirective(
                        directive,
                        targetForItem,
                        forDirectiveBlocks,
                        daoMethod,
                        project,
                        parentClassType,
                    )

                if (result.shouldReturn) {
                    return result.parentClassType
                }
                parentClassType = result.parentClassType
            }

            return parentClassType
        }

        private data class DirectiveProcessResult(
            val parentClassType: PsiParentClass?,
            val shouldReturn: Boolean = false,
        )

        private fun processDirective(
            directive: BlockToken,
            targetForItem: PsiElement?,
            forDirectiveBlocks: List<BlockToken>,
            daoMethod: PsiMethod?,
            project: Project,
            currentParentClassType: PsiParentClass?,
        ): DirectiveProcessResult {
            val formItem = ForItem(directive.item)

            if (targetForItem != null && formItem.element.textOffset > targetForItem.textOffset) {
                return DirectiveProcessResult(currentParentClassType, shouldReturn = true)
            }

            val forDirectiveExpr = formItem.getParentForDirectiveExpr()
            val forDirectiveDeclaration =
                forDirectiveExpr?.getForItemDeclaration()
                    ?: return DirectiveProcessResult(currentParentClassType)

            var updatedParentClassType =
                resolveParentClassType(
                    forDirectiveDeclaration,
                    forDirectiveBlocks,
                    daoMethod,
                    project,
                    currentParentClassType,
                )

            if (updatedParentClassType != null) {
                updatedParentClassType =
                    processDeclarationBlocks(
                        forDirectiveDeclaration,
                        daoMethod,
                        project,
                        updatedParentClassType,
                    )

                if (targetForItem != null && formItem.element.text == targetForItem.text) {
                    return DirectiveProcessResult(updatedParentClassType, shouldReturn = true)
                }
            }

            return DirectiveProcessResult(updatedParentClassType)
        }

        private fun resolveParentClassType(
            forDirectiveDeclaration: Any,
            forDirectiveBlocks: List<BlockToken>,
            daoMethod: PsiMethod?,
            project: Project,
            currentParentClassType: PsiParentClass?,
        ): PsiParentClass? {
            val declaration = forDirectiveDeclaration as? ForDeclarationItem
            val declarationTopElement =
                declaration?.getDeclarationChildren()?.first()
                    ?: return currentParentClassType

            val findDeclarationForItem = findForItem(declarationTopElement, forDirectives = forDirectiveBlocks)

            if (findDeclarationForItem == null && daoMethod != null) {
                val matchParam = daoMethod.findParameter(declarationTopElement.text)
                if (matchParam != null) {
                    val convertOptional = PsiClassTypeUtil.convertOptionalType(matchParam.type, project)
                    return PsiParentClass(convertOptional)
                }
            }

            return currentParentClassType
        }

        private fun processDeclarationBlocks(
            forDirectiveDeclaration: Any,
            daoMethod: PsiMethod?,
            project: Project,
            parentClassType: PsiParentClass,
        ): PsiParentClass? {
            val isBatchAnnotation =
                daoMethod?.let {
                    PsiDaoMethod(project, it).daoType.isBatchAnnotation()
                } == true

            val declaration = forDirectiveDeclaration as? ForDeclarationItem
            val forItemDeclarationBlocks =
                declaration?.getDeclarationChildren()
                    ?: return parentClassType

            var resultParentClassType: PsiParentClass? = parentClassType

            getFieldAccessLastPropertyClassType(
                forItemDeclarationBlocks,
                project,
                parentClassType,
                isBatchAnnotation = isBatchAnnotation,
                complete = { lastType ->
                    resultParentClassType = extractNestedClassType(lastType, project)
                },
            )

            return resultParentClassType
        }

        private fun extractNestedClassType(
            lastType: PsiParentClass,
            project: Project,
        ): PsiParentClass? {
            val classType = lastType.type as? PsiClassType ?: return null

            if (PsiClassTypeUtil.isIterableType(classType, project)) {
                val nestClass = classType.parameters.firstOrNull()
                return nestClass?.let { PsiParentClass(it) }
            }

            return null
        }

        fun getTopForDirectiveDeclarationClassType(
            topForDirectiveItem: PsiElement,
            project: Project,
            daoMethod: PsiMethod?,
        ): PsiParentClass? {
            var result: PsiParentClass? = null
            var fieldAccessTopParentClass: PsiParentClass? = null

            val forDirectiveExpr =
                PsiTreeUtil.getParentOfType(topForDirectiveItem, SqlElForDirective::class.java)
            val forDirectiveDeclaration = forDirectiveExpr?.getForItemDeclaration()
            if (forDirectiveDeclaration != null) {
                var isBatchAnnotation = false
                val forItemDeclarationBlocks =
                    if (forDirectiveDeclaration.element is SqlElStaticFieldAccessExpr) {
                        val staticFieldAccessExpr =
                            forDirectiveDeclaration.element as SqlElStaticFieldAccessExpr
                        staticFieldAccessExpr.accessElements
                    } else {
                        forDirectiveDeclaration.getDeclarationChildren()
                    }

                // Defined by StaticFieldAccess
                if (forDirectiveDeclaration.element is SqlElStaticFieldAccessExpr) {
                    val file = topForDirectiveItem.containingFile
                    val staticFieldAccessExpr =
                        forDirectiveDeclaration.element as SqlElStaticFieldAccessExpr
                    val clazz = staticFieldAccessExpr.elClass
                    val staticElement = PsiStaticElement(clazz.elIdExprList, file)
                    val referenceClazz = staticElement.getRefClazz() ?: return null

                    // In the case of staticFieldAccess, the property that is called first is retrieved.
                    val staticContext =
                        getStaticFieldAccessTopElementClassType(
                            staticFieldAccessExpr,
                            referenceClazz,
                        )
                    if (staticContext?.validationResult != null) {
                        return null
                    }
                    fieldAccessTopParentClass = staticContext?.parent
                } else {
                    // Defined by DAO parameter
                    if (daoMethod == null) return null

                    val topElementText =
                        forDirectiveDeclaration.getDeclarationChildren().firstOrNull()?.text
                            ?: return null
                    isBatchAnnotation = PsiDaoMethod(project, daoMethod).daoType.isBatchAnnotation()

                    val matchParam = daoMethod.findParameter(cleanString(topElementText))
                    val daoParamType = matchParam?.type ?: return null
                    fieldAccessTopParentClass = PsiParentClass(PsiClassTypeUtil.convertOptionalType(daoParamType, project))
                }
                fieldAccessTopParentClass?.let {
                    getFieldAccessLastPropertyClassType(
                        forItemDeclarationBlocks,
                        topForDirectiveItem.project,
                        it,
                        isBatchAnnotation = isBatchAnnotation,
                        complete = { lastType ->
                            val classType = lastType.type as? PsiClassType
                            val nestClass =
                                if (classType != null &&
                                    PsiClassTypeUtil.Companion.isIterableType(classType, project)
                                ) {
                                    classType.parameters.firstOrNull()
                                } else {
                                    null
                                }
                            nestClass?.let { result = PsiParentClass(it) }
                        },
                    )
                }
            }
            return result
        }

        fun getStaticFieldAccessTopElementClassType(
            staticFieldAccessExpr: SqlElStaticFieldAccessExpr,
            referenceClazz: PsiClass,
            shortName: String = "",
        ): FieldAccessContext? {
            val topElement = staticFieldAccessExpr.elIdExprList.firstOrNull() ?: return null
            val searchText = cleanString(topElement.text)
            val prentClazz = PsiParentClass(referenceClazz.psiClassType)
            val context =
                FieldAccessContext(
                    parent = prentClazz,
                    parentListBaseType = null,
                    nestIndex = 0,
                    completeResult = null,
                    validationResult = null,
                )
            val parametersExpr = PsiTreeUtil.nextLeaf(topElement)?.parent as? SqlElParameters
            if (parametersExpr == null) {
                val topPropertyField = referenceClazz.findStaticField(searchText)

                topPropertyField?.type?.let {
                    context.parent = PsiParentClass(it)
                    return context
                }
            } else {
                val resolveResult =
                    FieldMethodResolver.resolveStaticMethod(
                        topElement,
                        prentClazz,
                        topElement.text,
                        parametersExpr,
                        topElement.project,
                        shortName,
                    )
                if (resolveResult.type != null) {
                    context.parent = resolveResult.type
                    return context
                } else if (resolveResult.validation != null) {
                    context.validationResult = resolveResult.validation
                    return context
                } else {
                    context.validationResult =
                        ValidationNotFoundStaticPropertyResult(
                            topElement,
                            staticFieldAccessExpr.elClass,
                            shortName,
                        )
                    return context
                }
            }
            context.validationResult =
                ValidationNotFoundStaticPropertyResult(
                    topElement,
                    staticFieldAccessExpr.elClass,
                    shortName,
                )
            return null
        }

        /**
         * Analyzes the element's field accesses and gets the type of the last property being called
         * @param blocks List of elements to be analyzed
         * @param project Project
         * @param topParent The parent class of the element to be analyzed(DaoParam, forDirectiveItem, StaticFieldAccess)
         * @param shortName Inspection class shortName
         * @param dropLastIndex The number of elements to exclude from the end of the block to search
         * @param findFieldMethod Callback when property is found
         * @param complete Callback when the analysis is completed
         * @return ValidationResult
         */
        fun getFieldAccessLastPropertyClassType(
            blocks: List<PsiElement>,
            project: Project,
            topParent: PsiParentClass,
            isBatchAnnotation: Boolean = false,
            shortName: String = "",
            dropLastIndex: Int = 0,
            findFieldMethod: ((PsiType) -> PsiParentClass)? = { type -> PsiParentClass(type) },
            complete: ((PsiParentClass) -> Unit) = { parent: PsiParentClass? -> },
        ): ValidationResult? {
            val initialParent = resolveInitialParent(topParent, project, isBatchAnnotation) ?: return null
            val parentType = PsiClassTypeUtil.convertOptionalType(initialParent.type, project)
            val classType =
                parentType as? PsiClassType
                    ?: return ValidationNotFoundTopTypeResult(blocks.first(), shortName)

            val searchBlocks = blocks.drop(1).dropLast(dropLastIndex)
            if (dropLastIndex > 0 && searchBlocks.isEmpty()) {
                complete.invoke(initialParent)
                return ValidationCompleteResult(blocks.last(), initialParent)
            }

            val context =
                FieldAccessContext(
                    parent = initialParent,
                    parentListBaseType =
                        if (PsiClassTypeUtil.isIterableType(classType, project)) {
                            PsiClassTypeUtil.convertOptionalType(parentType, project)
                        } else {
                            null
                        },
                    nestIndex = 0,
                    completeResult = null,
                    validationResult = null,
                )

            val finalContext =
                processFieldAccessBlocks(
                    searchBlocks,
                    context,
                    project,
                    shortName,
                    findFieldMethod,
                )

            complete.invoke(finalContext.parent)
            return finalContext.completeResult ?: finalContext.validationResult
        }

        private fun resolveInitialParent(
            topParent: PsiParentClass,
            project: Project,
            isBatchAnnotation: Boolean,
        ): PsiParentClass? {
            return if (isBatchAnnotation) {
                val parentType = PsiClassTypeUtil.convertOptionalType(topParent.type, project)
                val nextClassType = parentType as? PsiClassType ?: return null
                val nestType = nextClassType.parameters.firstOrNull() ?: return null
                PsiParentClass(PsiClassTypeUtil.convertOptionalType(nestType, project))
            } else {
                val convertOptional = PsiClassTypeUtil.convertOptionalType(topParent.type, project)
                PsiParentClass(convertOptional)
            }
        }

        private fun processFieldAccessBlocks(
            searchBlocks: List<PsiElement>,
            context: FieldAccessContext,
            project: Project,
            shortName: String,
            findFieldMethod: ((PsiType) -> PsiParentClass)?,
        ): FieldAccessContext {
            for (element in searchBlocks) {
                val searchElm = cleanString(getSearchElementText(element))
                if (searchElm.isEmpty()) {
                    context.completeResult = ValidationCompleteResult(element, context.parent)
                    return context
                }

                val field = FieldMethodResolver.resolveField(context, searchElm, project)
                val methodResult = FieldMethodResolver.resolveMethod(context, element, searchElm, project, shortName)

                context.nestIndex++
                if (field == null && methodResult.type == null) {
                    context.completeResult = null
                    return context
                }

                val method = methodResult.type
                findFieldMethod?.invoke(field?.type ?: method?.type ?: context.parent.type)
                updateParentContext(element, field, method, context)
            }
            return context
        }

        private fun updateParentContext(
            element: PsiElement,
            field: PsiParentClass?,
            method: PsiParentClass?,
            context: FieldAccessContext,
        ) {
            if (field != null && element.nextSibling !is SqlElParameters) {
                context.parent = field
                context.completeResult = ValidationCompleteResult(element, context.parent)
            } else if (method != null) {
                context.parent = method
                context.completeResult = ValidationCompleteResult(element, context.parent)
            }
        }

        private fun getSearchElementText(elm: PsiElement): String =
            if (elm is SqlElIdExpr || elm.elementType == SqlTypes.EL_IDENTIFIER) {
                elm.text
            } else {
                ""
            }

        fun resolveForDirectiveItemClassTypeBySuffixElement(searchName: String): PsiType? =
            if (searchName.endsWith(HAS_NEXT_PREFIX)) {
                PsiTypes.booleanType()
            } else if (searchName.endsWith(INDEX_PREFIX)) {
                PsiTypes.intType()
            } else {
                null
            }
    }
}
