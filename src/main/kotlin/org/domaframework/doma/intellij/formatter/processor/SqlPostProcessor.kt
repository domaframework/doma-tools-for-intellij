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

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil.isInjectedSqlFile
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.setting.SqlLanguage

class SqlPostProcessor : PostFormatProcessor {
    companion object {
        private const val FILE_END_PADDING = " ${StringUtil.LINE_SEPARATE}"
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
        if (!isSqlFile(source) || isInjectedSqlFile(source)) {
            return rangeToReformat
        }

        val originalDocument = getDocument(source)
        val document = originalDocument ?: source.fileDocument
        val processedText = processDocumentText(document.text, originalDocument != null)

        if (document.text == processedText) {
            return rangeToReformat
        }

        updateDocument(source.project, document, processedText)
        return TextRange(0, processedText.length)
    }

    private fun isSqlFile(source: PsiFile): Boolean = source.language == SqlLanguage.INSTANCE

    private fun getDocument(source: PsiFile) = PsiDocumentManager.getInstance(source.project).getDocument(source)

    private fun processDocumentText(
        originalText: String,
        existsOriginalDocument: Boolean,
    ): String {
        val withoutTrailingSpaces = removeTrailingSpaces(originalText)
        return ensureProperFileEnding(withoutTrailingSpaces, existsOriginalDocument)
    }

    private fun removeTrailingSpaces(text: String): String = text.replace(trailingSpacesRegex, "$1")

    private fun ensureProperFileEnding(
        text: String,
        isEndSpace: Boolean,
    ): String =
        text.trimEnd() +
            if (isEndSpace) FILE_END_PADDING else ""

    private fun updateDocument(
        project: Project,
        document: Document,
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
