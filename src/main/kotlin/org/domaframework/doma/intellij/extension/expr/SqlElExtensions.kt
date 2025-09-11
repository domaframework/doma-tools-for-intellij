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

import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

fun SqlCustomElCommentExpr.isConditionOrLoopDirective(): Boolean =
    PsiTreeUtil.getChildOfType(this, SqlElIfDirective::class.java) != null ||
        PsiTreeUtil.getChildOfType(this, SqlElForDirective::class.java) != null ||
        PsiTreeUtil.getChildOfType(
            this,
            SqlElElseifDirective::class.java,
        ) != null ||
        this.findElementAt(2)?.elementType == SqlTypes.EL_END ||
        this.findElementAt(2)?.elementType == SqlTypes.EL_ELSE

fun SqlElParameters.extractParameterTypes(psiManager: PsiManager): List<PsiType?> =
    this.elExprList.mapNotNull { param ->
        when (param) {
            is SqlElStaticFieldAccessExpr -> param.extractStaticFieldType()
            is SqlElFieldAccessExpr -> param.extractFieldType()
            is SqlElIdExpr -> {
                val daoMethod = findDaoMethod(this.containingFile)
                daoMethod
                    ?.parameterList
                    ?.parameters
                    ?.find { it.name == param.text }
                    ?.type
            }
            is SqlElPrimaryExpr -> {
                when (param.node.firstChildNode.elementType) {
                    SqlTypes.EL_NUMBER -> PsiTypes.intType()
                    SqlTypes.EL_STRING -> {
                        PsiType.getJavaLangString(psiManager, GlobalSearchScope.allScope(param.project))
                    }
                    SqlTypes.BOOLEAN -> PsiTypes.booleanType()
                    else -> null
                }
            }
            is SqlElFunctionCallExpr -> {
                param.extractFunctionReturnType()
            }

            else -> null
        }
    }
