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
package org.domaframework.doma.intellij.formatter.builder

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.psi.tree.IElementType
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock.SqlElCommentDirectiveType
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock

class SqlCustomSpacingBuilder {
    companion object {
        val normalSpacing: Spacing = Spacing.createSpacing(1, 1, 0, false, 0, 0)
        val nonSpacing: Spacing = Spacing.createSpacing(0, 0, 0, false, 0, 0)
    }

    private val spacingRules: MutableMap<Pair<IElementType?, IElementType?>?, Spacing?> = HashMap()

    fun withSpacing(
        left: IElementType?,
        right: IElementType?,
        spacing: Spacing?,
    ): SqlCustomSpacingBuilder {
        spacingRules.put(Pair(left, right), spacing)
        return this
    }

    fun getCustomSpacing(
        child1: Block?,
        child2: Block?,
    ): Spacing? {
        if (child1 is ASTBlock && child2 is ASTBlock) {
            val type1: IElementType? = child1.node?.elementType
            val type2: IElementType? = child2.node?.elementType
            val spacing: Spacing? = spacingRules[Pair(type1, type2)]
            if (spacing != null) {
                return spacing
            }
        }
        if (child1 == null && child2 is ASTBlock) {
            val type2: IElementType? = child2.node?.elementType
            val spacing: Spacing? = spacingRules[Pair(null, type2)]
            if (spacing != null) {
                return spacing
            }
        }
        return null
    }

    fun getSpacingWithIndentComma(
        child1: SqlBlock?,
        child2: SqlBlock,
    ): Spacing? {
        if (child2.parentBlock is SqlParallelListBlock) {
            return nonSpacing
        }

        when (child1) {
            null -> return nonSpacing
            is SqlWhitespaceBlock -> {
                val indentLen: Int = child2.indent.indentLen
                val afterNewLine = child1.getNodeText().substringAfterLast("\n", "")
                if (child1.getNodeText().contains("\n")) {
                    val currentIndent = afterNewLine.length
                    val newIndent =
                        if (currentIndent != indentLen) {
                            indentLen
                        } else {
                            0
                        }
                    return Spacing.createSpacing(newIndent, newIndent, 0, false, 0, 0)
                }
            }

            else -> {
                return getSpacing(child2)
            }
        }
        return null
    }

    fun getSpacing(child2: SqlBlock): Spacing =
        Spacing.createSpacing(
            child2.indent.indentLen,
            child2.indent.indentLen,
            0,
            false,
            0,
            0,
        )

    fun getSpacingColumnDefinition(child: SqlColumnBlock): Spacing? {
        val indentLen = child.createBlockIndentLen()
        return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
    }

    fun getSpacingColumnDefinitionRaw(child: SqlColumnDefinitionRawGroupBlock): Spacing? {
        val indentLen = child.createBlockIndentLen()
        return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
    }

    fun getSpacingElDirectiveComment(
        child: SqlElBlockCommentBlock,
        child2: SqlBlock,
    ): Spacing? {
        if (child2 is SqlRightPatternBlock) return null
        if (child.directiveType == SqlElCommentDirectiveType.CONDITION_LOOP) {
            val indent = child2.indent.indentLen
            return Spacing.createSpacing(indent, indent, 0, false, 0, 0)
        }

        if (child.directiveType == SqlElCommentDirectiveType.NORMAL ||
            child.directiveType == SqlElCommentDirectiveType.LITERAL
        ) {
            return nonSpacing
        }

        return normalSpacing
    }

    fun getSpacingRightPattern(block: SqlRightPatternBlock): Spacing? {
        val parentBlock = block.parentBlock

        if (block.isSaveSpace(null)) {
            return getSpacing(block)
        }

        if (TypeUtil.isExpectedClassType(SqlRightPatternBlock.NEW_LINE_EXPECTED_TYPES, parentBlock)) {
            return getSpacing(block)
        }

        if (parentBlock is SqlParallelListBlock) {
            val lastKeywordGroup = parentBlock.getChildBlocksDropLast().lastOrNull()
            return if (lastKeywordGroup is SqlKeywordGroupBlock) {
                normalSpacing
            } else {
                nonSpacing
            }
        }

        if (parentBlock is SqlDataTypeParamBlock || !block.preSpaceRight) return nonSpacing
        return normalSpacing
    }
}
