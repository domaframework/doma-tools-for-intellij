package org.domaframework.doma.intellij.formatter

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.end
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.start
import org.toml.lang.psi.ext.elementType

class SqlFormatPreProcessor : PreFormatProcessor {
    override fun process(
        node: ASTNode,
        rangeToReformat: TextRange,
    ): TextRange = processText(node.psi.containingFile, rangeToReformat)

    private fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
    ): TextRange {
        if (source.language != SqlLanguage.INSTANCE) return rangeToReformat

        val visitor = SqlFormatVisitor()
        source.accept(visitor)

        val docManager = PsiDocumentManager.getInstance(source.project)
        val document = docManager.getDocument(source) ?: return rangeToReformat

        var index = visitor.replaces.count { it.elementType != TokenType.WHITE_SPACE }
        visitor.replaces.asReversed().forEach {
            if (it.elementType != TokenType.WHITE_SPACE) {
                val newKeyword =
                    if (checkKeywordPrevElement(index, it)) {
                        "\n${it.text.uppercase()}"
                    } else {
                        it.text.uppercase()
                    }
                document.deleteString(it.textRange.start, it.textRange.end)
                document.insertString(it.textRange.start, newKeyword)
                index--
            } else {
                val currentIndent = it.text.substringAfter("\n", "").length
                val start = it.textRange.end - currentIndent
                document.deleteString(
                    start,
                    it.textRange.end,
                )
                document.insertString(it.textRange.end - currentIndent, "")
            }
        }
        docManager.commitDocument(document)

        return rangeToReformat.grown(visitor.replaces.size)
    }

    private fun checkKeywordPrevElement(
        index: Int,
        element: PsiElement,
    ): Boolean =
        index > 1 &&
            element.prevSibling != null &&
            element.prevSibling.elementType != SqlTypes.LEFT_PAREN &&
            !element.prevSibling.text.contains("\n")
}

private class SqlFormatVisitor : PsiRecursiveElementVisitor() {
    val replaces = mutableListOf<PsiElement>()

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)

        val newLineKeywords =
            listOf(
                "select",
                "insert",
                "update",
                "delete",
                "set",
                "from",
                "where",
                "group",
                "and",
                "or",
            )

        if (PsiTreeUtil.getParentOfType(element, SqlBlockComment::class.java) == null) {
            when (element.elementType) {
                SqlTypes.KEYWORD -> {
                    if (newLineKeywords.contains(element.text.lowercase())
                    ) {
                        replaces.add(element)
                    }
                }

                SqlTypes.LEFT_PAREN, SqlTypes.COMMA -> {
                    replaces.add(element)
                }
                TokenType.WHITE_SPACE -> {
                    if (element.nextSibling?.elementType == SqlTypes.KEYWORD &&
                        newLineKeywords.contains(element.nextSibling.text.lowercase())
                    ) {
                        replaces.add(element)
                    }
                }
            }
        }
    }
}
