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
package org.domaframework.doma.intellij.common.sql.foritem

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiParameter
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.validator.SqlElFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.psi.SqlElIdExpr

/**
 * In the For directive, define the element based on the Dao parameter.
 * %for [ForItem] : [ForDeclarationDaoBaseItem]
 *   %for [ForItem] : [ForDeclarationItem]
 *    ...
 */
class ForDeclarationDaoBaseItem(
    private val blocks: List<SqlElIdExpr>,
    val nestIndex: Int,
    val domaAnnotationType: DomaAnnotationType,
    val daoParameter: PsiParameter? = null,
) : ForDeclarationItem(blocks.first()) {
    fun getPsiParentClass(): PsiParentClass? {
        val daoParamFieldValidator =
            SqlElFieldAccessorChildElementValidator(
                blocks,
                element.containingFile,
                "",
                daoParameter,
            )

        var lastType: PsiParentClass? = null
        val errorElement =
            daoParamFieldValidator.validateChildren(
                complete = { parent ->
                    lastType = parent
                },
            )
        if (errorElement != null || lastType == null) return null

        val topElm = blocks.first()
        (lastType.type as? PsiClassType)?.let { if (!PsiClassTypeUtil.isIterableType(it, topElm.project)) return null }

        var nestClassType: PsiClassType? = (lastType.type as? PsiClassType)
        var i = 0
        if (domaAnnotationType.isBatchAnnotation()) {
            nestClassType?.parameters?.firstOrNull() as? PsiClassType?
        }
        while (nestClassType != null && i <= nestIndex && PsiClassTypeUtil.isIterableType(nestClassType, topElm.project)) {
            nestClassType = nestClassType.parameters.firstOrNull() as? PsiClassType?
            i++
        }
        return nestClassType?.let { PsiParentClass(it) }
    }
}
