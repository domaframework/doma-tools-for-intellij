package org.domaframework.doma.intellij.formatter

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.tree.IElementType

object PseudoASTBuilder {
    fun createLeafNode(
        type: IElementType,
        text: String,
    ): ASTNode = LeafPsiElement(type, text)

    fun createCompositeNode(
        type: IElementType,
        vararg children: TreeElement,
    ): ASTNode {
        val parent = CompositeElement(type)
        for (child in children) {
            parent.rawAddChildren(child)
        }
        return parent
    }
}
