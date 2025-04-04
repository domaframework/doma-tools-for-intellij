package org.domaframework.doma.intellij.formatter

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor

class SqlPostProcessor : PostFormatProcessor {
    override fun processElement(
        source: PsiElement,
        settings: CodeStyleSettings,
    ): PsiElement = source

    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings,
    ): TextRange {
        val project: Project = source.project
        val document = PsiDocumentManager.getInstance(project).getDocument(source) ?: return rangeToReformat

        val originalText = document.text
        val withoutTrailingSpaces = originalText.replace(Regex(" +(\r?\n)"), "$1")
        val finalText = withoutTrailingSpaces.trimEnd() + " \n"

        if (originalText == finalText) {
            return rangeToReformat
        }

        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                document.setText(finalText)
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }
        return TextRange(0, finalText.length)
    }
}
