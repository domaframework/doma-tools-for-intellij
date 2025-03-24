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
package org.domaframework.doma.intellij.formatter.block.group

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.lang.tree.util.children
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.impl.source.tree.CompositeElement
import org.domaframework.doma.intellij.formatter.PseudoASTBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlGroupTopKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock
import org.domaframework.doma.intellij.psi.SqlTypes

abstract class SqlGroupBlock(
    node: ASTNode,
    internal val groupTopNode: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val parentGroupNode: SqlBlock?,
    spacingBuilder: SpacingBuilder,
) : SqlBlock(
        node,
        wrap,
        alignment,
        null,
        spacingBuilder,
    ) {
    val topLevelKeywords = listOf("select", "insert", "update", "delete", "with")
    val someLevelKeywords = listOf("from", "where", "group", "having", "order", "limit", "offset")
    val subQueryKeywords = listOf("inner", "outer", "left", "right", "cross", "join")

    override val searchKeywordLevelHistory = mutableListOf<Int>()

    open fun isLoopContinuation(child: ASTNode): Boolean = true

    override fun buildChildren(): MutableList<AbstractBlock> {
        val parentNode = CompositeElement(groupTopNode.elementType)
        blocks.add(SqlGroupTopKeywordBlock(groupTopNode, groupTopNode, wrap, alignment, spacingBuilder))
        searchKeywordLevelHistory.add(indentLevel)

        var child = groupTopNode.treeNext
        var nonWhiteSpaceChild: SqlBlock? = null

        println("----------------- Group: ${groupTopNode.text}-----------------")
        if (parentGroupNode != null && parentGroupNode.indentLevel > 0) {
            println("Parent: ${parentGroupNode.node.text}")
        }
        while (child != null && !endBlock && isLoopContinuation(child)) {
            if (child is PsiWhiteSpace && !blockSkip) {
                blocks.add(
                    SqlWhitespaceBlock(
                        child,
                        wrap,
                        alignment,
                        nonWhiteSpaceChild,
                        spacingBuilder,
                    ),
                )
            } else {
                val childBlock = getBlock(child)
                if (blocks.isNotEmpty()) {
                    (blocks.last() as? SqlWhitespaceBlock)?.nextNode = childBlock
                }
                updateSearchKeywordLevelHistory(childBlock, child)
                nonWhiteSpaceChild = childBlock
            }
            child = child.treeNext
        }
        println("Build Block: ${this.groupTopNode.text}=========")
        println("Blocks Size: ${blocks.size}")
        println("Blocks: ${blocks.map { " ${it.node.textRange}" }}")
        println("=========Build Block: END")

        val project = node.psi.project
        WriteCommandAction.runWriteCommandAction(project) {
            parentNode.addChildren(
                groupTopNode,
                null,
                null,
            )
            this.myNode =
                blocks.map {
                    parentNode.addChildren(
                        PseudoASTBuilder.createLeafNode(it.node.elementType, it.node.text),
                        null,
                        null,
                    )
                } as ASTNode
        }
        println("New Block: ${this.myNode.text}")
        println("Children: ${this.myNode.children().map { it.text }}")
        return blocks.map { it }.toMutableList()
    }

    override fun updateSearchKeywordLevelHistory(
        childBlock: SqlBlock,
        child: ASTNode,
    ) {
        if (child.elementType == SqlTypes.RIGHT_PAREN) {
            blockSkip = false
            val leftIndex = searchKeywordLevelHistory.indexOfLast { it == 3 }
            if (leftIndex >= 0) {
                searchKeywordLevelHistory
                    .subList(
                        leftIndex,
                        searchKeywordLevelHistory.size,
                    ).clear()
                println("hit RIGHT_PAREN: $searchKeywordLevelHistory")
                pendingCommentBlocks.clear()
            } else {
                if (!searchKeywordLevelHistory.contains(3)) {
                    blockSkip = true
                    endBlock = true
                }
            }
            return
        }
        val lastLevel =
            if (searchKeywordLevelHistory.isNotEmpty()) {
                searchKeywordLevelHistory.last()
            } else {
                indentLevel
            }
        if (childBlock is SqlGroupBlock) {
            val childIndentLevel = childBlock.indentLevel
            println("Hit Group Block: Lv:  $childIndentLevel ->  ${child.text}")
            if (lastLevel == 3) {
                blockSkip = true
                println("${node.text} skip in subGroup $searchKeywordLevelHistory")
                return
            }
            if (childIndentLevel <= lastLevel) {
                blocks.add(childBlock)
                println("Add Node: ${this.node.text} ->  ${child.text}")
                blockSkip = true
                if (childIndentLevel != lastLevel) {
                    searchKeywordLevelHistory.add(childIndentLevel)
                }
            } else {
                if (lastLevel == indentLevel) {
                    blocks.add(childBlock)
                }
                searchKeywordLevelHistory.add(childIndentLevel)
                blockSkip = true
            }
            if (childIndentLevel <= indentLevel) {
                blockSkip = true
                endBlock = true
                println("End Group: ${this.node.text} ->  ${child.text}")
            }
            println("${node.text} updateSearchKeywordLevelHistory: $searchKeywordLevelHistory")
        } else if (lastLevel == indentLevel && !blockSkip) {
            blocks.add(childBlock)
            println("Add Node: ${this.node.text} ->  ${child.text}")
        }
    }

    override fun getIndent(): Indent? {
        println("Node getIndent : ${groupTopNode.text}")
        if (parentGroupNode == null || parentGroupNode.indentLevel == 0) return Indent.getNoneIndent()
        val parentIndent = parentGroupNode.indentCount
        val parentTextLen = parentGroupNode.node.text.length

        println("ParentGroupNode: ${parentGroupNode.node.text}")
        println("ParentGroupNode Indent: ${parentGroupNode.indentCount}")

        indentCount = getIndentCount(parentIndent, parentTextLen)

        println("Node Indent: ${groupTopNode.text} $indentCount")
        return Indent.getSpaceIndent(indentCount)
    }

    protected open fun getIndentCount(
        parentIndent: Int,
        parentTextLen: Int,
    ): Int = 0

    override fun isLeaf(): Boolean = false
}
