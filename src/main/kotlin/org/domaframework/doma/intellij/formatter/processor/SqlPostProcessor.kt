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
package org.domaframework.doma.intellij.formatter.processor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import org.domaframework.doma.intellij.setting.SqlLanguage

class SqlPostProcessor : PostFormatProcessor {
    companion object {
        private const val FILE_END_PADDING = " \n"
    }

    private val trailingSpacesRegex = Regex(" +(\r?\n)")

    override fun processElement(
        source: PsiElement,
        settings: CodeStyleSettings,
    ): PsiElement = source

    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings,
    ): TextRange {
        if (!isSqlFile(source)) {
            return rangeToReformat
        }

        val document = getDocument(source) ?: return rangeToReformat
        val processedText = processDocumentText(document.text)

        if (document.text == processedText) {
            return rangeToReformat
        }

        updateDocument(source.project, document, processedText)
        return TextRange(0, processedText.length)
    }

    private fun isSqlFile(source: PsiFile): Boolean = source.language == SqlLanguage.INSTANCE

    private fun getDocument(source: PsiFile) = PsiDocumentManager.getInstance(source.project).getDocument(source)

    private fun processDocumentText(originalText: String): String {
        val withoutTrailingSpaces = removeTrailingSpaces(originalText)
        return ensureProperFileEnding(withoutTrailingSpaces)
    }

    private fun removeTrailingSpaces(text: String): String = text.replace(trailingSpacesRegex, "$1")

    private fun ensureProperFileEnding(text: String): String = text.trimEnd() + FILE_END_PADDING

    private fun updateDocument(
        project: Project,
        document: com.intellij.openapi.editor.Document,
        newText: String,
    ) {
        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                document.setText(newText)
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }
    }
}
