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
package org.domaframework.doma.intellij.inspection.dao.inspector

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.isCollector
import org.domaframework.doma.intellij.extension.psi.isFunctionClazz
import org.domaframework.doma.intellij.extension.psi.isSelectOption
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Check if Dao method arguments are used in the corresponding SQL file
 */
class DaoMethodVariableInspector : AbstractBaseJavaLocalInspectionTool() {
    private data class DaoMethodVariableVisitorResult(
        val elements: List<PsiParameter>,
        val deplicateForItemElements: List<PsiParameter>,
    )

    override fun getDisplayName(): String = "Method argument usage check"

    override fun getShortName(): String = "org.domaframework.doma.intellij.variablechecker"

    override fun getGroupDisplayName(): String = "DomaTools"

    override fun isEnabledByDefault(): Boolean = true

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                super.visitMethod(method)
                val file = method.containingFile
                if (!isJavaOrKotlinFileType(file) || getDaoClass(file) == null) return

                val psiDaoMethod = PsiDaoMethod(method.project, method)
                if (!psiDaoMethod.useSqlAnnotation() && !psiDaoMethod.isUseSqlFileMethod()) return

                val methodParameters =
                    method.methodParameters
                        .filter { !it.isFunctionClazz && !it.isSelectOption && !(it.isCollector && psiDaoMethod.isSelectTypeCollect()) }
                val sqlFileManager =
                    psiDaoMethod.sqlFile?.let {
                        method.project.findFile(it)
                    } ?: return

                val params = methodParameters.toList()
                val result = findElementsInSqlFile(sqlFileManager, params)
                params.forEach { param ->
                    if (!result.elements.contains(param)) {
                        val message =
                            if (result.deplicateForItemElements.contains(param)) {
                                MessageBundle.message("inspection.invalid.dao.duplicate")
                            } else {
                                MessageBundle.message(
                                    "inspection.invalid.dao.paramUse",
                                    param.name,
                                )
                            }
                        holder.registerProblem(
                            (param.originalElement as PsiParameterImpl).nameIdentifier,
                            message,
                            ProblemHighlightType.ERROR,
                        )
                    }
                }
            }
        }
    }

    private fun findElementsInSqlFile(
        sqlFile: PsiFile,
        args: List<PsiParameter>,
    ): DaoMethodVariableVisitorResult {
        val elements = mutableListOf<PsiParameter>()
        val deplicateForItemElements = mutableListOf<PsiParameter>()
        var iterator: Iterator<PsiParameter>
        sqlFile.accept(
            object : PsiRecursiveElementVisitor() {
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
                                val elementParent = PsiTreeUtil.getParentOfType(element, SqlElForDirective::class.java)
                                val isForItemSide = elementParent?.getForItem()?.textOffset == element.textOffset

                                // Check if the element name definition source is in the for directive
                                val forDirectiveBlocks =
                                    ForDirectiveUtil.getForDirectiveBlocks(element)
                                val forItem = ForDirectiveUtil.findForItem(element, forDirectives = forDirectiveBlocks)

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
            },
        )
        val result = DaoMethodVariableVisitorResult(elements, deplicateForItemElements)
        return result
    }
}
