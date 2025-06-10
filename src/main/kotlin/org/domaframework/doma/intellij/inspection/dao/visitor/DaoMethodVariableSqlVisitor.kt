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
package org.domaframework.doma.intellij.inspection.dao.visitor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class DaoMethodVariableSqlVisitor(
    private val args: List<PsiParameter>,
    private val elements: MutableList<PsiParameter>,
    private val deplicateForItemElements: MutableList<PsiParameter>,
) : PsiRecursiveElementVisitor() {
    private var iterator: Iterator<PsiParameter>

    init {
        iterator = args.minus(elements.toSet()).iterator()
    }

    // Recursively explore child elements in a file with PsiRecursiveElementVisitor.
    override fun visitElement(element: PsiElement) {
        if ((
                element.elementType == SqlTypes.EL_IDENTIFIER ||
                    element is SqlElPrimaryExpr
            ) &&
            element.prevSibling?.elementType != SqlTypes.DOT
        ) {
            iterator = args.minus(elements.toSet()).iterator()
            while (iterator.hasNext()) {
                val arg = iterator.next()
                if (element.text == arg.name) {
                    // Check if you are in a For directive
                    val elementParent =
                        PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java)
                    val isForItemSide =
                        elementParent?.getForItem()?.textOffset == element.textOffset

                    // Check if the element name definition source is in the for directive
                    val forDirectiveBlocks =
                        ForDirectiveUtil.getForDirectiveBlocks(element)
                    val forItem =
                        ForDirectiveUtil.findForItem(element, forDirectives = forDirectiveBlocks)

                    if (forItem != null || isForItemSide) {
                        deplicateForItemElements.add(arg)
                    } else {
                        elements.add(arg)
                    }
                    break
                }
            }
        }
        super.visitElement(element)
    }
}
