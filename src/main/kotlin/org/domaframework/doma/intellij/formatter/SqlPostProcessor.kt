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
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.domaframework.doma.intellij.state.DomaToolsFunctionEnableSettings

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
        if (!isEnableFormat()) return rangeToReformat
        if (source.language != SqlLanguage.INSTANCE) return rangeToReformat

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

    private fun isEnableFormat(): Boolean {
        val setting = DomaToolsFunctionEnableSettings.getInstance()
        val isEnableFormat = setting.state.isEnableSqlFormat
        return isEnableFormat
    }
}
