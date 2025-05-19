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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class DirectiveCompletion(
    private val originalFile: PsiFile,
    private val bindText: String,
    private val element: PsiElement,
    private val result: CompletionResultSet,
) {
    fun directiveHandle(symbol: String): Boolean {
        return when (symbol) {
            "%" ->
                PercentDirectiveHandler(
                    originalFile = originalFile,
                    element = element,
                    result = result,
                ).directiveHandle()

            "#" ->
                EmbeddedDirectiveHandler(
                    originalFile = originalFile,
                    element = element,
                    result = result,
                ).directiveHandle()

            "^" ->
                LiteralDirectiveHandler(
                    originalFile = originalFile,
                    element = element,
                    result = result,
                ).directiveHandle()

            "@" ->
                StaticDirectiveHandler(
                    originalFile = originalFile,
                    element = element,
                    result = result,
                    bindText = bindText,
                ).directiveHandle()

            else -> return false
        }
    }
}
