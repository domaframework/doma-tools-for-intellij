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

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

val SqlElStaticFieldAccessExpr.accessElements: List<SqlElIdExpr>
    get() =
        this.elIdExprList
            .sortedBy { it.textOffset }
            .toList()

val SqlElFieldAccessExpr.accessElements: List<SqlElIdExpr?>
    get() =
        this.elExprList
            .mapNotNull { it as SqlElIdExpr }
            .sortedBy { it.textOffset }
            .toList()

fun SqlElFieldAccessExpr.accessElementsPrevOriginalElement(targetTextOffset: Int): List<SqlElIdExpr> =
    this.accessElements.filter { it != null && it.textOffset <= targetTextOffset }.mapNotNull { it }

fun SqlCustomElCommentExpr.isConditionOrLoopDirective(): Boolean =
    PsiTreeUtil.getChildOfType(this, SqlElIfDirective::class.java) != null ||
        PsiTreeUtil.getChildOfType(this, SqlElForDirective::class.java) != null ||
        PsiTreeUtil.getChildOfType(
            this,
            SqlElElseifDirective::class.java,
        ) != null ||
        this.findElementAt(2)?.elementType == SqlTypes.EL_END ||
        this.findElementAt(2)?.elementType == SqlTypes.EL_ELSE
