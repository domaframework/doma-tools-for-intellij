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

import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.validator.SqlElStaticFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

/**
 * In the For directive, define the element based on the Dao parameter.
 * %for [ForItem] : [ForDeclarationStaticFieldAccessorItem]
 *   %for [ForItem] : [ForDeclarationItem]
 *    ...
 */
class ForDeclarationStaticFieldAccessorItem(
    private val blocks: List<SqlElIdExpr>,
    private val staticFieldAccessExpr: SqlElStaticFieldAccessExpr,
    override val index: Int = 0,
    private val shortName: String = "",
) : ForDeclarationDaoBaseItem(blocks, null, index) {
    /***
     * Obtain the type information of the For item defined from the Dao parameters.
     */
    override fun getPsiParentClass(): PsiParentClass? {
        val staticFieldValidator =
            SqlElStaticFieldAccessorChildElementValidator(
                blocks,
                staticFieldAccessExpr,
                shortName = shortName,
            )

        var lastPsiParentClass: PsiParentClass? = null
        staticFieldValidator.validateChildren(
            complete = { parent ->
                lastPsiParentClass = parent
            },
        )
        if (lastPsiParentClass == null) return null

        if (blocks.size == 1) {
            val searchElementText = cleanString(blocks.first().text)
            val field = lastPsiParentClass.findField(searchElementText)
            if (field != null) {
                val fieldType = field.type
                return PsiParentClass(fieldType)
            }

            val method = lastPsiParentClass.findMethod(searchElementText)
            if (method != null) {
                val methodReturnType = method.returnType ?: return null
                return PsiParentClass(methodReturnType)
            }
        }
        return lastPsiParentClass
    }
}
