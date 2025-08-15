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

import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.inspection.sql.processor.InspectionStaticFieldAccessVisitorProcessor
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

val SqlElStaticFieldAccessExpr.accessElements: List<SqlElIdExpr>
    get() =
        this.elIdExprList
            .sortedBy { it.textOffset }
            .toList()

fun SqlElStaticFieldAccessExpr.extractStaticFieldType(): PsiType? {
    val processor = InspectionStaticFieldAccessVisitorProcessor(shortName = "")
    return processor.getStaticFieldAccessLastPropertyClassType(this)
}
