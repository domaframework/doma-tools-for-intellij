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
package org.domaframework.doma.intellij.inspection.sql.processor

import com.intellij.codeInspection.ProblemsHolder
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationForDirectiveItemTypeResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.psi.SqlElForDirective

class InspectionForDirectiveVisitorProcessor(
    val shortName: String,
    private val element: SqlElForDirective,
) : InspectionVisitorProcessor(shortName) {
    fun check(holder: ProblemsHolder) {
        val forItem = element.getForItem() ?: return
        val directiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(forItem, false)
        val declarationType =
            ForDirectiveUtil.getForDirectiveItemClassType(element.project, directiveBlocks)

        if (declarationType == null) {
            ValidationForDirectiveItemTypeResult(
                forItem,
                this.shortName,
            ).highlightElement(holder)
        }
    }
}
