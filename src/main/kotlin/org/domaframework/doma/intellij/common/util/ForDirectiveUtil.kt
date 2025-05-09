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
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.findStaticField
import org.domaframework.doma.intellij.extension.psi.findStaticMethod
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.getForItemDeclaration
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

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
                        val directiveBlocks =
                            PsiTreeUtil
                                .findChildrenOfType(file, PsiElement::class.java)
                                .filter { elm ->
                                    (
                                        elm.elementType == SqlTypes.EL_FOR ||
                                            elm.elementType == SqlTypes.EL_IF ||
                                            elm.elementType == SqlTypes.EL_END
                                    )
                                }.sortedBy { it.textOffset }
                                .map {
                                    when (it.elementType) {
                                        SqlTypes.EL_FOR -> {
                                            val item =
                                                (it.parent as? SqlElForDirective)?.getForItem()
                                            BlockToken(
                                                BlockType.FOR,
                                                item ?: it,
                                                item?.textOffset ?: 0,
                                            )
                                        }

                                        SqlTypes.EL_IF ->
                                            BlockToken(
                                                BlockType.IF,
                                                it,
                                                it.textOffset,
                                            )

                                        else -> BlockToken(BlockType.END, it, it.textOffset)
                                    }
                                }
                        var stack = mutableListOf<BlockToken>()
                        val filterPosition =
                            if (skipSelf) {
                                directiveBlocks.filter {
                                    it.position < targetElement.textOffset
                                }
                            } else {
                                directiveBlocks.filter { it.position <= targetElement.textOffset }
                            }
                        filterPosition.forEach { block ->
                            when (block.type) {
                                BlockType.FOR, BlockType.IF -> stack.add(block)
                                BlockType.END -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                            }
                        }
                        if (skipSelf) {
                            stack =
                                stack
                                    .filter { !isSameForDirective(it.item, targetElement) }
                                    .toMutableList()
                        }

                        CachedValueProvider.Result.create(
                            stack.filter { it.type == BlockType.FOR },
                            file,
                        )
                    }
                }
            return cachedValue.value
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
            val searchText = targetElement.text.replace("_has_next", "").replace("_index", "")
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
        ): PsiParentClass? {
            // Get the type of the top for directive definition element
            // Defined in Dao parameters or static property calls
            if (forDirectiveBlocks.isEmpty()) return null
            var parentClassType =
                getTopForDirectiveDeclarationClassType(
                    forDirectiveBlocks.first().item,
                    project,
                ) ?: return null
            forDirectiveBlocks.drop(1).forEach { directive ->
                // Get the definition type of the target directive
                val formItem = ForItem(directive.item)
                val forDirectiveExpr = formItem.getParentForDirectiveExpr()
                val forDirectiveDeclaration = forDirectiveExpr?.getForItemDeclaration()
                if (forDirectiveDeclaration != null) {
                    val forItemDeclarationBlocks = forDirectiveDeclaration.getDeclarationChildren()
                    getFieldAccessLastPropertyClassType(
                        forItemDeclarationBlocks,
                        project,
                        parentClassType,
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
                            nestClass?.let { parentClassType = PsiParentClass(it) }
                        },
                    )
                }
            }

            return parentClassType
        }

        fun getTopForDirectiveDeclarationClassType(
            topForDirectiveItem: PsiElement,
            project: Project,
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
                    fieldAccessTopParentClass =
                        getStaticFieldAccessTopElementClassType(
                            staticFieldAccessExpr,
                            referenceClazz,
                        )
                } else {
                    // Defined by Dao parameter
                    val file = topForDirectiveItem.containingFile ?: return null
                    val daoMethod = findDaoMethod(file) ?: return null
                    val topElementText =
                        forDirectiveDeclaration.getDeclarationChildren().firstOrNull()?.text
                            ?: return null
                    isBatchAnnotation = PsiDaoMethod(project, daoMethod).daoType.isBatchAnnotation()

                    val matchParam = daoMethod.findParameter(cleanString(topElementText))
                    val daoParamType = matchParam?.type ?: return null
                    fieldAccessTopParentClass = PsiParentClass(daoParamType)
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
        ): PsiParentClass? {
            val topElement = staticFieldAccessExpr.elIdExprList.firstOrNull() ?: return null
            val searchText = cleanString(topElement.text)
            if (topElement.nextSibling?.elementType != SqlTypes.EL_PARAMETERS) {
                val topPropertyField = referenceClazz.findStaticField(searchText)
                topPropertyField?.type?.let {
                    return PsiParentClass(it)
                }
            } else {
                val topPropertyMethod = referenceClazz.findStaticMethod(searchText)
                topPropertyMethod?.let {
                    val returnType = it.returnType ?: return null
                    return PsiParentClass(returnType)
                }
            }
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
            var parent =
                if (isBatchAnnotation) {
                    val parentType = topParent.type
                    val nextClassType = parentType as? PsiClassType ?: return null
                    val nestType = nextClassType.parameters.firstOrNull() ?: return null
                    PsiParentClass(nestType)
                } else {
                    topParent
                }
            val parentType = parent.type
            // TODO: Display an error message that the property cannot be called.
            val classType = parentType as? PsiClassType ?: return null

            var competeResult: ValidationCompleteResult? = null

            val searchBlocks = blocks.drop(1).dropLast(dropLastIndex)
            if (dropLastIndex > 0 && searchBlocks.isEmpty()) {
                complete.invoke(parent)
                return ValidationCompleteResult(
                    blocks.last(),
                    parent,
                )
            }

            // When a List type element is used as the parent,
            // the original declared type is retained and the referenced type is obtained by nesting.
            var parentListBaseType: PsiType? =
                if (PsiClassTypeUtil.Companion.isIterableType(classType, project)) {
                    parentType
                } else {
                    null
                }
            var nestIndex = 0

            // Analyze a block element and get the type of the last called property
            for (element in searchBlocks) {
                val searchElm = cleanString(getSearchElementText(element))
                if (searchElm.isEmpty()) {
                    complete.invoke(parent)
                    return ValidationCompleteResult(
                        element,
                        parent,
                    )
                }

                val field =
                    parent
                        .findField(searchElm)
                        ?.let { match ->
                            val type =
                                parentListBaseType?.let {
                                    PsiClassTypeUtil.Companion.getParameterType(
                                        project,
                                        match.type,
                                        it,
                                        nestIndex,
                                    )
                                }
                                    ?: match.type
                            val classType = type as? PsiClassType
                            if (classType != null &&
                                PsiClassTypeUtil.Companion.isIterableType(
                                    classType,
                                    element.project,
                                )
                            ) {
                                parentListBaseType = type
                                nestIndex = 0
                            }
                            findFieldMethod?.invoke(type)
                        }
                val method =
                    parent
                        .findMethod(searchElm)
                        ?.let { match ->
                            val returnType = match.returnType ?: return null
                            val methodReturnType =
                                parentListBaseType?.let {
                                    PsiClassTypeUtil.Companion.getParameterType(
                                        project,
                                        returnType,
                                        it,
                                        nestIndex,
                                    )
                                }
                                    ?: returnType
                            val classType = methodReturnType as? PsiClassType
                            if (classType != null &&
                                PsiClassTypeUtil.Companion.isIterableType(
                                    classType,
                                    element.project,
                                )
                            ) {
                                parentListBaseType = methodReturnType
                                nestIndex = 0
                            }
                            findFieldMethod?.invoke(methodReturnType)
                        }
                nestIndex++
                if (field == null && method == null) {
                    return ValidationPropertyResult(
                        element,
                        parent,
                        shortName,
                    )
                }

                if (field != null && element.nextSibling !is SqlElParameters) {
                    parent = field
                    competeResult =
                        ValidationCompleteResult(
                            element,
                            parent,
                        )
                } else if (method != null) {
                    parent = method
                    competeResult =
                        ValidationCompleteResult(
                            element,
                            parent,
                        )
                }
            }
            complete.invoke(parent)
            return competeResult
        }

        private fun getSearchElementText(elm: PsiElement): String =
            if (elm is SqlElIdExpr || elm.elementType == SqlTypes.EL_IDENTIFIER) {
                elm.text
            } else {
                ""
            }

        fun resolveForDirectiveItemClassTypeBySuffixElement(searchName: String): PsiType? =
            if (searchName.endsWith("_has_next")) {
                PsiTypes.booleanType()
            } else if (searchName.endsWith("_index")) {
                PsiTypes.intType()
            } else {
                null
            }
    }
}
