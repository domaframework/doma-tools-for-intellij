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

import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import org.domaframework.doma.intellij.common.util.DomaToolsSettingUtil
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class SqlFormatPreProcessor : PreFormatProcessor {
    override fun process(
        node: ASTNode,
        rangeToReformat: TextRange,
    ): TextRange = processText(node.psi.containingFile, rangeToReformat)

    private fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
    ): TextRange {
        if (!DomaToolsSettingUtil.isEnableFormat(source.project)) return rangeToReformat
        if (source.language != SqlLanguage.INSTANCE) return rangeToReformat

        logging()

        val visitor = SqlFormatVisitor()
        source.accept(visitor)

        val docManager = PsiDocumentManager.getInstance(source.project)
        val document = docManager.getDocument(source) ?: return rangeToReformat

        val keywordList = visitor.replaces.filter { it.elementType != TokenType.WHITE_SPACE }
        val replaceKeywordList = visitor.replaces.filter { it.elementType == SqlTypes.KEYWORD }
        var index = keywordList.size
        var keywordIndex = replaceKeywordList.size

        visitor.replaces.asReversed().forEach {
            val createQueryType = getCreateQueryGroup(keywordList, index)
            val textRangeStart = it.startOffset
            val textRangeEnd = textRangeStart + it.text.length
            if (it.elementType != TokenType.WHITE_SPACE) {
                // Add a newline before any element that needs a newline+indent, without overlapping if there is already a newline
                index--
                var newKeyword = getUpperText(it)
                when (it.elementType) {
                    SqlTypes.KEYWORD -> {
                        keywordIndex--
                        newKeyword = getKeywordNewText(index, it, createQueryType, keywordList)
                    }

                    SqlTypes.LEFT_PAREN -> {
                        newKeyword =
                            if (createQueryType == CreateQueryType.TABLE) {
                                getNewLineString(it.prevSibling, getUpperText(it))
                            } else if (keywordIndex > 0) {
                                if (replaceKeywordList[keywordIndex - 1].text.lowercase() == "insert" ||
                                    replaceKeywordList[keywordIndex - 1].text.lowercase() == "into"
                                ) {
                                    getNewLineString(it.prevSibling, getUpperText(it))
                                } else {
                                    getUpperText(it)
                                }
                            } else {
                                getUpperText(it)
                            }
                    }

                    SqlTypes.RIGHT_PAREN -> {
                        newKeyword =
                            getRightPatternNewText(
                                it,
                                newKeyword,
                                replaceKeywordList[keywordIndex - 1],
                                createQueryType,
                            )
                    }

                    SqlTypes.WORD -> {
                        newKeyword = getWordNewText(it, newKeyword, createQueryType)
                    }

                    SqlTypes.COMMA, SqlTypes.BLOCK_COMMENT, SqlTypes.OTHER -> {
                        newKeyword = getNewLineString(it.prevSibling, getUpperText(it))
                    }
                }
                document.deleteString(textRangeStart, textRangeEnd)
                document.insertString(textRangeStart, newKeyword)
            } else {
                // Remove spaces after newlines to reset indentation
                val nextSibling = it.nextSibling
                if (nextSibling.elementType == SqlTypes.BLOCK_COMMENT) {
                    removeSpacesAroundNewline(document, it.textRange)
                } else if (keywordIndex < replaceKeywordList.size) {
                    val nextElement = replaceKeywordList[keywordIndex]
                    if (isNewLineOnlyCreateTable(nextSibling) && createQueryType == CreateQueryType.TABLE) {
                        removeSpacesAroundNewline(document, it.textRange)
                    } else if (isSubGroupFirstElement(nextElement)) {
                        document.deleteString(textRangeStart, textRangeEnd)
                    } else if (isCreateViewAs(replaceKeywordList[keywordIndex], createQueryType)) {
                        removeSpacesAroundNewline(document, it.textRange)
                    } else {
                        val isNewLineGroup =
                            SqlKeywordUtil.getIndentType(nextElement.text ?: "").isNewLineGroup()
                        val isSetLineKeyword =
                            if (keywordIndex > 0) {
                                SqlKeywordUtil.isSetLineKeyword(
                                    nextElement.text,
                                    replaceKeywordList[keywordIndex - 1].text,
                                )
                            } else {
                                false
                            }

                        if (isNewLineGroup && !isSetLineKeyword || keywordList[index].elementType == SqlTypes.COMMA) {
                            removeSpacesAroundNewline(document, it.textRange)
                        } else {
                            document.replaceString(textRangeStart, textRangeEnd, " ")
                        }
                    }
                } else {
                    removeSpacesAroundNewline(document, it.textRange)
                }
            }
        }

        docManager.commitDocument(document)

        return rangeToReformat.grown(visitor.replaces.size)
    }

    private fun removeSpacesAroundNewline(
        document: Document,
        range: TextRange,
    ) {
        val originalText = document.getText(range)
        val newText = originalText.replace(Regex("\\s*\\n\\s*"), "\n")
        document.replaceString(range.startOffset, range.endOffset, newText)
    }

    /**
     * Checks for special case keyword elements and specific combinations of keywords with line breaks and capitalization only
     */
    private fun getKeywordNewText(
        index: Int,
        element: PsiElement,
        createQueryType: CreateQueryType,
        keywordList: List<PsiElement>,
    ): String =
        if (element.text.lowercase() == "end") {
            getNewLineString(element.prevSibling, getUpperText(element))
        } else if (isCreateViewAs(element, createQueryType)) {
            getNewLineString(element.prevSibling, getUpperText(element))
        } else if (isSubGroupFirstElement(element)) {
            getUpperText(element)
        } else if (SqlKeywordUtil.getIndentType(element.text).isNewLineGroup()) {
            if (index > 0 &&
                SqlKeywordUtil.isSetLineKeyword(
                    element.text,
                    keywordList[index - 1].text,
                )
            ) {
                getUpperText(element)
            } else {
                getNewLineString(element.prevSibling, getUpperText(element))
            }
        } else {
            getUpperText(element)
        }

    private fun getRightPatternNewText(
        element: PsiElement,
        keyword: String,
        nextKeyword: PsiElement,
        createQueryType: CreateQueryType,
    ): String {
        var newKeyword = keyword
        val elementText = element.text
        if (createQueryType == CreateQueryType.TABLE) {
            val prefixElements =
                getElementsBeforeKeyword(element.prevLeafs.toList()) { it.elementType == SqlTypes.LEFT_PAREN }
            val containsColumnRaw =
                prefixElements.findLast { isColumnDefinedRawElementType(it) } != null
            newKeyword =
                if (containsColumnRaw) {
                    getNewLineString(element.prevSibling, elementText)
                } else {
                    elementText
                }
        } else if (nextKeyword.text.lowercase() == "set") {
            newKeyword = getNewLineString(element.prevSibling, elementText)
        } else {
            newKeyword = elementText
        }
        return newKeyword
    }

    private fun getWordNewText(
        element: PsiElement,
        newKeyword: String,
        createQueryType: CreateQueryType,
    ): String {
        newKeyword
        var prev = element.prevSibling
        var isColumnName = true
        while (prev != null && prev.elementType != SqlTypes.LEFT_PAREN && prev.elementType != SqlTypes.COMMA) {
            if (prev !is PsiWhiteSpace &&
                prev.elementType != SqlTypes.LINE_COMMENT &&
                prev.elementType != SqlTypes.BLOCK_COMMENT
            ) {
                isColumnName =
                    prev.elementType == SqlTypes.COMMA ||
                    prev.elementType == SqlTypes.LEFT_PAREN
                break
            }
            prev = prev.prevSibling
        }

        return if (createQueryType == CreateQueryType.TABLE && isColumnName) {
            getNewLineString(element.prevSibling, getUpperText(element))
        } else {
            getUpperText(element)
        }
    }

    private fun isCreateViewAs(
        element: PsiElement,
        createQueryType: CreateQueryType,
    ): Boolean =
        element.text.lowercase() == "as" &&
            createQueryType == CreateQueryType.VIEW

    private fun isColumnDefinedRawElementType(element: PsiElement): Boolean =
        element.elementType == SqlTypes.WORD ||
            element.elementType == SqlTypes.KEYWORD ||
            element.elementType == SqlTypes.COMMA

    private fun getCreateQueryGroup(
        keywordList: List<PsiElement>,
        index: Int,
    ): CreateQueryType {
        var topLastKeyWord: PsiElement? = null
        var attachmentKeywordType = CreateQueryType.NONE
        keywordList
            .dropLast(keywordList.size.minus(index))
            .filter {
                it.elementType == SqlTypes.KEYWORD
            }.asReversed()
            .forEach { key ->
                if (SqlKeywordUtil.isTopKeyword(key.text)) {
                    topLastKeyWord = key
                    return@forEach
                }
                if (SqlKeywordUtil.isAttachedKeyword(key.text)) {
                    attachmentKeywordType = CreateQueryType.getCreateTableType(key.text)
                }
            }
        val prevKeywordText = topLastKeyWord?.text?.lowercase()
        val isCreateGroup = prevKeywordText == "create"
        if (!isCreateGroup) return CreateQueryType.NONE
        return attachmentKeywordType
    }

    /**
     * The column definition elements of Create Table, "(", "WORD", and ")" must be line breaks
     */
    private fun isNewLineOnlyCreateTable(nextElement: PsiElement): Boolean =
        nextElement.elementType == SqlTypes.LEFT_PAREN ||
            nextElement.elementType == SqlTypes.RIGHT_PAREN ||
            nextElement.elementType == SqlTypes.WORD

    fun <T> getElementsBeforeKeyword(
        elements: List<T>,
        isLeft: (T) -> Boolean,
    ): List<T> = elements.takeWhile { element -> !isLeft(element) }

    private fun getNewLineString(
        prevElement: PsiElement?,
        text: String,
    ): String =
        if (prevElement?.text?.contains("\n") == false) {
            "\n$text"
        } else {
            text
        }

    private fun getUpperText(element: PsiElement): String =
        if (element.elementType == SqlTypes.KEYWORD) {
            element.text.uppercase()
        } else {
            element.text
        }

    private fun isSubGroupFirstElement(element: PsiElement): Boolean =
        getElementsBeforeKeyword(element.prevLeafs.toList()) { it.elementType == SqlTypes.LEFT_PAREN }
            .findLast { it !is PsiWhiteSpace } == null

    private fun logging() {
        PluginLoggerUtil.countLogging(
            this::class.java.simpleName,
            "SqlFormat",
            "Format",
            System.nanoTime(),
        )
    }
}

private class SqlFormatVisitor : PsiRecursiveElementVisitor() {
    val replaces = mutableListOf<PsiElement>()
    var lastElement: PsiElement? = null

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element !is PsiFile && element.nextSibling == null) {
            lastElement = element
        }

        if (PsiTreeUtil.getParentOfType(element, SqlBlockComment::class.java) == null) {
            when (element.elementType) {
                SqlTypes.KEYWORD, SqlTypes.COMMA, SqlTypes.LEFT_PAREN, SqlTypes.RIGHT_PAREN, SqlTypes.WORD -> {
                    replaces.add(element)
                }

                SqlTypes.OTHER -> {
                    if (element.text == "=") {
                        val updateSetKeyword =
                            replaces
                                .lastOrNull { it.elementType == SqlTypes.KEYWORD }
                        if (updateSetKeyword?.text?.lowercase() == "set") {
                            replaces.add(element)
                        }
                    }
                }

                SqlTypes.BLOCK_COMMENT ->
                    if (
                        element is SqlCustomElCommentExpr &&
                        element.isConditionOrLoopDirective()
                    ) {
                        replaces.add(element)
                    }
            }
        }
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        super.visitWhiteSpace(space)
        val nextElement = space.nextSibling
        if (nextElement != null &&
            (
                space.text.contains("\n") ||
                    nextElement.elementType == SqlTypes.LINE_COMMENT ||
                    nextElement.elementType == SqlTypes.BLOCK_COMMENT
            )
        ) {
            replaces.add(space)
        }
    }
}
