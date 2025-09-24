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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
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
import org.jetbrains.kotlin.utils.rethrow

class SqlFormatPreProcessor : PreFormatProcessor {
    data class TextReplacement(
        val offset: Int,
        val length: Int,
        val replacement: String,
    )

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

    private fun shouldFormatFile(source: PsiFile): Boolean = source.language == SqlLanguage.INSTANCE && !isInjectedSqlFile(source)

    private fun shouldProcessInjectedFile(source: PsiFile): Boolean {
        if (!isInjectedSqlFile(source)) return true

        val host =
            InjectedLanguageManager
                .getInstance(source.project)
                .getInjectionHost(source) as? PsiLiteralExpression
        return host?.isTextBlock == true
    }

    /**
     * PSI-safe fast processing
     */
    fun updateDocument(
        source: PsiFile,
        rangeToReformat: TextRange,
    ): ProcessResult {
        val docManager = PsiDocumentManager.getInstance(source.project)
        val document = docManager.getDocument(source) ?: return ProcessResult(null, rangeToReformat)

        if (!source.isValid) {
            return ProcessResult(document, rangeToReformat)
        }

        try {
            val visitor = SqlFormatVisitor()
            source.accept(visitor)

            if (!source.isValid) {
                return ProcessResult(document, rangeToReformat)
            }

            // Bulk string-based processing
            val originalText = document.getText(rangeToReformat)
            val formattedText =
                processTextWithPsiInfo(originalText, visitor.replaces, rangeToReformat.startOffset, document)

            if (formattedText != originalText) {
                val newRange =
                    ApplicationManager.getApplication().runWriteAction<TextRange> {
                        if (isInjectedSqlFile(source)) {
                            updateInjectedDocument(source, formattedText, rangeToReformat, docManager)
                        } else {
                            updateRegularDocument(document, formattedText, rangeToReformat, source, docManager)
                        }
                    }

                return ProcessResult(document, newRange)
            }

            return ProcessResult(document, rangeToReformat.grown(visitor.replaces.size))
        } catch (e: Exception) {
            rethrow(e)
            return ProcessResult(document, rangeToReformat)
        }
    }

    /**
     * Update injection document
     */
    private fun updateInjectedDocument(
        injectedFile: PsiFile,
        formattedText: String,
        rangeToReformat: TextRange,
        docManager: PsiDocumentManager,
    ): TextRange {
        val injectionManager = InjectedLanguageManager.getInstance(injectedFile.project)
        val injectionHost =
            injectionManager.getInjectionHost(injectedFile) as? PsiLiteralExpression
                ?: return rangeToReformat

        val hostFile = injectionHost.containingFile
        val hostDocument = docManager.getDocument(hostFile) ?: return rangeToReformat

        // Update injection document directly
        val injectedDocument = docManager.getDocument(injectedFile)
        if (injectedDocument != null && injectedDocument.isWritable) {
            injectedDocument.replaceString(
                rangeToReformat.startOffset,
                rangeToReformat.endOffset,
                formattedText,
            )

            docManager.commitDocument(injectedDocument)

            // Wait for PSI resynchronization
            PsiDocumentManager.getInstance(injectedFile.project).performForCommittedDocument(hostDocument) {
                if (injectedFile.isValid) {
                    return@performForCommittedDocument
                }
            }
        }

        return TextRange(rangeToReformat.startOffset, rangeToReformat.startOffset + formattedText.length)
    }

    /**
     * Regular document update
     */
    private fun updateRegularDocument(
        document: Document,
        formattedText: String,
        rangeToReformat: TextRange,
        source: PsiFile,
        docManager: PsiDocumentManager,
    ): TextRange {
        if (rangeToReformat.endOffset <= document.textLength) {
            val newText = trimLeadingWhitespaceAtFileHead(formattedText, source, rangeToReformat)
            document.replaceString(
                rangeToReformat.startOffset,
                rangeToReformat.endOffset,
                newText,
            )
            docManager.commitDocument(document)

            return TextRange(rangeToReformat.startOffset, rangeToReformat.startOffset + newText.length)
        }

        return rangeToReformat
    }

    /**
     * Fast string processing using PSI information
     */
    private fun processTextWithPsiInfo(
        originalText: String,
        psiElements: List<PsiElement>,
        baseOffset: Int,
        document: Document,
    ): String {
        val validElements =
            psiElements
                .filter { element ->
                    element.isValid && element.containingFile.isValid
                }.reversed()

        if (validElements.isEmpty()) return originalText

        val replacements = buildReplacementMap(validElements, baseOffset, document.charsSequence)
        return applyReplacements(originalText, replacements)
    }

    /**
     * Efficient replacement map construction
     */
    private fun buildReplacementMap(
        elements: List<PsiElement>,
        baseOffset: Int,
        documentText: CharSequence,
    ): List<TextReplacement> {
        val replacements = mutableListOf<TextReplacement>()

        var newNextElementStartOffset: Int? = elements.first().nextSibling?.startOffset
        var newNextElementText = elements.first().nextSibling?.text ?: ""
        var nextElementType: IElementType? = elements.first().nextSibling?.elementType
        elements.forEach { element ->
            val offset = element.startOffset
            if (offset < baseOffset || offset >= documentText.length) return@forEach

            val relativeOffset = offset - baseOffset
            val originalText = element.text

            val newText =
                when (element.elementType) {
                    TokenType.WHITE_SPACE -> {
                        if (element is PsiWhiteSpace) {
                            processWhiteSpaceElement(element, originalText, newNextElementStartOffset, newNextElementText, nextElementType)
                        } else {
                            originalText
                        }
                    }
                    SqlTypes.KEYWORD -> processKeywordElement(element)
                    SqlTypes.LEFT_PAREN -> processLeftParenElement(element)
                    SqlTypes.RIGHT_PAREN -> processRightParenElement(element)
                    SqlTypes.WORD, SqlTypes.FUNCTION_NAME -> processWordElement(element)
                    SqlTypes.COMMA, SqlTypes.OTHER -> processCommaOtherElement(element)
                    SqlTypes.BLOCK_COMMENT_START -> processBlockCommentStartElement(element)
                    else -> getUpperText(element)
                }
            newNextElementText = newText
            newNextElementStartOffset = offset
            nextElementType = element.elementType
            if (newText != originalText && relativeOffset >= 0) {
                replacements.add(
                    TextReplacement(relativeOffset, originalText.length, newText),
                )
            }
        }

        return replacements.sortedBy { it.offset }
    }

    /**
     * Fast string replacement
     */
    private fun applyReplacements(
        originalText: String,
        replacements: List<TextReplacement>,
    ): String {
        if (replacements.isEmpty()) return originalText

        val result = StringBuilder()
        var currentIndex = 0

        replacements.forEach { replacement ->
            if (replacement.offset > currentIndex) {
                result.append(originalText.substring(currentIndex, replacement.offset))
            }
            result.append(replacement.replacement)
            currentIndex = replacement.offset + replacement.length
        }
        if (currentIndex < originalText.length) {
            result.append(originalText.substring(currentIndex))
        }

        return result.toString()
    }

    /**
     * PSI element modification processing
     */
    private fun processWhiteSpaceElement(
        element: PsiWhiteSpace,
        originalText: String,
        newNextElementStartOffset: Int?,
        newNextElementText: String,
        nextElementType: IElementType?,
    ): String {
        val nextElementText =
            if (newNextElementStartOffset == element.endOffset) {
                newNextElementText
            } else {
                element.nextSibling?.text ?: ""
            }
        val nextElementType =
            if (newNextElementStartOffset == element.endOffset) {
                nextElementType
            } else {
                element.nextSibling?.elementType
            }
        val preElement = element.prevSibling

        if (!targetElementTypes.contains(nextElementType) &&
            preElement?.elementType != SqlTypes.BLOCK_COMMENT
        ) {
            return SINGLE_SPACE
        }

        if (element.prevSibling == null) return ""

        val hasNewline = originalText.indexOf(LINE_SEPARATE) >= 0

        return when (nextElementType) {
            SqlTypes.LINE_COMMENT -> {
                when {
                    nextElementText.startsWith(LINE_SEPARATE) -> SINGLE_SPACE
                    hasNewline -> LINE_SEPARATE.toString()
                    else -> SINGLE_SPACE
                }
            }
            else -> {
                when {
                    nextElementText.indexOf(LINE_SEPARATE) >= 0 -> SINGLE_SPACE
                    hasNewline -> LINE_SEPARATE.toString()
                    else -> LINE_SEPARATE.toString()
                }
            }
        }
    }

    private fun processKeywordElement(element: PsiElement): String {
        val escapes = element.prevLeafs.filter { it.elementType == SqlTypes.OTHER }.toList()
        return if (hasEscapeBeforeWhiteSpace(escapes, element.node)) {
            element.text
        } else {
            getKeywordNewText(element)
        }
    }

    private fun processLeftParenElement(element: PsiElement): String = getNewLineLeftParenString(element.prevSibling, getUpperText(element))

    private fun processRightParenElement(element: PsiElement): String = getNewLineString(element.prevSibling, element.text)

    private fun processWordElement(element: PsiElement): String {
        val prev = findPreviousNonWhiteSpaceElement(element)
        return if (prev?.elementType == SqlTypes.BLOCK_COMMENT) {
            getNewLineString(element.prevSibling, getUpperText(element))
        } else {
            getUpperText(element)
        }
    }

    private fun processCommaOtherElement(element: PsiElement): String = getNewLineString(element.prevSibling, getUpperText(element))

    private fun processBlockCommentStartElement(element: PsiElement): String =
        if (element.nextSibling?.elementType == SqlTypes.BLOCK_COMMENT_CONTENT) {
            "$LINE_SEPARATE${getUpperText(element)}"
        } else {
            getNewLineString(PsiTreeUtil.prevLeaf(element), getUpperText(element))
        }

    private fun hasEscapeBeforeWhiteSpace(
        prevBlocks: List<PsiElement>,
        start: ASTNode,
    ): Boolean {
        val escapeCount =
            prevBlocks.count {
                it.elementType == SqlTypes.OTHER && it.text in escapeCharacters
            }
        return if (escapeCount % 2 == 0) false else findEscapeEndCharacter(start)
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
                element.prevSibling?.elementType == SqlTypes.BLOCK_COMMENT

        return if (shouldAddNewLine) {
            getNewLineString(element.prevSibling, upperText)
        } else {
            upperText
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

    private fun trimLeadingWhitespaceAtFileHead(
        document: String,
        source: PsiFile,
        rangeToReformat: TextRange,
    ): String {
        if (rangeToReformat.startOffset != 0 || !source.isValid) return document
        if (isInjectedSqlFile(source)) return document

        if (document.isEmpty()) return document

        var start = 0
        if (document[0] == '\uFEFF') start = 1

        var i = start
        while (i < document.length) {
            val c = document[i]
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                i++
            } else {
                break
            }
        }
        if (i > start && i <= document.length) {
            document.removeRange(start, i)
        }
        return document
    }

    private fun logging() {
        PluginLoggerUtil.countLogging(
            this::class.java.simpleName,
            "SqlFormat",
            "Format",
            System.nanoTime(),
        )
    }
}
