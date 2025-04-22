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
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective

class SqlElForDirectiveIdExprReference(
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    private val cachedResolve: CachedValue<PsiElement?> by lazy {
        CachedValuesManager.getManager(element.project).createCachedValue {
            val result = doResolve()
            CachedValueProvider.Result(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    val file: PsiFile? = element.containingFile

    override fun resolve(): PsiElement? = cachedResolve.value

    private fun doResolve(): PsiElement? {
        if (file == null || !isSupportFileType(file)) return null
        val startTime = System.nanoTime()
        return superResolveLogic(startTime)
    }

    private fun superResolveLogic(startTime: Long): PsiElement? {
        val declarationItem = getDeclarationItem()
        PluginLoggerUtil.countLogging(
            this::class.java.simpleName,
            "ReferenceForDirectiveItem",
            "Reference",
            startTime,
        )
        return declarationItem
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun getDeclarationItem(): PsiElement? {
        val forDirectiveParent = PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java)
        if (forDirectiveParent == null) return null

        return PsiTreeUtil.getChildrenOfType(forDirectiveParent, SqlElFieldAccessExpr::class.java)?.last()
            ?: PsiTreeUtil.getChildrenOfType(forDirectiveParent, SqlElFieldAccessExpr::class.java)?.last()
    }
}
