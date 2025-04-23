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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeafs
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationDaoBaseItem
import org.domaframework.doma.intellij.common.sql.foritem.ForDirectiveItemBase
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.sql.validator.SqlElForItemFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationDaoParamResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.getForItemDeclaration
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class ForDirectiveInspection(
    private val shorName: String,
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

    var declarationFieldElements = mutableListOf<SqlElIdExpr>()

    private val cachedForDirectiveBlocks: MutableMap<PsiElement, CachedValue<List<BlockToken>>> =
        mutableMapOf()

    fun getForItem(targetElement: PsiElement): ForItem? {
        val forBlocks = getForDirectiveBlock(targetElement)
        val targetName =
            targetElement.text
                .replace("_has_next", "")
                .replace("_index", "")
        val forItem = forBlocks.lastOrNull { it.item.text == targetName }
        forItem?.let { return ForItem(it.item) } ?: return null
    }

    fun checkForItem(blockElements: List<PsiElement>): ValidationResult? {
        val targetElement: PsiElement = blockElements.firstOrNull() ?: return null
        val file = targetElement.containingFile ?: return null

        val forItem = getForItem(targetElement)
        var errorElement: ValidationResult? = ValidationDaoParamResult(targetElement, "", shorName)
        if (forItem != null) {
            val declarationItem =
                getDeclarationItem(forItem, file)

            if (declarationItem != null && declarationItem is ForDeclarationDaoBaseItem) {
                val forItemElementsParentClass = declarationItem.getPsiParentClass()
                if (forItemElementsParentClass != null) {
                    val validator =
                        SqlElForItemFieldAccessorChildElementValidator(
                            blockElements,
                            forItemElementsParentClass,
                            shorName,
                        )
                    errorElement = validator.validateChildren()
                }
            }
        }
        return errorElement
    }

    /**
     * Count the `for`, `if`, and `end` elements from the beginning
     * to the target element (`targetElement`)
     * and obtain the `for` block information to which the `targetElement` belongs.
     */
    private fun getForDirectiveBlock(targetElement: PsiElement): List<BlockToken> {
        val cachedValue =
            cachedForDirectiveBlocks.getOrPut(targetElement) {
                CachedValuesManager.getManager(targetElement.project).createCachedValue {
                    val topElm =
                        targetElement.containingFile.firstChild
                            ?: return@createCachedValue CachedValueProvider.Result.create(
                                emptyList(),
                                targetElement.containingFile,
                            )
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

                    CachedValueProvider.Result.create(
                        stack.filter { it.type == BlockType.FOR },
                        targetElement.containingFile,
                    )
                }
            }
        return cachedValue.value
    }

    private fun getDeclarationItem(
        forItem: ForItem,
        file: PsiFile,
        searchIndex: Int = 0,
    ): ForDirectiveItemBase? {
        val forDirectiveParent = forItem.getParentForDirectiveExpr() ?: return null
        val declarationElement =
            forDirectiveParent.getForItemDeclaration() ?: return null
        declarationFieldElements = declarationElement.getDeclarationChildren().toMutableList()
        val topElm = declarationFieldElements.firstOrNull() ?: return null

        val parentForItem = getForItem(topElm)
        val index = searchIndex + 1
        if (parentForItem != null) {
            val parentDeclaration = getDeclarationItem(parentForItem, file, index)
            if (parentDeclaration is ForDeclarationDaoBaseItem) return parentDeclaration
        }

        return getForDeclarationDaoParamBase(topElm, searchIndex, file)
    }

    private fun getForDeclarationDaoParamBase(
        topElm: PsiElement,
        searchIndex: Int,
        file: PsiFile,
    ): ForDeclarationDaoBaseItem? {
        val daoMethod = findDaoMethod(file) ?: return null
        val validDaoParam = daoMethod.findParameter(topElm.text)
        if (validDaoParam == null) return null

        return ForDeclarationDaoBaseItem(
            declarationFieldElements,
            searchIndex,
            daoMethod.getDomaAnnotationType(),
            validDaoParam,
        )
    }
}
