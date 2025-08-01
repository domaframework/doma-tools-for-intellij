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
package org.domaframework.doma.intellij.common.sql.directive

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.domaframework.doma.intellij.common.sql.directive.collector.FunctionCallCollector
import org.domaframework.doma.intellij.common.sql.directive.collector.StaticClassPackageCollector
import org.domaframework.doma.intellij.common.sql.directive.collector.StaticPropertyCollector
import org.domaframework.doma.intellij.common.util.StringUtil.SINGLE_SPACE
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.jetbrains.kotlin.idea.base.util.module

class StaticDirectiveHandler(
    originalFile: PsiElement,
    private val element: PsiElement,
    private val caretNextText: String,
    private val result: CompletionResultSet,
    private val bindText: String,
) : DirectiveHandler(originalFile) {
    override fun directiveHandle(): Boolean {
        var handleResult = false

        if (isStaticFieldAccessTopElement(element)) {
            handleResult = staticDirectiveHandler(element, result)
        }
        if (handleResult) return true

        if (isSqlElClassCompletion()) {
            val module = element.module ?: return false
            handleResult =
                collectionModulePackages(
                    module,
                    result,
                )
        }
        if (handleResult) return true

        if (PsiTreeUtil.prevLeaf(element, true)?.elementType == SqlTypes.AT_SIGN) {
            // Built-in function completion
            handleResult = builtInDirectiveHandler(element, result)
        }
        return handleResult
    }

    /**
     * Determines whether code completion is needed for [SqlElClass] elements.
     */
    private fun isSqlElClassCompletion(): Boolean {
        if (element.elementType == SqlTypes.AT_SIGN &&
            PsiTreeUtil.prevLeaf(element)?.elementType == SqlTypes.AT_SIGN
        ) {
            return true
        }

        val elClassPattern = "^([a-zA-Z]*(\\.)+)*$"
        val regex = Regex(elClassPattern)
        val prevElements = PsiPatternUtil.getBindSearchWord(element, SqlTypes.AT_SIGN)
        val topAtSign = PsiTreeUtil.prevLeaf(prevElements.lastOrNull() ?: element, true)
        val prevWords = prevElements.reversed().joinToString("") { it.text }

        // If the cursor is in the middle of [SqlElClass],
        // search for the following @ and ensure that code completion is within [SqlElClass].
        if (element.elementType != SqlTypes.AT_SIGN) {
            var nextElement = PsiTreeUtil.nextLeaf(element, true)
            while (nextElement != null &&
                nextElement !is PsiWhiteSpace &&
                nextElement.elementType != SqlTypes.BLOCK_COMMENT_END &&
                nextElement.elementType != SqlTypes.AT_SIGN
            ) {
                nextElement = PsiTreeUtil.nextLeaf(nextElement, true)
            }
            val lastAtSign = PsiTreeUtil.nextLeaf(nextElement ?: element, true)
            if (regex.matches(prevWords) &&
                (
                    lastAtSign == null ||
                        nextElement.elementType != SqlTypes.AT_SIGN ||
                        lastAtSign.elementType == SqlTypes.BLOCK_COMMENT_END
                )
            ) {
                return false
            }
        }

        // Check if there is a partially entered class package name ahead and ensure that input is in [SqlElClass].
        if (prevElements.isEmpty()) return false
        return (
            topAtSign?.elementType == SqlTypes.AT_SIGN &&
                PsiTreeUtil.prevLeaf(topAtSign, true)?.elementType != SqlTypes.EL_IDENTIFIER &&
                regex.matches(prevWords)
        )
    }

    /**
     * Code completion for static properties after [SqlElClass].
     */
    private fun isStaticFieldAccessTopElement(element: PsiElement): Boolean {
        val prev = PsiTreeUtil.prevLeaf(element, true)
        val staticFieldAccess =
            PsiTreeUtil.getParentOfType(prev, SqlElStaticFieldAccessExpr::class.java)
        val sqlElClassWords = PsiPatternUtil.getBindSearchWord(element.containingFile, element, SINGLE_SPACE)
        return (
            staticFieldAccess != null && staticFieldAccess.elIdExprList.isEmpty()
        ) ||
            (
                prev?.elementType == SqlTypes.AT_SIGN &&
                    prev.parent is SqlElStaticFieldAccessExpr
            ) ||
            (
                sqlElClassWords.startsWith("@") &&
                    sqlElClassWords.endsWith("@")
            )
    }

    private fun staticDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
    ): Boolean {
        val prev = PsiTreeUtil.prevLeaf(element, true)
        val clazzRef =
            PsiTreeUtil
                .getChildOfType(prev, SqlElClass::class.java)
                ?: PsiTreeUtil.getChildOfType(PsiTreeUtil.prevLeaf(element)?.parent, SqlElClass::class.java)

        val sqlElClassWords = PsiPatternUtil.getBindSearchWord(element.containingFile, element, SINGLE_SPACE)
        val sqlElClassName = PsiTreeUtil.getChildrenOfTypeAsList(clazzRef, PsiElement::class.java).joinToString("") { it.text }
        val fqdn = if (sqlElClassName.isNotEmpty()) sqlElClassName else sqlElClassWords.replace("@", "")

        val collector = StaticPropertyCollector(element, caretNextText, bindText)
        val candidates = collector.collectCompletionSuggest(fqdn) ?: return false
        result.addAllElements(candidates.field)
        candidates.methods.forEach { m -> result.addElement(m) }
        return true
    }

    private fun collectionModulePackages(
        module: Module,
        result: CompletionResultSet,
    ): Boolean {
        val collector = StaticClassPackageCollector(element, module)
        val candidates = collector.collect() ?: return false
        result.addAllElements(candidates)
        return true
    }

    private fun builtInDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
    ): Boolean {
        if (BindDirectiveUtil.getDirectiveType(element) == DirectiveType.BUILT_IN) {
            val prefix = getBindSearchWord(element, bindText)
            val collector =
                FunctionCallCollector(element.containingFile, caretNextText, prefix)
            val candidates = collector.collect()
            candidates?.let { it1 -> result.addAllElements(it1) }
            return true
        }
        return false
    }
}
