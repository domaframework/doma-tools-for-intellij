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
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf
import com.intellij.util.ProcessingContext
import org.domaframework.doma.intellij.psi.SqlCustomElExpr
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<out PsiReference?> {
        if (element !is SqlCustomElExpr) return PsiReference.EMPTY_ARRAY

        return if (element is SqlElClass) {
            arrayOf(SqlElClassExprReference(element))
        } else if (element is SqlElIdExpr) {
            when {
                getParentClassPsiType(element, SqlElFunctionCallExpr::class.java) != null ->
                    arrayOf(SqlElFunctionCallExprReference(element))

                getParentClassPsiType(element, SqlElClass::class.java) != null ->
                    arrayOf(
                        SqlElClassExprReference(element),
                    )

                getParentClassPsiType(
                    element,
                    SqlElStaticFieldAccessExpr::class.java,
                ) != null -> arrayOf(SqlElStaticFieldReference(element))

                getParentClassPsiType(element, SqlElForDirective::class.java) != null &&
                    element.prevLeaf()?.prevLeaf()?.elementType == SqlTypes.EL_FOR ->
                    arrayOf(
                        SqlElForDirectiveIdExprReference(element),
                    )

                else -> arrayOf(SqlElIdExprReference(element))
            }
        } else {
            PsiReference.EMPTY_ARRAY
        }
    }

    private fun <R : PsiElement> getParentClassPsiType(
        element: PsiElement,
        parentClass: Class<R>,
    ): R? = PsiTreeUtil.getParentOfType(element, parentClass)
}
