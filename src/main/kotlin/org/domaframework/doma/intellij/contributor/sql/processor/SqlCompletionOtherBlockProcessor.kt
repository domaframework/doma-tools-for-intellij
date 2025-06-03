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
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlCompletionOtherBlockProcessor : SqlCompletionBlockProcessor() {
    override fun generateBlock(targetElement: PsiElement): List<PsiElement> {
        if (targetElement is PsiWhiteSpace &&
            targetElement.text.length > 1 &&
            PsiTreeUtil.prevLeaf(targetElement, true)?.elementType != SqlTypes.DOT
        ) {
            return emptyList()
        }

        val prevElms = findSelfBlocks(targetElement)
        if (prevElms.isNotEmpty()) {
            return filterBlocks(prevElms)
        }
        return emptyList()
    }
}
