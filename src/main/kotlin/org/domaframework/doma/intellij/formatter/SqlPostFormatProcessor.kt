// Kotlin - file: src/main/kotlin/org/domaframework/doma/intellij/formatter/post/SqlPostFormatProcessor.kt
package org.domaframework.doma.intellij.formatter

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.end
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.start
import org.toml.lang.psi.ext.elementType

class SqlPostFormatProcessor : PostFormatProcessor {
    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings,
    ): TextRange {
        if (source.language != SqlLanguage.INSTANCE) return rangeToReformat

        val visitor = SqlPostFormatVisitor(settings)
        source.accept(visitor)

        val docManager = PsiDocumentManager.getInstance(source.project)
        val document = docManager.getDocument(source) ?: return rangeToReformat

        visitor.keywords.asReversed().forEach {
            document.deleteString(it.textRange.start, it.textRange.end)
            document.insertString(it.textRange.start, it.text.uppercase())
        }
        docManager.commitDocument(document)

        return rangeToReformat.grown(visitor.keywords.size)
    }

    override fun processElement(
        element: PsiElement,
        settings: CodeStyleSettings,
    ): PsiElement = processText(element.containingFile, element.textRange, settings) as PsiElement
}

private class SqlPostFormatVisitor(
    settings: CodeStyleSettings,
) : PsiRecursiveElementVisitor() {
    val keywords = mutableListOf<PsiElement>()

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element.elementType != SqlTypes.KEYWORD) return

        keywords.add(element)
    }
}
