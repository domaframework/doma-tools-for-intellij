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
package org.domaframework.doma.intellij.inspection

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationDaoBaseItem
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationItem
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationStaticFieldAccessorItem
import org.domaframework.doma.intellij.common.sql.foritem.ForDirectiveItemBase
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.sql.validator.SqlElForItemFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.getForItemDeclaration
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class ForDirectiveInspection(
    private val daoMethod: PsiMethod,
    private val shortName: String = "",
) {
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

    private val cachedForDirectiveBlocks: MutableMap<PsiElement, CachedValue<List<BlockToken>>> =
        mutableMapOf()

    fun getForItem(targetElement: PsiElement): ForItem? {
        val forBlocks = getForDirectiveBlock(targetElement)
        return getForItem(targetElement, forBlocks)
    }

    fun getForItem(
        targetElement: PsiElement,
        forBlocks: List<BlockToken>,
    ): ForItem? {
        val targetName =
            targetElement.text
                .replace("_has_next", "")
                .replace("_index", "")
        val forItem = forBlocks.lastOrNull { it.item.text == targetName }
        forItem?.let { return ForItem(it.item) } ?: return null
    }

    /**
     * Analyze the field access of the for item definition and finally get the declared type
     * @return [ValidationResult] is used to display the analysis results.
     */
    fun validateFieldAccessByForItem(blockElements: List<PsiElement>): ValidationResult? {
        val targetElement: PsiElement = blockElements.firstOrNull() ?: return null
        val topElm = blockElements.first()

        val forDirectives = getForDirectiveBlock(targetElement)
        val forItem = getForItem(targetElement, forDirectives) ?: return createErrorResult(targetElement)
        val domaAnnotationType = daoMethod.getDomaAnnotationType()

        val declarationItem = getDeclarationTopItem(forItem, 0)
        if (declarationItem !is ForDeclarationDaoBaseItem) return createErrorResult(targetElement)

        val daoParamDeclarativeType =
            declarationItem.getPsiParentClass()
                ?: return ValidationDaoParamResult(targetElement, daoMethod.name, shortName)

        val initialType = daoParamDeclarativeType.type as? PsiClassType
        if (initialType == null || !PsiClassTypeUtil.isIterableType(initialType, topElm.project)) return null

        val finalType = analyzeNestedForDirectives(forDirectives, initialType, domaAnnotationType.isBatchAnnotation(), topElm)
        return finalType?.let { ValidationCompleteResult(topElm, PsiParentClass(it)) } ?: createErrorResult(targetElement)
    }

    private fun createErrorResult(targetElement: PsiElement): ValidationResult = ValidationDaoParamResult(targetElement, "", shortName)

    private fun analyzeNestedForDirectives(
        forDirectives: List<BlockToken>,
        initialType: PsiClassType,
        isBatchAnnotation: Boolean,
        topElm: PsiElement,
    ): PsiClassType? {
        var nestClassType: PsiClassType? = if (isBatchAnnotation) initialType.parameters.firstOrNull() as? PsiClassType else initialType
        var listIndex = 1

        for ((i, targetForDirective) in forDirectives.withIndex()) {
            if (nestClassType == null) break
            val targetDirectiveParent =
                PsiTreeUtil.getParentOfType(
                    targetForDirective.item,
                    SqlElForDirective::class.java,
                )
            val targetElementParent =
                PsiTreeUtil.getParentOfType(
                    topElm,
                    SqlElForDirective::class.java,
                )
            if (targetDirectiveParent == targetElementParent) continue

            val currentForItem = ForItem(targetForDirective.item)
            val currentDeclaration = currentForItem.getParentForDirectiveExpr()?.getForItemDeclaration() ?: continue

            val declarationType = processDeclarationElement(currentDeclaration, nestClassType, i)
            if (declarationType != null) {
                if (!PsiClassTypeUtil.isIterableType(declarationType, topElm.project)) {
                    return null
                }
                nestClassType = declarationType.parameters.firstOrNull() as? PsiClassType
                listIndex = 1
            } else {
                nestClassType = processListType(nestClassType, listIndex, topElm)
                listIndex++
            }
        }
        return nestClassType
    }

    private fun processDeclarationElement(
        currentDeclaration: ForDeclarationItem,
        nestClassType: PsiClassType,
        index: Int,
    ): PsiClassType? {
        val declarationElement = currentDeclaration.element
        if (declarationElement is SqlElStaticFieldAccessExpr) {
            val forItem = ForDeclarationStaticFieldAccessorItem(declarationElement.accessElements, declarationElement, index)
            val declarationType = forItem.getPsiParentClass()?.type as? PsiClassType
            return declarationType
        }

        val declarationChildren = currentDeclaration.getDeclarationChildren()
        if (declarationChildren.size > 1) {
            val validator = SqlElForItemFieldAccessorChildElementValidator(declarationChildren, PsiParentClass(nestClassType), shortName)
            return validator.validateChildren()?.parentClass?.type as? PsiClassType
        }
        return null
    }

    private fun processListType(
        nestClassType: PsiClassType,
        listIndex: Int,
        topElm: PsiElement,
    ): PsiClassType? {
        var currentType: PsiClassType? = nestClassType
        repeat(listIndex) {
            currentType?.let { type ->
                if (PsiClassTypeUtil.isIterableType(type, topElm.project)) {
                    currentType = type.parameters.firstOrNull() as? PsiClassType
                }
            }
        }
        return currentType
    }

    fun getForDirectiveBlockSize(target: PsiElement): Int = getForDirectiveBlock(target).size

    /**
     * Count the `for`, `if`, and `end` elements from the beginning
     * to the target element (`targetElement`)
     * and obtain the `for` block information to which the `targetElement` belongs.
     */
    private fun getForDirectiveBlock(targetElement: PsiElement): List<BlockToken> {
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
                                        val item = (it.parent as? SqlElForDirective)?.getForItem()
                                        BlockToken(
                                            BlockType.FOR,
                                            item ?: it,
                                            item?.textOffset ?: 0,
                                        )
                                    }

                                    SqlTypes.EL_IF -> BlockToken(BlockType.IF, it, it.textOffset)
                                    else -> BlockToken(BlockType.END, it, it.textOffset)
                                }
                            }
                    val stack = mutableListOf<BlockToken>()
                    val filterPosition = directiveBlocks.filter { it.position < targetElement.textOffset }
                    filterPosition.forEach { block ->
                        when (block.type) {
                            BlockType.FOR, BlockType.IF -> stack.add(block)
                            BlockType.END -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                        }
                    }
                    directiveBlocks.forEach { block ->
                        when (block.type) {
                            BlockType.FOR, BlockType.IF -> stack.add(block)
                            BlockType.END -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                        }
                    }

                    CachedValueProvider.Result.create(
                        stack.filter { it.type == BlockType.FOR },
                        file,
                    )
                }
            }
        return cachedValue.value
    }

    /***
     * Get the top element to define the item.
     */
    private fun getDeclarationTopItem(
        forItem: ForItem,
        searchIndex: Int = 0,
    ): ForDirectiveItemBase? {
        val forDirectiveParent = forItem.getParentForDirectiveExpr() ?: return null
        val declarationSideElement =
            forDirectiveParent.getForItemDeclaration() ?: return null
        val declarationElement = declarationSideElement.element
        if (declarationElement is SqlElStaticFieldAccessExpr) {
            return ForDeclarationStaticFieldAccessorItem(declarationElement.accessElements, declarationElement, searchIndex)
        }

        val declarationFieldElements = declarationSideElement.getDeclarationChildren().toMutableList()
        val declarationSideElements = declarationFieldElements.firstOrNull() ?: return null

        val parentForItem = getForItem(declarationSideElements)
        val index = searchIndex + 1
        if (parentForItem != null) {
            val parentDeclaration = getDeclarationTopItem(parentForItem, index)
            if (parentDeclaration is ForDeclarationDaoBaseItem) return parentDeclaration
        }

        val validDaoParam = daoMethod.findParameter(declarationSideElements.text)
        if (validDaoParam == null) return null

        return ForDeclarationDaoBaseItem(
            declarationFieldElements,
            validDaoParam,
            searchIndex,
        )
    }
}
