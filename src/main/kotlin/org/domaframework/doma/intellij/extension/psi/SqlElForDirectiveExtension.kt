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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.sql.foritem.ForDeclarationItem
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlTypes

fun SqlElForDirective.getForItem(): PsiElement? =
    PsiTreeUtil
        .getChildOfType(this, SqlElIdExpr::class.java)

fun SqlElForDirective.getForItemDeclaration(): ForDeclarationItem? {
    val parentCommentBlock =
        PsiTreeUtil.getParentOfType(this, SqlBlockComment::class.java)
            ?: return null
    val childLeafs = parentCommentBlock.childLeafs()
    val start = childLeafs.firstOrNull { it.elementType == SqlTypes.SEPARATOR } ?: return null
    val end = childLeafs.last()

    val rightItems =
        childLeafs
            .filter {
                it.textOffset > start.textOffset &&
                    it.textOffset < end.textOffset &&
                    it !is PsiWhiteSpace &&
                    it !is PsiErrorElement
            }.toList()
    if (rightItems.isEmpty()) {
        return null
    }
    val declarationElm =
        PsiTreeUtil.getChildrenOfType(this, SqlElFieldAccessExpr::class.java)?.last()
            ?: PsiTreeUtil.getChildrenOfType(this, SqlElIdExpr::class.java)?.last()
    return declarationElm?.let { ForDeclarationItem(it) }
}
