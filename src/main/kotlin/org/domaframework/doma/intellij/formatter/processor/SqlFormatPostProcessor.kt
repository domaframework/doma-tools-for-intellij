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
import org.domaframework.doma.intellij.setting.SqlLanguage

class SqlFormatPostProcessor : SqlPostProcessor() {
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

        val document = getDocument(source) ?: return rangeToReformat
        val processedText = processDocumentText(document.text, true)

        if (document.text == processedText) {
            return rangeToReformat
        }

        updateDocument(source.project, document, processedText)
        return TextRange(0, processedText.length)
    }

    private fun isSqlFile(source: PsiFile): Boolean = source.language == SqlLanguage.INSTANCE

    private fun isInjectedSqlFile(source: PsiFile): Boolean {
        val injectedLanguageManager = InjectedLanguageManager.getInstance(source.project)
        return injectedLanguageManager.isInjectedFragment(source)
    }

    private fun getDocument(source: PsiFile) = PsiDocumentManager.getInstance(source.project).getDocument(source)

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
