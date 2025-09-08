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

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlKeywordGroupBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.TOP,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(node, context) {
    val topKeywordBlocks: MutableList<SqlBlock> = mutableListOf(this)
    var canAddTopKeyword = true
    private val topKeywordTypes =
        listOf(
            SqlKeywordBlock::class,
            SqlKeywordGroupBlock::class,
        )

    fun updateTopKeywordBlocks(block: SqlBlock) {
        val hasBlock = topKeywordBlocks.contains(block)
        val matchKeywordType =
            block.node.elementType == SqlTypes.KEYWORD &&
                TypeUtil.isExpectedClassType(
                    topKeywordTypes,
                    block,
                )

        if (matchKeywordType && canAddTopKeyword && !hasBlock) {
            topKeywordBlocks.add(block)
        }
        indent.groupIndentLen = createGroupIndentLen()
    }

    override val indent =
        ElementIndent(
            indentLevel,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = indentLevel
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun addChildBlock(childBlock: SqlBlock) {
        super.addChildBlock(childBlock)
        val lastChild = childBlocks.lastOrNull()
        val canAppendToLastChild =
            lastChild?.node?.elementType == SqlTypes.KEYWORD &&
                TypeUtil.isExpectedClassType(
                    topKeywordTypes,
                    lastChild,
                )
        if (canAddTopKeyword) {
            canAddTopKeyword = canAppendToLastChild || lastChild == null
        }
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlSelectQueryGroupBlock &&
            SqlKeywordUtil.isSelectSecondOptionKeyword(getNodeText())
        ) {
            lastGroup.secondGroupBlocks.add(this)
        }

        if (lastGroup is SqlWithCommonTableGroupBlock) {
            lastGroup.queryGroupBlock.add(this)
        }
    }

    open fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        lastGroup: SqlBlock?,
    ): Int {
        if (lastGroup == null) {
            return createBlockIndentLen(preChildBlock)
        }
        if (preChildBlock == null) return createBlockIndentLen(preChildBlock)

        if (preChildBlock.indent.indentLevel == this.indent.indentLevel &&
            !SqlKeywordUtil.isSetLineKeyword(getNodeText(), preChildBlock.getNodeText())
        ) {
            val diffPretextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
            return preChildBlock.indent.indentLen.minus(diffPretextLen)
        } else {
            return createBlockIndentLen(preChildBlock)
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    open fun createBlockIndentLen(preChildBlock: SqlBlock?): Int {
        parentBlock?.let { parent ->
            when (indentLevel) {
                IndentType.TOP -> {
                    if (SqlKeywordUtil.isSetLineKeyword(
                            getNodeText(),
                            preChildBlock?.getNodeText() ?: "",
                        )
                    ) {
                        val prevBlockIndent = preChildBlock?.indent?.indentLen ?: 0
                        val prevBlockLen = preChildBlock?.getNodeText()?.length ?: 0
                        return prevBlockIndent.plus(prevBlockLen).plus(1)
                    }
                    return if (parent.indent.indentLevel == IndentType.FILE) {
                        0
                    } else {
                        parent.indent.groupIndentLen
                    }
                }

                IndentType.INLINE_SECOND -> {
                    if (parent.indent.indentLevel == IndentType.FILE) 0
                    return parent.indent.groupIndentLen
                        .plus(1)
                }

                else -> return 1
            }
            return 1
        } ?: return 1
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(getTotalTopKeywordLength())

    fun getTotalTopKeywordLength(): Int = topKeywordBlocks.sumOf { it.getNodeText().length.plus(1) }.minus(1)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        val prevWord = prevBlocks.findLast { it is SqlKeywordBlock || it is SqlKeywordGroupBlock }
        return !SqlKeywordUtil.isSetLineKeyword(this.getNodeText(), prevWord?.getNodeText() ?: "") &&
            !SqlKeywordUtil.isSetLineKeyword(this.getNodeText(), lastGroup?.getNodeText() ?: "") &&
            lastGroup !is SqlFunctionParamBlock
    }
}
