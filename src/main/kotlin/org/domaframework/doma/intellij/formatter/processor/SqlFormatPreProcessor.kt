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
import com.intellij.psi.util.prevLeafs
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
        setOf(
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

    private val escapeCharacters = setOf("\"", "[", "`", "]")
    private val escapeEndCharacters = setOf("\"", "`", "]")

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
        if (!shouldFormatFile(source)) return rangeToReformat

        logging()

        if (!shouldProcessInjectedFile(source)) return rangeToReformat

        return updateDocument(source, rangeToReformat).range
    }

    private fun shouldFormatFile(source: PsiFile): Boolean = source.language == SqlLanguage.INSTANCE || isInjectedSqlFile(source)

    private fun shouldProcessInjectedFile(source: PsiFile): Boolean {
        if (!isInjectedSqlFile(source)) return true

        val host =
            InjectedLanguageManager
                .getInstance(source.project)
                .getInjectionHost(source) as? PsiLiteralExpression
        return host?.isTextBlock == true
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

        visitor.replaces.asReversed().forEach { current ->
            if (current.elementType != TokenType.WHITE_SPACE) {
                processNonWhiteSpaceElement(current, document)
                index--
                if (current.elementType == SqlTypes.KEYWORD) keywordIndex--
            } else {
                removeSpacesAroundNewline(document, current as PsiWhiteSpace)
            }
        }

        trimLeadingWhitespaceAtFileHead(document, source, rangeToReformat)
        docManager.commitDocument(document)

        return ProcessResult(document, rangeToReformat.grown(visitor.replaces.size))
    }

    private fun trimLeadingWhitespaceAtFileHead(
        document: Document,
        source: PsiFile,
        rangeToReformat: TextRange,
    ) {
        if (rangeToReformat.startOffset != 0) return
        if (isInjectedSqlFile(source)) return

        val chars = document.charsSequence
        if (chars.isEmpty()) return

        var start = 0
        // Save Bom
        if (chars[0] == '\uFEFF') start = 1

        var i = start
        while (i < chars.length) {
            val c = chars[i]
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                i++
            } else {
                break
            }
        }
        if (i > start) {
            document.deleteString(start, i)
        }
    }

    private fun processNonWhiteSpaceElement(
        current: PsiElement,
        document: Document,
    ) {
        val textRangeStart = current.startOffset
        val textRangeEnd = textRangeStart + current.text.length
        val newText = getProcessedText(current)

        document.deleteString(textRangeStart, textRangeEnd)
        document.insertString(textRangeStart, newText)
    }

    private fun getProcessedText(current: PsiElement): String {
        val newKeyword =
            when (current.elementType) {
                SqlTypes.KEYWORD -> processKeyword(current)
                SqlTypes.LEFT_PAREN -> getNewLineLeftParenString(current.prevSibling, getUpperText(current))
                SqlTypes.RIGHT_PAREN -> getRightPatternNewText(current)
                SqlTypes.WORD, SqlTypes.FUNCTION_NAME -> getWordNewText(current)
                SqlTypes.COMMA, SqlTypes.OTHER -> getNewLineString(current.prevSibling, getUpperText(current))
                SqlTypes.BLOCK_COMMENT_START -> processBlockCommentStart(current)
                else -> getUpperText(current)
            }

        return newKeyword
    }

    private fun processKeyword(current: PsiElement): String {
        val escapes = current.prevLeafs.filter { it.elementType == SqlTypes.OTHER }.toList()
        return if (hasEscapeBeforeWhiteSpace(escapes, current.node)) {
            current.text
        } else {
            getKeywordNewText(current)
        }
    }

    private fun processBlockCommentStart(current: PsiElement): String =
        if (current.nextSibling?.elementType == SqlTypes.BLOCK_COMMENT_CONTENT) {
            "$LINE_SEPARATE${getUpperText(current)}"
        } else {
            getNewLineString(PsiTreeUtil.prevLeaf(current), getUpperText(current))
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

        val newText = calculateNewWhiteSpaceText(element, nextElement, originalText, nextElementText, preElement)
        document.replaceString(range.startOffset, range.endOffset, newText)
    }

    private fun calculateNewWhiteSpaceText(
        element: PsiWhiteSpace,
        nextElement: PsiElement?,
        originalText: String,
        nextElementText: String,
        preElement: PsiElement?,
    ): String {
        if (!targetElementTypes.contains(nextElement?.elementType) && preElement?.elementType != SqlTypes.BLOCK_COMMENT) {
            return SINGLE_SPACE
        }

        if (element.prevSibling == null) return ""

        return when (nextElement.elementType) {
            SqlTypes.LINE_COMMENT -> processLineCommentWhiteSpace(originalText, nextElementText)
            else -> processDefaultWhiteSpace(originalText, nextElementText)
        }
    }

    private fun processLineCommentWhiteSpace(
        originalText: String,
        nextElementText: String,
    ): String =
        when {
            nextElementText.startsWith(LINE_SEPARATE) -> SINGLE_SPACE
            originalText.contains(LINE_SEPARATE) -> originalText.replace(Regex("\\s*\\n\\s*"), LINE_SEPARATE)
            else -> SINGLE_SPACE
        }

    private fun processDefaultWhiteSpace(
        originalText: String,
        nextElementText: String,
    ): String =
        when {
            nextElementText.contains(LINE_SEPARATE) -> SINGLE_SPACE
            originalText.contains(LINE_SEPARATE) -> originalText.replace(Regex("\\s*\\n\\s*"), LINE_SEPARATE)
            else -> LINE_SEPARATE
        }

    private fun hasEscapeBeforeWhiteSpace(
        prevBlocks: List<PsiElement>,
        start: ASTNode,
    ): Boolean {
        val escapeCount =
            prevBlocks.count {
                it.elementType == SqlTypes.OTHER && it.text in escapeCharacters
            }

        if (escapeCount % 2 == 0) return false

        return findEscapeEndCharacter(start)
    }

    private fun findEscapeEndCharacter(start: ASTNode): Boolean {
        var node = start.treeNext
        while (node != null) {
            when {
                node.elementType == SqlTypes.OTHER && node.text in escapeEndCharacters -> return true
                node.psi is PsiWhiteSpace -> return false
            }
            node = node.treeNext
        }
        return false
    }

    /**
     * Checks for special case keyword elements and specific combinations of keywords with line breaks and capitalization only
     */
    private fun getKeywordNewText(element: PsiElement): String {
        val keywordText = element.text.lowercase()
        val upperText = getUpperText(element)
        val shouldAddNewLine =
            SqlKeywordUtil.getIndentType(keywordText).isNewLineGroup() ||
                element.prevSibling.elementType == SqlTypes.BLOCK_COMMENT

        return if (shouldAddNewLine) {
            getNewLineString(element.prevSibling, upperText)
        } else {
            upperText
        }
    }

    private fun getRightPatternNewText(element: PsiElement): String {
        val elementText = element.text
        return getNewLineString(element.prevSibling, elementText)
    }

    private fun getWordNewText(element: PsiElement): String {
        val prev = findPreviousNonWhiteSpaceElement(element)

        return if (prev?.elementType == SqlTypes.BLOCK_COMMENT) {
            getNewLineString(element.prevSibling, getUpperText(element))
        } else {
            getUpperText(element)
        }
    }

    private fun findPreviousNonWhiteSpaceElement(element: PsiElement): PsiElement? {
        var prev = element.prevSibling
        while (prev != null && prev.elementType != SqlTypes.LEFT_PAREN && prev.elementType != SqlTypes.COMMA) {
            if (prev !is PsiWhiteSpace && prev.elementType != SqlTypes.LINE_COMMENT) {
                return prev
            }
            prev = prev.prevSibling
        }
        return prev
    }

    private fun getNewLineLeftParenString(
        prevElement: PsiElement?,
        text: String,
    ): String {
        val shouldAddNewLine =
            prevElement?.elementType == SqlTypes.BLOCK_COMMENT ||
                (
                    prevElement?.text?.contains(LINE_SEPARATE) == false &&
                        prevElement.prevSibling != null &&
                        prevElement.elementType != SqlTypes.FUNCTION_NAME
                )

        return if (shouldAddNewLine) "$LINE_SEPARATE$text" else text
    }

    private fun getNewLineString(
        prevElement: PsiElement?,
        text: String,
    ): String {
        val shouldAddNewLine =
            prevElement?.elementType == SqlTypes.BLOCK_COMMENT ||
                prevElement?.elementType == SqlTypes.BLOCK_COMMENT_END ||
                (prevElement?.text?.contains(LINE_SEPARATE) == false && prevElement.prevSibling != null)

        return if (shouldAddNewLine) "$LINE_SEPARATE$text" else text
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
