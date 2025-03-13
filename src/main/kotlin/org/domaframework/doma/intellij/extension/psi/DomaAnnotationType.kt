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
package org.domaframework.doma.intellij.extension.psi

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiModifierListOwner

enum class DomaAnnotationType(
    val fqdn: String,
    val extension: String = "sql",
) {
    SqlProcessor("org.seasar.doma.SqlProcessor"),
    Script("org.seasar.doma.Script", extension = "script"),
    BatchInsert("org.seasar.doma.BatchInsert"),
    BatchUpdate("org.seasar.doma.BatchUpdate"),
    BatchDelete("org.seasar.doma.BatchDelete"),
    Select("org.seasar.doma.Select"),
    Insert("org.seasar.doma.Insert"),
    Update("org.seasar.doma.Update"),
    Delete("org.seasar.doma.Delete"),
    Sql("org.seasar.doma.Sql"),
    Unknown("Unknown"),
    ;

    fun isBatchAnnotation(): Boolean = this == BatchInsert || this == BatchUpdate || this == BatchDelete

    fun isRequireSqlTemplate(): Boolean = this == Select || this == Script || this == SqlProcessor

    private fun useSqlFileOption(): Boolean =
        this == Insert ||
            this == Update ||
            this == Delete ||
            this == BatchInsert ||
            this == BatchUpdate ||
            this == BatchDelete

    fun getPsiAnnotation(element: PsiModifierListOwner): PsiAnnotation? =
        AnnotationUtil.findAnnotation(
            element,
            this.fqdn,
        )

    fun getSqlFileVal(element: PsiAnnotation): Boolean =
        when (this.useSqlFileOption()) {
            true -> AnnotationUtil.getBooleanAttributeValue(element, "sqlFile") == true
            false -> false
        }
}
