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
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock

object InsertClauseUtil {
    fun getInsertClauseSubGroup(
        lastGroup: SqlBlock,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlSubGroupBlock? {
        if (lastGroup is SqlInsertQueryGroupBlock) {
            return SqlInsertColumnGroupBlock(child, sqlBlockFormattingCtx)
        }
        if (lastGroup is SqlValuesGroupBlock) {
            return SqlInsertValueGroupBlock(child, sqlBlockFormattingCtx)
        }
        return null
    }
}
