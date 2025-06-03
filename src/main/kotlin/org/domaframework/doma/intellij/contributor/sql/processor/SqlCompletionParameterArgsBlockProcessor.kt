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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import org.domaframework.doma.intellij.extension.psi.isNotWhiteSpace
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlTypes
import kotlin.collections.addAll

class SqlCompletionParameterArgsBlockProcessor(
    private val targetElement: PsiElement,
) : SqlCompletionBlockProcessor() {
    override fun generateBlock(targetElement: PsiElement): List<PsiElement> = emptyList()

    /**
     * Generates the first argument block for a given parameter parent element.
     * This method collects all children of the parameter parent, including nested field access expressions.
     *
     * @param parameterParent The parent PsiElement of the parameter.
     * @return A list of PsiElements representing the first argument block.
     */
    fun generateFirstArgsBlock(parameterParent: PsiElement): List<PsiElement> {
        val children = mutableListOf<PsiElement>()
        parameterParent.children.forEach { child ->
            if (child is SqlElFieldAccessExpr) {
                children.addAll(child.children)
            } else {
                children.add(child)
            }
        }
        return children
    }

    /**
     * Retrieves the parent element of the second argument block.
     * This method traverses previous leaf elements to find the parent of type SqlElParameters.
     *
     * @return The parent PsiElement of the second argument block, or null if not found.
     */
    fun getSecondArgsParent(): PsiElement? =
        targetElement.prevLeafs
            .takeWhile {
                it.isNotWhiteSpace() &&
                    it.elementType != SqlTypes.LEFT_PAREN
            }.firstOrNull {
                PsiTreeUtil.getParentOfType(
                    it,
                    SqlElParameters::class.java,
                ) != null
            }

    fun generateSecondArgsBlock(parameterArg: PsiElement): List<PsiElement> {
        val parameterParent = PsiTreeUtil.getParentOfType(parameterArg, SqlElParameters::class.java)
        val children = mutableListOf<PsiElement>()
        parameterParent
            ?.children
            ?.reversed()
            ?.takeWhile {
                it.nextSibling?.elementType != SqlTypes.COMMA
            }?.forEach { child ->
                if (child is SqlElFieldAccessExpr) {
                    children.addAll(child.children)
                } else {
                    children.add(child)
                }
            }
        val blocks = children.reversed()

        return filterBlocks(blocks)
    }
}
