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
package org.domaframework.doma.intellij.extension.expr

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

val SqlElStaticFieldAccessExpr.accessElements: List<PsiElement>
    get() {
        return PsiTreeUtil
            .getChildrenOfType(this, PsiElement::class.java)
            ?.filter {
                (
                    it.elementType == SqlTypes.EL_IDENTIFIER ||
                        it is SqlElPrimaryExpr
                )
            }?.sortedBy { it.textOffset }
            ?.toList()
            ?: emptyList()
    }

val SqlElStaticFieldAccessExpr.fqdn: String
    get() {
        val elClazz = PsiTreeUtil.getChildOfType(this, SqlElClass::class.java) ?: return ""
        val fqdn = PsiTreeUtil.getChildrenOfTypeAsList(elClazz, PsiElement::class.java)
        return fqdn.toList().joinToString("") { it.text }
    }
