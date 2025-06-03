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
package org.domaframework.doma.intellij.contributor.sql.processor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import org.domaframework.doma.intellij.extension.psi.isNotWhiteSpace
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElTermExpr
import org.domaframework.doma.intellij.psi.SqlTypes

abstract class SqlCompletionBlockProcessor {
    abstract fun generateBlock(targetElement: PsiElement): List<PsiElement>?

    protected fun filterBlocks(blocks: List<PsiElement>): List<PsiElement> =
        blocks
            .filter {
                it is SqlElIdExpr || it.elementType == SqlTypes.EL_IDENTIFIER || it == blocks.last()
            }.sortedBy { it.textOffset }

    // Get the list of elements before itself
    protected fun findSelfBlocks(targetElement: PsiElement): List<PsiElement> {
        val prevElements =
            targetElement.prevLeafs
                .takeWhile {
                    it.elementType != SqlTypes.BLOCK_COMMENT_START &&
                        !isSqlElSymbol(it) &&
                        it.elementType != SqlTypes.AT_SIGN &&
                        it !is SqlElTermExpr &&
                        it !is SqlBlockComment
                }.toList()

        var inParameter = false
        val formatElements = mutableListOf<PsiElement>()
        prevElements.forEach { prev ->
            if (prev.elementType == SqlTypes.RIGHT_PAREN) inParameter = true
            if (!inParameter) {
                formatElements.add(prev)
            }
            if (prev.elementType == SqlTypes.LEFT_PAREN) inParameter = false
        }

        val filterElements =
            formatElements
                .takeWhile { it !is PsiWhiteSpace }
                .filter {
                    it is PsiErrorElement ||
                        it is SqlElIdExpr ||
                        it.elementType == SqlTypes.EL_IDENTIFIER
                }.filter {
                    it.isNotWhiteSpace() &&
                        PsiTreeUtil.getParentOfType(it, SqlElParameters::class.java) == null
                }.plus(targetElement)
                .sortedBy { it.textOffset }

        return if (filterElements.isNotEmpty()) filterElements else emptyList()
    }

    private fun isSqlElSymbol(element: PsiElement): Boolean =
        element.elementType == SqlTypes.EL_PLUS ||
            element.elementType == SqlTypes.EL_MINUS ||
            element.elementType == SqlTypes.ASTERISK ||
            element.elementType == SqlTypes.EL_DIVIDE_EXPR ||
            element.elementType == SqlTypes.EL_EQ ||
            element.elementType == SqlTypes.EL_NE ||
            element.elementType == SqlTypes.EL_LE_EXPR ||
            element.elementType == SqlTypes.EL_LT_EXPR ||
            element.elementType == SqlTypes.EL_GE_EXPR ||
            element.elementType == SqlTypes.EL_GT_EXPR ||
            element.elementType == SqlTypes.EL_AND ||
            element.elementType == SqlTypes.EL_NOT ||
            element.elementType == SqlTypes.SEPARATOR
}
