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
import org.domaframework.doma.intellij.formatter.block.IndentType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock

class SqlCustomSpacingBuilder {
    companion object;

    private val spacingRules: MutableMap<Pair<IElementType?, IElementType?>?, Spacing?> = HashMap()

    fun withSpacing(
        left: IElementType?,
        right: IElementType?,
        spacing: Spacing?,
    ): SqlCustomSpacingBuilder {
        spacingRules.put(Pair(left, right), spacing)
        return this
    }

    fun getSpacing(
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
        child2: SqlCommaBlock,
    ): Spacing? {
        val indentLen: Int = child2.indentLen
        when (child1) {
            null -> return Spacing.createSpacing(0, 0, 0, false, 0, 0)
            is SqlWhitespaceBlock -> {
                val afterNewLine = child1.node.text.substringAfterLast("\n", "")
                if (child1.node.text.contains("\n")) {
                    val currentIndent = afterNewLine.length
                    val newIndent =
                        if (currentIndent != indentLen) {
                            indentLen.minus(currentIndent)
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
        return Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
    }

    fun getSpacingWithWhiteSpace(
        child1: SqlWhitespaceBlock,
        child2: SqlKeywordBlock,
    ): Spacing? {
        if (child1.node.text.contains("\n")) {
            val postNewLine = child1.node.text.substringAfterLast("\n", "")
            val newIndent =
                if (child1.node.text.endsWith("\n") || postNewLine.isEmpty()) {
                    child2.indentLen
                } else {
                    child2.indentLen - postNewLine.length
                }
            println("Spacing: ${child2.node.text} , $newIndent")
            return Spacing.createSpacing(
                newIndent,
                newIndent,
                0,
                false,
                0,
                0,
            )
        } else {
            val newIndent =
                if (child2.indentLevel < IndentType.SUB) {
                    child2.indentLen
                } else {
                    1 - child1.node.text.length
                }

            println("Spacing: ${child2.node.text} , $newIndent")
            return Spacing.createSpacing(
                newIndent,
                newIndent,
                1,
                false,
                0,
                1,
            )
        }
        return null
    }

    fun getSpacingWithIndentLevel(child: SqlKeywordBlock): Spacing? {
        val parentBlock = child.parentBlock
        val indentLen: Int = child.indentLen
        return when (child.indentLevel) {
            IndentType.TOP -> {
                return if (parentBlock?.parentBlock == null) {
                    Spacing.createSpacing(0, 0, 0, false, 0, 0)
                } else if (parentBlock.indentLevel == IndentType.SUB) {
                    Spacing.createSpacing(0, 0, 0, false, 0, 0)
                } else {
                    Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
                }
            }

            IndentType.SECOND -> {
                return if (parentBlock?.indentLevel == IndentType.SUB) {
                    Spacing.createSpacing(1, 1, 0, false, 0, 0)
                } else {
                    Spacing.createSpacing(indentLen, indentLen, 1, false, 0, 1)
                }
            }

            IndentType.SUB -> {
                return Spacing.createSpacing(1, 1, 0, false, 0, 0)
            }

            else -> {
                return Spacing.createSpacing(1, 1, 0, false, 0, 0)
            }
        }
        return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
    }
}
