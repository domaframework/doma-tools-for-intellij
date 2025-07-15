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
package org.domaframework.doma.intellij.formatter.block.other

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlEscapeBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlOtherBlock(node, context) {
    var isEndEscape = false

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        super.setParentPropertyBlock(lastGroup)
        // If the number of escape characters, including itself, is even
        isEndEscape = lastGroup?.childBlocks?.count { it is SqlEscapeBlock }?.let { it % 2 == 0 } == true
    }

    override fun createBlockIndentLen(): Int =
        if (isEndEscape) {
            0
        } else {
            1
        }
}
