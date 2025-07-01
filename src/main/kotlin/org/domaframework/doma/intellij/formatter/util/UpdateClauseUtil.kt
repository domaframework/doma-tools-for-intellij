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
package org.domaframework.doma.intellij.formatter.util

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateValueGroupBlock

object UpdateClauseUtil {
    fun getUpdateClauseSubGroup(
        lastGroup: SqlBlock,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? =
        if (lastGroup is SqlUpdateSetGroupBlock) {
            if (lastGroup.childBlocks.firstOrNull { it is SqlUpdateColumnGroupBlock } == null) {
                SqlUpdateColumnGroupBlock(child, sqlBlockFormattingCtx)
            } else if (lastGroup.childBlocks.lastOrNull { it is SqlUpdateColumnGroupBlock } != null) {
                SqlUpdateValueGroupBlock(child, sqlBlockFormattingCtx)
            } else {
                SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
            }
        } else {
            null
        }
}
