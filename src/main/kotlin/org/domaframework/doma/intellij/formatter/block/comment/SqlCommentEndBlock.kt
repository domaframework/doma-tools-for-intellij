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
package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlCommentEndBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlCommentSeparateBlock(node, context) {
    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        parentBlock?.let { parent ->
            val contents =
                PsiTreeUtil.getChildOfType<PsiComment>(parent.node.psi, PsiComment::class.java)
            return contents != null
        }
        return false
    }
}
