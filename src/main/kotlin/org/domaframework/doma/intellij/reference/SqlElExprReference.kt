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
package org.domaframework.doma.intellij.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.psi.SqlElIdExpr

abstract class SqlElExprReference(
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    var psiClassType: PsiType? = null

    protected val cachedResolve: CachedValue<PsiElement?> by lazy {
        CachedValuesManager.getManager(element.project).createCachedValue {
            val result = doResolve()
            CachedValueProvider.Result(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    val file: PsiFile? =
        element.containingFile
            ?: element.containingFile.originalFile

    override fun resolve(): PsiElement? = cachedResolve.value

    open fun doResolve(): PsiElement? {
        if (file == null || !isSupportFileType(file)) return null
        val startTime = System.nanoTime()
        return superResolveLogic(startTime, file)
    }

    abstract fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement?

    protected fun <R : PsiElement> getBlockCommentElements(
        element: PsiElement,
        base: Class<R>,
    ): List<PsiElement> {
        val fieldAccessExpr = PsiTreeUtil.findFirstParent(element) { it !is SqlElIdExpr }
        if (fieldAccessExpr != null && !base.isInstance(fieldAccessExpr)) return listOf(element)

        val nodeElm =
            PsiTreeUtil
                .getChildrenOfType(
                    fieldAccessExpr,
                    SqlElIdExpr::class.java,
                )?.filter { it.textOffset <= element.textOffset }

        return nodeElm
            ?.toList()
            ?.sortedBy { it.textOffset } ?: emptyList()
    }
}
