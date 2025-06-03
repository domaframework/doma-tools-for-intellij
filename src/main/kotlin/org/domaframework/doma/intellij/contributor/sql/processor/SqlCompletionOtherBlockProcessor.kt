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
    /**
     * Generate a list of related elements for suggesting regular DAO argument parameters
     * and instance properties of field access elements.
     *
     * @param targetElement Element at the caret position.
     * @return List of preceding elements related to the caret position.
     */
    override fun generateBlock(targetElement: PsiElement): List<PsiElement> {
        // When entering a new bind variable, an empty list is returned and element names defined as DAO argument parameters
        // or in loop directives are suggested.
        if (targetElement is PsiWhiteSpace &&
            targetElement.text.length > 1 &&
            PsiTreeUtil.prevLeaf(targetElement, true)?.elementType != SqlTypes.DOT
        ) {
            return emptyList()
        }

        // For SqlElFieldAccessExpr,this is the list from the top element to the element at the caret position.
        val prevElms = findSelfBlocks(targetElement)
        if (prevElms.isNotEmpty()) {
            return filterBlocks(prevElms)
        }
        return emptyList()
    }
}
