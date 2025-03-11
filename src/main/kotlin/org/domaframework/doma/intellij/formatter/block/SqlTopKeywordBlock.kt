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
package org.domaframework.doma.intellij.formatter.block

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode

/**
 * 改行、インデント付ける
 */
class SqlTopKeywordBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val indentLevel: Int,
    spacingBuilder: SpacingBuilder,
) : SqlKeywordBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
    ) {
    override fun getIndent(): Indent? {
        if (indentLevel <= 0) {
            return Indent.getNoneIndent()
        }

        val spaces = "  "
        val indentBuilder = StringBuilder()
        for (level in 0..<indentLevel) {
            indentBuilder.append(spaces)
        }
        return Indent.getSpaceIndent(indentBuilder.length)
    }
}
