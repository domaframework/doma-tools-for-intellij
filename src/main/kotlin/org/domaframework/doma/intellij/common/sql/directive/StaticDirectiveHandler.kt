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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.sql.directive.collector.FunctionCallCollector
import org.domaframework.doma.intellij.common.sql.directive.collector.StaticClassPackageCollector
import org.domaframework.doma.intellij.common.sql.directive.collector.StaticPropertyCollector
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

        if (isNextStaticFieldAccess(element)) {
            handleResult = staticDirectiveHandler(element, result)
        }
        if (handleResult) return true

        if (PsiTreeUtil.nextLeaf(element)?.elementType == SqlTypes.AT_SIGN ||
            element.elementType == SqlTypes.AT_SIGN
        ) {
            val module = element.module ?: return false
            handleResult =
                collectionModulePackages(
                    module,
                    result,
                )
        }
        if (handleResult) return true

        if (PsiTreeUtil.prevLeaf(element)?.elementType == SqlTypes.AT_SIGN) {
            // Built-in function completion
            handleResult = builtInDirectiveHandler(element, result)
        }
        return handleResult
    }

    private fun isNextStaticFieldAccess(element: PsiElement): Boolean {
        val prev = PsiTreeUtil.prevLeaf(element)
        return element.prevSibling is SqlElStaticFieldAccessExpr ||
            (
                prev?.elementType == SqlTypes.AT_SIGN &&
                    prev.parent is SqlElStaticFieldAccessExpr
            )
    }

    private fun staticDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
    ): Boolean {
        val clazzRef =
            PsiTreeUtil
                .getChildOfType(element.prevSibling, SqlElClass::class.java)
                ?: PsiTreeUtil.getChildOfType(PsiTreeUtil.prevLeaf(element)?.parent, SqlElClass::class.java)
        val fqdn =
            PsiTreeUtil.getChildrenOfTypeAsList(clazzRef, PsiElement::class.java).joinToString("") { it.text }

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
