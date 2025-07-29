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
