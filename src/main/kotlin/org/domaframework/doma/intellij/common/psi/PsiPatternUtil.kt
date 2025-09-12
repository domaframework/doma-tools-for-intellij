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
package org.domaframework.doma.intellij.common.psi

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import com.intellij.util.ProcessingContext
import org.domaframework.doma.intellij.common.sql.directive.DirectiveCompletion
import org.domaframework.doma.intellij.common.util.StringUtil.SINGLE_SPACE
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlTypes

object PsiPatternUtil {
    /**
     * Creates a pattern that matches elements under a PsiComment or a specific parent class,
     * regardless of depth.
     *
     * @param parentClass The specific class to match as a parent.
     * @return A PsiElementPattern that matches the desired elements.
     */
    fun <R : PsiElement> createPattern(parentClass: Class<R>): PsiElementPattern.Capture<PsiElement> =
        PlatformPatterns.psiElement().with(
            object : PatternCondition<PsiElement>("PsiParentCondition") {
                override fun accepts(
                    element: PsiElement,
                    context: ProcessingContext?,
                ): Boolean {
                    val inComment = PsiTreeUtil.getParentOfType(element, parentClass, true) != null
                    if (inComment) return true

                    // Even incomplete elements during input are analyzed as strings and included in the code completion process within block comments.
                    var prevElement = PsiTreeUtil.prevLeaf(element, true)
                    while (prevElement != null &&
                        !(
                            prevElement.nextSibling is SqlCustomElCommentExpr &&
                                !prevElement.nextSibling.text.endsWith("*/")
                        )
                    ) {
                        prevElement = PsiTreeUtil.prevLeaf(prevElement, true)
                    }

                    var endBlock = PsiTreeUtil.nextLeaf(element, true)
                    while (endBlock != null && endBlock.elementType != SqlTypes.BLOCK_COMMENT_END) {
                        endBlock = PsiTreeUtil.nextLeaf(endBlock, true)
                    }

                    return prevElement != null && endBlock != null
                }
            },
        )

    fun createDirectivePattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement().with(
            object : PatternCondition<PsiElement>("PsiParentCondition") {
                override fun accepts(
                    element: PsiElement,
                    context: ProcessingContext?,
                ): Boolean {
                    val bindText = PsiTreeUtil.prevLeaf(element)?.text ?: ""
                    val directiveSymbol = DirectiveCompletion.directiveSymbols
                    return directiveSymbol.any {
                        bindText.startsWith(it) ||
                            (element.elementType == SqlTypes.EL_IDENTIFIER && PsiTreeUtil.prevLeaf(element)?.text == it) ||
                            (
                                element.elementType == TokenType.BAD_CHARACTER &&
                                    element.parent
                                        ?.prevLeafs
                                        ?.firstOrNull { p -> p.text == it || p.elementType == SqlTypes.BLOCK_COMMENT_START }
                                        ?.text == it
                            )
                    }
                }
            },
        )
    }

    fun isMatchFileExtension(extension: String): PsiElementPattern.Capture<PsiElement> =
        PlatformPatterns.psiElement().with(
            object : PatternCondition<PsiElement>("PsiParentCondition") {
                override fun accepts(
                    element: PsiElement,
                    context: ProcessingContext?,
                ): Boolean =
                    element.containingFile
                        ?.originalFile
                        ?.virtualFile
                        ?.extension == extension
            },
        )

    fun createReferencePattern(): PsiElementPattern.Capture<PsiElement> =
        PlatformPatterns.psiElement().with(
            object : PatternCondition<PsiElement>("PsiReferenceParentCondition") {
                override fun accepts(
                    element: PsiElement,
                    context: ProcessingContext?,
                ): Boolean {
                    if (element is SqlElClass) return true
                    if (element is SqlElIdExpr) {
                        return PsiTreeUtil.getParentOfType(element, SqlElClass::class.java) == null
                    }
                    return false
                }
            },
        )

    /**
     * Get the string to search from the cursor position to the start of a block comment or a blank space
     * @return The string up to the specified character
     */
    fun getBindSearchWord(
        originalFile: PsiElement,
        element: PsiElement,
        symbol: String,
    ): String {
        val text = originalFile.containingFile?.text ?: SINGLE_SPACE
        val offset = element.textOffset
        val builder = StringBuilder()
        for (i in offset - 1 downTo 0) {
            val char = text[i]
            if (char.isWhitespace()) break
            builder.insert(0, char)
        }
        val prefix =
            builder
                .toString()
                .replace("/*", "")
                .substringAfter(symbol)
        return prefix
    }

    fun getBindSearchWord(
        element: PsiElement,
        targetType: IElementType?,
    ): MutableList<PsiElement> {
        var prevElement = PsiTreeUtil.prevLeaf(element, true)
        val prevElements = mutableListOf<PsiElement>()
        while (prevElement != null &&
            prevElement !is PsiWhiteSpace &&
            prevElement.elementType != targetType &&
            prevElement.elementType != SqlTypes.BLOCK_COMMENT_START
        ) {
            prevElements.add(prevElement)
            prevElement = PsiTreeUtil.prevLeaf(prevElement, true)
        }
        return prevElements
    }
}
