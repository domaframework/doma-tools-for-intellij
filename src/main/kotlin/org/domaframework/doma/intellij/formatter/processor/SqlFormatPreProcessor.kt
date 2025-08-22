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

import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil.isInjectedSqlFile
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.common.util.StringUtil.LINE_SEPARATE
import org.domaframework.doma.intellij.common.util.StringUtil.SINGLE_SPACE
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.visitor.SqlFormatVisitor
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class SqlFormatPreProcessor : PreFormatProcessor {
    private val targetElementTypes =
        listOf(
            SqlTypes.KEYWORD,
            SqlTypes.LEFT_PAREN,
            SqlTypes.RIGHT_PAREN,
            SqlTypes.BLOCK_COMMENT_START,
            SqlTypes.BLOCK_COMMENT,
            SqlTypes.LINE_COMMENT,
            SqlTypes.WORD,
            SqlTypes.COMMA,
            SqlTypes.OTHER,
        )

    data class ProcessResult(
        val document: Document?,
        val range: TextRange,
    )

    override fun process(
        node: ASTNode,
        rangeToReformat: TextRange,
    ): TextRange = processText(node.psi.containingFile, rangeToReformat)

    private fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
    ): TextRange {
        // Turn on by default the code formatter that only runs when explicitly invoked by the user.
        // Handle both direct SQL files and injected SQL in Java files
        if (source.language != SqlLanguage.INSTANCE && !isInjectedSqlFile(source)) {
            return rangeToReformat
        }
        logging()

        // Do not execute processor processing in single-line text state
        if (isInjectedSqlFile(source)) {
            val host = InjectedLanguageManager.getInstance(source.project).getInjectionHost(source) as? PsiLiteralExpression
            if (host?.isTextBlock != true) return rangeToReformat
        }
        val result = updateDocument(source, rangeToReformat)
        return result.range
    }

    fun updateDocument(
        source: PsiFile,
        rangeToReformat: TextRange,
    ): ProcessResult {
        val visitor = SqlFormatVisitor()
        source.accept(visitor)

        val docManager = PsiDocumentManager.getInstance(source.project)
        val document = docManager.getDocument(source) ?: return ProcessResult(null, rangeToReformat)

        val keywordList = visitor.replaces.filter { it.elementType != TokenType.WHITE_SPACE }
        val replaceKeywordList = visitor.replaces.filter { it.elementType == SqlTypes.KEYWORD }
        var index = keywordList.size
        var keywordIndex = replaceKeywordList.size

        visitor.replaces.asReversed().forEach {
            val textRangeStart = it.startOffset
            val textRangeEnd = textRangeStart + it.text.length
            if (it.elementType != TokenType.WHITE_SPACE) {
                // Add a newline before any element that needs a newline+indent, without overlapping if there is already a newline
                index--
                var newKeyword = getUpperText(it)
                when (it.elementType) {
                    SqlTypes.KEYWORD -> {
                        keywordIndex--
                        newKeyword = getKeywordNewText(it)
                    }

                    SqlTypes.LEFT_PAREN -> {
                        newKeyword = getNewLineLeftParenString(it.prevSibling, getUpperText(it))
                    }

                    SqlTypes.RIGHT_PAREN -> {
                        newKeyword =
                            getRightPatternNewText(it)
                    }

                    SqlTypes.WORD, SqlTypes.FUNCTION_NAME -> {
                        newKeyword = getWordNewText(it, newKeyword)
                    }

                    SqlTypes.COMMA, SqlTypes.OTHER -> {
                        newKeyword = getNewLineString(it.prevSibling, getUpperText(it))
                    }

                    SqlTypes.BLOCK_COMMENT_START -> {
                        newKeyword =
                            getNewLineString(PsiTreeUtil.prevLeaf(it), getUpperText(it))
                    }
                }
                document.deleteString(textRangeStart, textRangeEnd)
                document.insertString(textRangeStart, newKeyword)
            } else {
                removeSpacesAroundNewline(document, it as PsiWhiteSpace)
            }
        }

        docManager.commitDocument(document)

        return ProcessResult(document, rangeToReformat.grown(visitor.replaces.size))
    }

    private fun removeSpacesAroundNewline(
        document: Document,
        element: PsiWhiteSpace,
    ) {
        val range = element.textRange
        val originalText = document.getText(range)
        val nextElement = element.nextSibling
        val nextElementText = nextElement?.let { document.getText(it.textRange) } ?: ""
        val preElement = element.prevSibling

        var newText = ""
        if (!targetElementTypes.contains(nextElement?.elementType) && preElement?.elementType != SqlTypes.BLOCK_COMMENT) {
            newText = originalText.replace(originalText, SINGLE_SPACE)
        } else {
            newText =
                if (element.prevSibling == null) {
                    ""
                } else {
                    when (nextElement.elementType) {
                        SqlTypes.LINE_COMMENT -> {
                            if (nextElementText.startsWith(LINE_SEPARATE)) {
                                originalText.replace(originalText, SINGLE_SPACE)
                            } else if (originalText.contains(LINE_SEPARATE)) {
                                originalText.replace(Regex("\\s*\\n\\s*"), LINE_SEPARATE)
                            } else {
                                originalText.replace(originalText, SINGLE_SPACE)
                            }
                        }

                        else -> {
                            if (nextElementText.contains(LINE_SEPARATE) == true) {
                                originalText.replace(originalText, SINGLE_SPACE)
                            } else if (originalText.contains(LINE_SEPARATE)) {
                                originalText.replace(Regex("\\s*\\n\\s*"), LINE_SEPARATE)
                            } else {
                                originalText.replace(originalText, LINE_SEPARATE)
                            }
                        }
                    }
                }
        }
        document.replaceString(range.startOffset, range.endOffset, newText)
    }

    /**
     * Checks for special case keyword elements and specific combinations of keywords with line breaks and capitalization only
     */
    private fun getKeywordNewText(element: PsiElement): String {
        val keywordText = element.text.lowercase()
        val upperText = getUpperText(element)
        return if (SqlKeywordUtil.getIndentType(keywordText).isNewLineGroup()) {
            val prevElement = element.prevSibling
            getNewLineString(prevElement, upperText)
        } else {
            upperText
        }
    }

    private fun getRightPatternNewText(element: PsiElement): String {
        val elementText = element.text
        return getNewLineString(element.prevSibling, elementText)
    }

    private fun getWordNewText(
        element: PsiElement,
        newKeyword: String,
    ): String {
        newKeyword
        var prev = element.prevSibling
        var isColumnName = true
        while (prev != null && prev.elementType != SqlTypes.LEFT_PAREN && prev.elementType != SqlTypes.COMMA) {
            if (prev !is PsiWhiteSpace &&
                prev.elementType != SqlTypes.LINE_COMMENT
            ) {
                isColumnName =
                    prev.elementType == SqlTypes.COMMA ||
                    prev.elementType == SqlTypes.LEFT_PAREN
                break
            }
            prev = prev.prevSibling
        }

        return if (prev.elementType == SqlTypes.BLOCK_COMMENT) {
            getNewLineString(element.prevSibling, getUpperText(element))
        } else {
            getUpperText(element)
        }
    }

    private fun getNewLineLeftParenString(
        prevElement: PsiElement?,
        text: String,
    ): String =
        if (prevElement?.elementType == SqlTypes.BLOCK_COMMENT ||
            (
                prevElement?.text?.contains(LINE_SEPARATE) == false &&
                    prevElement.prevSibling != null &&
                    prevElement.elementType != SqlTypes.FUNCTION_NAME
            )
        ) {
            "$LINE_SEPARATE$text"
        } else {
            text
        }

    private fun getNewLineString(
        prevElement: PsiElement?,
        text: String,
    ): String =
        if (prevElement?.elementType == SqlTypes.BLOCK_COMMENT ||
            (
                prevElement?.text?.contains(LINE_SEPARATE) == false &&
                    prevElement.prevSibling != null
            )
        ) {
            "$LINE_SEPARATE$text"
        } else {
            text
        }

    private fun getUpperText(element: PsiElement): String =
        if (element.elementType == SqlTypes.KEYWORD) {
            element.text.uppercase()
        } else {
            element.text
        }

    private fun logging() {
        PluginLoggerUtil.Companion.countLogging(
            this::class.java.simpleName,
            "SqlFormat",
            "Format",
            System.nanoTime(),
        )
    }
}
