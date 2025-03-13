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
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.psi.isFunctionClazz
import org.domaframework.doma.intellij.extension.psi.isSelectOption
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr

/**
 * Check if Dao method arguments are used in the corresponding SQL file
 */
class DaoMethodVariableInspector : AbstractBaseJavaLocalInspectionTool() {
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
                        .filter { !it.isFunctionClazz && !it.isSelectOption }
                val sqlFileManager =
                    psiDaoMethod.sqlFile?.let {
                        method.project.findFile(it)
                    } ?: return

                findElementsInSqlFile(sqlFileManager, methodParameters.toList()).forEach { arg ->
                    holder.registerProblem(
                        (arg.originalElement as PsiParameterImpl).nameIdentifier,
                        MessageBundle.message("inspection.dao.method.variable.error", arg.name),
                        ProblemHighlightType.ERROR,
                    )
                }
            }
        }
    }

    fun findElementsInSqlFile(
        sqlFile: PsiFile,
        args: List<PsiParameter>,
    ): List<PsiParameter> {
        val elements = mutableListOf<PsiParameter>()
        var iterator: Iterator<PsiParameter>
        sqlFile.accept(
            object : PsiRecursiveElementVisitor() {
                // Recursively explore child elements in a file with PsiRecursiveElementVisitor.
                override fun visitElement(element: PsiElement) {
                    if (element is SqlElPrimaryExpr) {
                        iterator = args.minus(elements.toSet()).iterator()
                        while (iterator.hasNext()) {
                            val arg = iterator.next()
                            if (element.text == arg.name) {
                                elements.add(arg)
                                break
                            }
                        }
                    }
                    super.visitElement(element)
                }
            },
        )
        return args.minus(elements.toSet())
    }
}
