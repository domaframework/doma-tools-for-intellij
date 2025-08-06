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

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import org.domaframework.doma.intellij.common.util.StringUtil

abstract class SqlPostProcessor : PostFormatProcessor {
    private val trailingSpacesRegex = Regex(" +(\r?\n)")

    override fun processElement(
        element: PsiElement,
        settings: CodeStyleSettings,
    ): PsiElement = element

    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings,
    ): TextRange = rangeToReformat

    protected fun processDocumentText(originalText: String): String {
        val withoutTrailingSpaces = removeTrailingSpaces(originalText)
        return ensureProperFileEnding(withoutTrailingSpaces)
    }

    private fun removeTrailingSpaces(text: String): String = text.replace(trailingSpacesRegex, "$1")

    private fun ensureProperFileEnding(text: String): String = text.trimEnd() + StringUtil.LINE_SEPARATE
}
