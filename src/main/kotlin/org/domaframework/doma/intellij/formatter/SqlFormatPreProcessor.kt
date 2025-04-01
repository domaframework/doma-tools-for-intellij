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
import org.domaframework.doma.intellij.psi.SqlBlockComment
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
        if (source.language != SqlLanguage.INSTANCE) return rangeToReformat

        val visitor = SqlFormatVisitor()
        source.accept(visitor)

        val docManager = PsiDocumentManager.getInstance(source.project)
        val document = docManager.getDocument(source) ?: return rangeToReformat

        val keywordList = visitor.replaces.filter { it.elementType != TokenType.WHITE_SPACE }
        val replaceKeywordList = visitor.replaces.filter { it.elementType == SqlTypes.KEYWORD }
        var index = keywordList.size
        var keywordIndex = replaceKeywordList.size

        val documentLastElement = visitor.lastElement
        val documentLastRange = visitor.lastElement?.textRange
        if (documentLastRange != null && documentLastRange.endOffset <= rangeToReformat.endOffset) {
            if (documentLastElement !is PsiWhiteSpace || documentLastElement.text?.contains("\n") == false) {
                document.insertString(documentLastRange.endOffset, "\n")
            }
        }

        visitor.replaces.asReversed().forEach {
            val createQueryType = getCreateQueryGroup(keywordList, index)
            val textRangeStart = it.startOffset
            val textRangeEnd = textRangeStart + it.text.length
            if (it.elementType != TokenType.WHITE_SPACE) {
                index--
                var newKeyword = getUpperText(it)
                when (it.elementType) {
                    SqlTypes.KEYWORD -> {
                        keywordIndex--
                        newKeyword =
                            if (checkKeywordPrevElement(index, it) &&
                                SqlKeywordUtil.getIndentType(it.text).isNewLineGroup() ||
                                it.text.lowercase() == "end" ||
                                (
                                    it.text.lowercase() == "as" &&
                                        createQueryType == CreateQueryType.VIEW
                                )
                            ) {
                                if (SqlKeywordUtil.isSetLineKeyword(
                                        it.text,
                                        keywordList[index - 1].text,
                                    )
                                ) {
                                    getUpperText(it)
                                } else {
                                    getNewLineString(it)
                                }
                            } else {
                                getUpperText(it)
                            }
                    }
                    SqlTypes.LEFT_PAREN -> {
                        newKeyword =
                            if (createQueryType == CreateQueryType.TABLE) {
                                getNewLineString(it)
                            } else {
                                getUpperText(it)
                            }
                    }
                    SqlTypes.RIGHT_PAREN -> {
                        val prefixElements =
                            getElementsBeforeKeyword(it.prevLeafs.toList()) { it.elementType == SqlTypes.LEFT_PAREN }
                        val containsColumnRaw =
                            prefixElements.findLast { isColumnDefinedRawElementType(it) } != null
                        newKeyword =
                            if (createQueryType == CreateQueryType.TABLE) {
                                if (containsColumnRaw) {
                                    getNewLineString(it)
                                } else {
                                    getUpperText(it)
                                }
                            } else {
                                getUpperText(it)
                            }
                    }

                    SqlTypes.WORD -> {
                        var prev = it.prevSibling
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

                        newKeyword =
                            if (createQueryType == CreateQueryType.TABLE && isColumnName) {
                                getNewLineString(it)
                            } else {
                                getUpperText(it)
                            }
                    }
                    SqlTypes.COMMA -> {
                        newKeyword = getNewLineString(it)
                    }
                }
                document.deleteString(textRangeStart, textRangeEnd)
                document.insertString(textRangeStart, newKeyword)
            } else {
                if (keywordIndex < replaceKeywordList.size) {
                    val nextElement = replaceKeywordList[keywordIndex]
                    if ((
                            SqlKeywordUtil.getIndentType(nextElement.text ?: "").isNewLineGroup() &&
                                SqlKeywordUtil.isSetLineKeyword(
                                    replaceKeywordList[keywordIndex].text,
                                    replaceKeywordList[keywordIndex - 1].text,
                                )
                        ) ||
                        (
                            isNewLineOnlyCreateTable(nextElement) && createQueryType == CreateQueryType.TABLE
                        )
                    ) {
                        document.deleteString(textRangeStart, textRangeEnd)
                        document.insertString(textRangeStart, " ")
                    } else {
                        val currentIndent = it.text.substringAfter("\n", "").length
                        val start = textRangeEnd - currentIndent
                        document.deleteString(start, textRangeEnd)
                    }
                } else {
                    val currentIndent = it.text.substringAfter("\n", "").length
                    val start = textRangeEnd - currentIndent
                    document.deleteString(start, textRangeEnd)
                }
            }
        }

        docManager.commitDocument(document)

        return rangeToReformat.grown(visitor.replaces.size)
    }

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

    private fun isNewLineOnlyCreateTable(nextElement: PsiElement): Boolean =
        nextElement.elementType == SqlTypes.LEFT_PAREN ||
            nextElement.elementType == SqlTypes.RIGHT_PAREN ||
            nextElement.elementType == SqlTypes.WORD

    fun <T> getElementsBeforeKeyword(
        elements: List<T>,
        isLeft: (T) -> Boolean,
    ): List<T> = elements.takeWhile { element -> !isLeft(element) }

    private fun getNewLineString(element: PsiElement): String =
        if (element.prevSibling?.text?.contains("\n") == false) {
            "\n${getUpperText(element)}"
        } else {
            getUpperText(element)
        }

    private fun getUpperText(element: PsiElement): String =
        if (element.elementType == SqlTypes.KEYWORD) {
            element.text.uppercase()
        } else {
            element.text
        }

    private fun checkKeywordPrevElement(
        index: Int,
        element: PsiElement,
    ): Boolean =
        index > 0 &&
            element.prevSibling != null &&
            element.prevSibling.elementType != SqlTypes.LEFT_PAREN
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
                SqlTypes.KEYWORD, SqlTypes.COMMA -> {
                    replaces.add(element)
                }
                SqlTypes.LEFT_PAREN, SqlTypes.RIGHT_PAREN, SqlTypes.WORD -> {
                    replaces.add(element)
                }
            }
        }
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        super.visitWhiteSpace(space)
        val nextElement = space.nextSibling
        if (nextElement != null &&
            space.text.contains("\n")
        ) {
            replaces.add(space)
        }
    }
}
