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
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf
import com.intellij.psi.util.prevLeafs
import com.intellij.util.ProcessingContext
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
                ): Boolean = PsiTreeUtil.getParentOfType(element, parentClass, true) != null
            },
        )

    fun createDirectivePattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement().with(
            object : PatternCondition<PsiElement>("PsiParentCondition") {
                override fun accepts(
                    element: PsiElement,
                    context: ProcessingContext?,
                ): Boolean {
                    val bindText = element.prevLeaf()?.text ?: ""
                    val directiveSymbol = listOf("%", "@", "^", "#")
                    return directiveSymbol.any {
                        bindText.startsWith(it) ||
                            (element.elementType == SqlTypes.EL_IDENTIFIER && element.prevLeaf()?.text == it) ||
                            (
                                element.elementType == TokenType.BAD_CHARACTER &&
                                    element.parent.prevLeafs
                                        .firstOrNull { p -> p.text == it || p.elementType == SqlTypes.BLOCK_COMMENT_START }
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
                ): Boolean = element.containingFile.originalFile.virtualFile.extension == extension
            },
        )

    /**
     * Get the string to search from the cursor position to the start of a block comment or a blank space
     * @return search Keyword
     */
    fun getBindSearchWord(
        originalFile: PsiElement,
        element: PsiElement,
        symbol: String,
    ): String {
        val text = originalFile.containingFile.text
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
}
