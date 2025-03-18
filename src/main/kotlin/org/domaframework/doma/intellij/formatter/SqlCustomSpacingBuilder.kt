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

class SqlCustomSpacingBuilder {
    companion object {
        fun defaultSpacing(): Spacing? = Spacing.createSpacing(1, 1, 0, false, 0)
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

    fun getSpacing(
        parent: Block?,
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
        return defaultSpacing()
    }
}
