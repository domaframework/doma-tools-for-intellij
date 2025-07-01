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
package org.domaframework.doma.intellij.formatter.block.group.function

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * Function call group
 *
 * For Example:
 * -- FUNC is [SqlFunctionGroupBlock]
 * SELECT FUNC(index -- "(" that after FUNC is [SqlParamGroupBlock]
 *             , (SELECT number -- "index" and "," is [SqlParamBlock]
 *                  FROM demo)
 *             , (num1 + num2))
 */
class SqlFunctionGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(node, context) {
    val parameterGroupBlock: SqlParamGroupBlock? = null
}
