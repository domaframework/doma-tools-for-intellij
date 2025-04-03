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
package org.domaframework.doma.intellij.formatter

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.psi.tree.IElementType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlDataTypeBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSubQueryGroupBlock

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
        val indentLen: Int = child2.indent.indentLen
        when (child1) {
            null -> return Spacing.createSpacing(0, 0, 0, false, 0, 0)
            is SqlWhitespaceBlock -> {
                val afterNewLine = child1.node.text.substringAfterLast("\n", "")
                if (child1.node.text.contains("\n")) {
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
                return Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
            }
        }
        return null
    }

    fun getSpacing(child2: SqlNewGroupBlock): Spacing? =
        Spacing.createSpacing(
            child2.indent.indentLen,
            child2.indent.indentLen,
            0,
            false,
            0,
            0,
        )

    fun getSpacingDataType(child: SqlDataTypeBlock): Spacing? {
        val indentLen = child.createIndentLen()
        return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
    }

    fun getSpacingColumnDefinitionRaw(child: SqlColumnDefinitionRawGroupBlock): Spacing? {
        val indentLen = child.indent.indentLen
        return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
    }

    fun getSpacingColumnDefinitionRawEndRight(child: SqlRightPatternBlock): Spacing? {
        val indentLen = child.indent.indentLen
        return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
    }

    /**
     * Adjust line breaks and indentation depending on the block indent type
     */
    fun getSpacingWithIndentLevel(child: SqlNewGroupBlock): Spacing? {
        val parentBlock = child.parentBlock
        val indentLen: Int = child.indent.indentLen

        return when (child.indent.indentLevel) {
            IndentType.TOP -> {
                return if (parentBlock?.parentBlock == null) {
                    nonSpacing
                } else if (parentBlock is SqlSubGroupBlock) {
                    nonSpacing
                } else {
                    Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
                }
            }

            IndentType.SECOND -> {
                return if (parentBlock is SqlSubQueryGroupBlock) {
                    normalSpacing
                } else if (SqlKeywordUtil.isSetLineKeyword(child.node.text, parentBlock?.node?.text ?: "")) {
                    null
                } else {
                    Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
                }
            }

            IndentType.SECOND_OPTION -> {
                return Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
            }

            IndentType.SUB -> {
                if (parentBlock is SqlCreateKeywordGroupBlock) {
                    return Spacing.createSpacing(0, 0, 1, false, 0, 1)
                }
                return null
            }

            IndentType.INLINE -> {
                return normalSpacing
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let {
                    val parentIndentLen = it.indent.groupIndentLen
                    val parentTextLen = it.node.text.length
                    val newIndentLen = parentIndentLen.plus(parentTextLen).plus(1)
                    return Spacing.createSpacing(newIndentLen, newIndentLen, 1, false, 0, 1)
                }
                return Spacing.createSpacing(0, 0, 1, false, 0, 1)
            }

            else -> {
                return null
            }
        }
        return null
    }
}
