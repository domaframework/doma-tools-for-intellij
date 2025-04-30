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
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr

class SqlElForDirectiveIdExprReference(
    element: PsiElement,
) : SqlElExprReference(element) {
    override fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val declarationItem = getDeclarationItem()
        if (declarationItem != null) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceForDirectiveItem",
                "Reference",
                startTime,
            )
        }
        return declarationItem
    }

    /**
     * In the for directive, set the reference on the left side to the element on the right side.
     */
    private fun getDeclarationItem(): PsiElement? {
        val forDirectiveParent =
            PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java) ?: return null

        return PsiTreeUtil
            .getChildrenOfType(forDirectiveParent, SqlElFieldAccessExpr::class.java)
            ?.last()
            ?: PsiTreeUtil
                .getChildrenOfType(forDirectiveParent, SqlElIdExpr::class.java)
                ?.last()
    }
}
