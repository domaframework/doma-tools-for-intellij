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
package org.domaframework.doma.intellij.common.sql.directive

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.VariableLookupItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.extension.psi.searchParameter

class LiteralDirectiveHandler(
    private val originalFile: PsiFile,
    private val element: PsiElement,
    private val result: CompletionResultSet,
) : DirectiveHandler(originalFile) {
    private val symbol = "^"

    override fun directiveHandle(): Boolean =
        directiveHandler(
            element,
            originalFile,
            result,
        ) { daoMethod, bind ->
            daoMethod
                ?.searchParameter(bind)
                ?.filter {
                    PsiTypeChecker.isBaseClassType(it.type)
                }?.map { param -> VariableLookupItem(param) }
                ?.toList()
                ?: emptyList()
        }

    private fun directiveHandler(
        it: PsiElement,
        originalFile: PsiFile,
        result: CompletionResultSet,
        processor: (PsiMethod?, String) -> List<VariableLookupItem>,
    ): Boolean {
        if (isDirective(it, symbol)) {
            val daoMethod = findDaoMethod(originalFile)
            val prefix = getBindSearchWord(element, symbol)
            val candidates = processor(daoMethod, prefix)
            result.addAllElements(candidates)
            return true
        }
        return false
    }
}
