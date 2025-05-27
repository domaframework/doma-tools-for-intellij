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
package org.domaframework.doma.intellij.common.sql.directive.collector

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.VariableLookupItem
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.directive.CompletionSuggest
import org.domaframework.doma.intellij.common.util.SqlCompletionUtil.createMethodLookupElement
import org.domaframework.doma.intellij.extension.psi.psiClassType

class StaticPropertyCollector(
    private val element: PsiElement,
    private val caretNextText: String,
    private val bind: String,
) : StaticDirectiveHandlerCollector() {
    public override fun collectCompletionSuggest(fqdn: String): CompletionSuggest? {
        val psiStaticElement = PsiStaticElement(fqdn, element.containingFile)
        val javaClass =
            psiStaticElement.getRefClazz() ?: return null
        val parentClazz = PsiParentClass(javaClass.psiClassType)
        parentClazz.let { clazz ->
            val fields =
                clazz.searchStaticField(bind)?.map { f -> VariableLookupItem(f) }
            val methods =
                clazz.searchStaticMethod(bind)?.map { m ->
                    LookupElementBuilder
                        .create(createMethodLookupElement(caretNextText, m))
                        .withPresentableText(m.name)
                        .withTailText(m.parameterList.text, true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withTypeText(m.returnType?.presentableText ?: "")
                }
            return CompletionSuggest(fields ?: emptyList(), methods ?: emptyList())
        }
    }
}
