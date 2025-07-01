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
package org.domaframework.doma.intellij.formatter.block.group.keyword

import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

open class SqlKeywordGroupBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.TOP,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(node, context) {
    override val indent =
        ElementIndent(
            indentLevel,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        val preChildBlock = lastGroup?.childBlocks?.dropLast(1)?.lastOrNull()
        indent.indentLevel = indentLevel

        val baseIndentLen = getBaseIndentLen(preChildBlock, lastGroup)
        indent.groupIndentLen = baseIndentLen.plus(getNodeText().length)
        indent.indentLen = adjustIndentIfFirstChildIsLineComment(baseIndentLen)
        createGroupIndentLen()
    }

//    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
//        if (lastGroup is SqlSelectKeywordGroupBlock &&
//            SqlKeywordUtil.isSelectSecondOptionKeyword(getNodeText())
//        ) {
//            lastGroup.secondGroupBlocks.add(this)
//        }
//
//        if (getNodeText() == "values" && lastGroup is SqlInsertQueryGroupBlock) {
//            lastGroup.valueKeywordBlock = this
//        }
//    }

    open fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        block: SqlBlock?,
    ): Int {
        if (block == null) {
            return createBlockIndentLen(preChildBlock)
        }
        if (preChildBlock == null) return createBlockIndentLen(preChildBlock)

        if (preChildBlock.indent.indentLevel == this.indent.indentLevel &&
            !SqlKeywordUtil.isSetLineKeyword(getNodeText(), preChildBlock.getNodeText())
        ) {
            if (indent.indentLevel == IndentType.SECOND) {
                val diffPreBlockTextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
                return preChildBlock.indent.indentLen.minus(diffPreBlockTextLen)
            } else {
                val diffPretextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
                return preChildBlock.indent.indentLen.minus(diffPretextLen)
            }
        } else {
            return createBlockIndentLen(preChildBlock)
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? {
        if (!isAdjustIndentOnEnter() && parentBlock?.indent?.indentLevel == IndentType.SUB) {
            return Indent.getSpaceIndent(0)
        }
        return Indent.getNoneIndent()
    }

    /**
     * Adjust the indent position of the subgroup block element itself if it has a comment
     */
    open fun adjustIndentIfFirstChildIsLineComment(baseIndent: Int): Int {
        parentBlock?.let { parent ->
            if (indent.indentLevel == IndentType.TOP) {
                return if (parent is SqlSubGroupBlock) {
                    return if (parent.isFirstLineComment) {
                        parent.indent.groupIndentLen.minus(parent.getNodeText().length)
                    } else {
                        val newIndentLen = baseIndent.minus(1)
                        return if (newIndentLen >= 0) newIndentLen else 0
                    }
                } else {
                    return baseIndent
                }
            }
        }
        return baseIndent
    }

    open fun createBlockIndentLen(preChildBlock: SqlBlock?): Int {
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let {
                    val groupLen = it.indent.groupIndentLen
                    if (SqlKeywordUtil.isSetLineKeyword(getNodeText(), preChildBlock?.getNodeText() ?: "")) {
                        val prevBlockIndent = preChildBlock?.indent?.indentLen ?: 0
                        val prevBlockLen = preChildBlock?.getNodeText()?.length ?: 0
                        return prevBlockIndent.plus(prevBlockLen).plus(1)
                    }
                    return if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else {
                        groupLen
                    }
                } ?: return 0
            }

            IndentType.SECOND -> {
                parentBlock?.let { parent ->
                    val groupLen = parent.indent.groupIndentLen

                    if (parent.indent.indentLevel == IndentType.FILE) {
                        return 0
                    } else {
                        parent.parentBlock?.let { grand ->
                            return if (grand is SqlCreateViewGroupBlock) {
                                groupLen.minus(this.getNodeText().length)
                            } else if (grand is SqlSubGroupBlock) {
                                groupLen.minus(getNodeText().length).plus(1)
                            } else {
                                groupLen.minus(this.getNodeText().length)
                            }
                        } ?: return groupLen.minus(this.getNodeText().length)
                    }
                } ?: return 1
            }

            IndentType.SECOND_OPTION -> {
                parentBlock?.let { parent ->
                    val groupLen = parent.indent.groupIndentLen
                    if (parent.indent.indentLevel == IndentType.FILE) {
                        return 0
                    }
                    val subGroupBlock = parent.parentBlock as? SqlSubGroupBlock
                    val newIndent =
                        if (parent is SqlSubQueryGroupBlock) {
                            return if (getNodeText() == "and") {
                                groupLen
                            } else {
                                groupLen.plus(1)
                            }
                        } else if (getNodeText() == "and" && parent.getNodeText() == "or") {
                            return groupLen.plus(1)
                        } else if (parent is SqlKeywordGroupBlock && subGroupBlock != null && subGroupBlock.isFirstLineComment) {
                            groupLen
                        } else {
                            val removeStartOffsetLess =
                                parent.childBlocks.dropLast(1).filter {
                                    it.node.startOffset >
                                        parent.node.startOffset
                                }
                            val parentLen = getKeywordNameLength(removeStartOffsetLess, 0)
                            val parentTextLen = parent.indent.groupIndentLen.plus(parentLen)
                            return parentTextLen.minus(getNodeText().length)
                        }
                    return newIndent
                } ?: 1
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) 0
                    return it.indent.groupIndentLen
                        .plus(1)
                } ?: return 1
            }

            else -> return 1
        }
        return 1
    }

    protected open fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (indent.indentLevel == IndentType.SECOND_OPTION) {
                val parentLen = getKeywordNameLength(parent.childBlocks, 1)
                parent.indent.groupIndentLen
                    .plus(parentLen)
                    .minus(getNodeText().length)
            }
        } ?: 1
        return 1
    }
}
