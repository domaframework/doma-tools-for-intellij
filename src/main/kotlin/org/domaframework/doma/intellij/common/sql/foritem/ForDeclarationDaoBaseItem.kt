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

import com.intellij.psi.PsiParameter
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.validator.SqlElFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.psi.SqlElIdExpr

/**
 * In the For directive, define the element based on the Dao parameter.
 * %for [ForItem] : [ForDeclarationDaoBaseItem]
 *   %for [ForItem] : [ForDeclarationItem]
 *    ...
 */
open class ForDeclarationDaoBaseItem(
    private val blocks: List<SqlElIdExpr>,
    val daoParameter: PsiParameter? = null,
    open val index: Int = 0,
) : ForDeclarationItem(blocks.first()) {
    /***
     * Obtain the type information of the For item defined from the Dao parameters.
     */
    open fun getPsiParentClass(): PsiParentClass? {
        val daoParamFieldValidator =
            SqlElFieldAccessorChildElementValidator(
                blocks,
                element.containingFile,
                topDaoParameter = daoParameter,
            )

        var lastType: PsiParentClass? = null
        daoParamFieldValidator.validateChildren(
            complete = { parent ->
                lastType = parent
            },
        )
        if (lastType == null) return null
        return lastType
    }
}
