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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiParameter
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz

/**
 * In the For directive, define the element based on the Dao parameter.
 * %for [ForItem] : [ForDeclarationDaoBaseItem]
 *   %for [ForItem] : [ForDeclarationItem]
 *    ...
 */
class ForDeclarationDaoBaseItem(
    override val element: PsiElement,
    val daoParameter: PsiParameter,
    val nestIndex: Int,
    val domaAnnotationType: DomaAnnotationType,
) : ForDeclarationItem(element) {
    fun getPsiParentClass(): PsiParentClass? {
        var i = 0
        var nestClassType: PsiClassType? =
            (daoParameter.getIterableClazz(domaAnnotationType.isBatchAnnotation()).type as PsiClassType)
        if (domaAnnotationType.isBatchAnnotation()) {
            nestClassType?.parameters?.firstOrNull() as PsiClassType?
        }

        while (i <= nestIndex) {
            nestClassType = nestClassType?.parameters?.firstOrNull() as PsiClassType?
            i++
        }
        return nestClassType?.let { PsiParentClass(it) }
    }
}
