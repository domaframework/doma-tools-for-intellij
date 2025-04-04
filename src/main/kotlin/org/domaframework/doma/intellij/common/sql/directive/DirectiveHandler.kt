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

import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.jetbrains.kotlin.psi.psiUtil.prevLeaf

/**
 * Determine directive elements and perform code completion
 */
open class DirectiveHandler(
    private val originalFile: PsiElement,
) {
    open fun directiveHandle(): Boolean = false

    fun isDirective(
        it: PsiElement,
        symbol: String,
    ): Boolean {
        val prev = it.prevLeaf()
        return (
            prev?.text == symbol ||
                it.text.startsWith(symbol)
        )
    }

    protected fun getBindSearchWord(
        element: PsiElement,
        symbol: String,
    ): String = PsiPatternUtil.getBindSearchWord(originalFile, element, symbol)
}
