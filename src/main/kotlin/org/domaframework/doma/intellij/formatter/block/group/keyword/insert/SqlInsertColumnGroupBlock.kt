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
package org.domaframework.doma.intellij.formatter.block.group.keyword.insert

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * Block of columns to insert
 * For Example:
 *  INSERT INTO users
 *             (username -- [SqlInsertColumnGroupBlock]
 *              , email)
 *      VALUES ('user'
 *              , 'user@example.com')
 */
class SqlInsertColumnGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    // TODO Customize indentation
    override val offset = 2

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(1)
        updateParentGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlInsertQueryGroupBlock)?.columnDefinitionGroupBlock = this
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    /**
     * Set the column line indentation to match the length of the `INSERT` query keyword (including `INSERT INTO`).
     */
    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            val parentBaseLen = getParentInsertKeywordsIndentLength(parent)
            return parentBaseLen.plus(1)
        } ?: return offset
    }

    /**
     * Retrieve the length of the INSERT query keyword (including INSERT INTO)
     * and adjust the indentation baseline for the INSERT group accordingly.
     */
    private fun updateParentGroupIndentLen() {
        parentBlock?.let { parent ->
            // TODO Indentation is adjusted on the parent class side
            val parentBaseLen = getParentInsertKeywordsIndentLength(parent)
            parent.indent.groupIndentLen = parentBaseLen
        }
    }

    private fun getParentInsertKeywordsIndentLength(parent: SqlBlock): Int {
        if (parent is SqlInsertQueryGroupBlock) {
            var parentLen = getKeywordNameLength(parent.childBlocks, 1)
            return parentLen
                .plus(parent.indent.indentLen)
                .plus(parent.getNodeText().length)
        }
        return parent.indent.groupIndentLen
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
