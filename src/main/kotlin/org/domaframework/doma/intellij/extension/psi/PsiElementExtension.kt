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
package org.domaframework.doma.intellij.extension.psi

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import org.domaframework.doma.intellij.psi.SqlTypes

fun PsiElement.isNotWhiteSpace(): Boolean = this !is PsiWhiteSpace

// Get the list of elements before itself
fun PsiElement.findSelfBlocks(): List<PsiElement> {
    var elms = emptyList<PsiElement>()
    for (it in this.prevLeafs) {
        elms = elms.plus(it)
        if (!it.isNotWhiteSpace() || it.elementType == SqlTypes.AT_SIGN) break
    }

    elms
        .filter {
            it.elementType == SqlTypes.EL_PRIMARY_EXPR ||
                it.elementType == SqlTypes.EL_IDENTIFIER
        }.toList()
        .plus(this)
        .also { elms = it }
    if (elms.isNotEmpty()) {
        return elms.sortedBy { it.textOffset }
    }
    return emptyList()
}

/**
 * Traverse a node's parent hierarchy
 * to find the parent of the specified element type up to the comment block element
 */
fun PsiElement.findNodeParent(elementType: IElementType): PsiElement? {
    var parentElm = this
    while (parentElm.elementType != elementType &&
        parentElm.elementType != SqlTypes.BLOCK_COMMENT &&
        parentElm !is PsiFile &&
        parentElm !is PsiDirectory
    ) {
        parentElm = parentElm.parent
        if (parentElm.elementType == elementType) {
            return parentElm
        }
    }
    return null
}
