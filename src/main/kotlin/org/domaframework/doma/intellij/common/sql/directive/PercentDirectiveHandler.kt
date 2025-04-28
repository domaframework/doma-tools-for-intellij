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
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.prevLeafs

class PercentDirectiveHandler(
    originalFile: PsiElement,
    private val element: PsiElement,
    private val result: CompletionResultSet,
) : DirectiveHandler(originalFile) {
    override fun directiveHandle(): Boolean =
        percentDirectiveHandler(
            element,
            result,
        ) { bind ->
            val prevLeafCount =
                this.element.prevLeafs
                    .takeWhile { prev -> prev.text != "%" }
                    .toList()
                    .size
            listOf(
                "if",
                "elseif",
                "else",
                "end",
                "expand",
                "populate",
                "for",
                "!",
            ).filter {
                it.startsWith(bind)
            }.map {
                LookupElementBuilder
                    .create(it)
                    .withInsertHandler { context, _ ->
                        val start = context.startOffset - prevLeafCount
                        val tail = context.tailOffset
                        context.document.replaceString(start, tail, it)
                        context.editor.caretModel.moveToOffset(start + it.length)
                    }.withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
            }
        }

    private fun percentDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
        processor: (String) -> List<LookupElement>,
    ): Boolean {
        if (BindDirectiveUtil.getDirectiveType(element) == DirectiveType.PERCENT) {
            val prefix = getBindSearchWord(element, "%")
            result.withPrefixMatcher(PlainPrefixMatcher(prefix)).apply {
                val candidates = processor(prefix)
                result.addAllElements(candidates)
            }
            return true
        }
        return false
    }
}
