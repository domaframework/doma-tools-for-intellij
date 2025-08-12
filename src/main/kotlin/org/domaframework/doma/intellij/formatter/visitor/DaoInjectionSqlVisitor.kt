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
package org.domaframework.doma.intellij.formatter.visitor

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiLiteralExpression
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.formatter.processor.InjectionSqlFormatter

/**
 * Visitor for processing and formatting SQL injections in DAO files.
 * Formats SQL strings embedded in Java/Kotlin string literals while preserving indentation.
 */
class DaoInjectionSqlVisitor(
    private val project: Project,
) : JavaRecursiveElementVisitor() {
    companion object {
        private const val WRITE_COMMAND_NAME = "Format Injected SQL"
    }

    private val injectionSqlFormatter = InjectionSqlFormatter(project)
    private val formattingTasks = mutableListOf<FormattingTask>()

    override fun visitLiteralExpression(expression: PsiLiteralExpression) {
        super.visitLiteralExpression(expression)
        if ((expression.parent.parent.parent as? PsiAnnotation)?.qualifiedName != DomaAnnotationType.Sql.fqdn) return
        expression.value?.toString()?.let { originalText ->
            val existExpression =
                formattingTasks.find { it.expression == expression && !it.isOriginalTextBlock }
            val isTextBlock = existExpression?.isOriginalTextBlock ?: expression.isTextBlock
            if (existExpression != null) {
                formattingTasks.remove(existExpression)
            }
            formattingTasks.add(FormattingTask(expression, originalText, isTextBlock))
        }
    }

    fun processAllTextBlock() {
        if (formattingTasks.isEmpty()) return
        injectionSqlFormatter.processAllTextBlock(formattingTasks)
    }

    /**
     * Processes all collected formatting tasks in a single write action.
     * @param removeSpace Function to remove trailing spaces from formatted text
     */
    fun processAllReFormat(removeSpace: (String) -> String) {
        if (formattingTasks.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(project, WRITE_COMMAND_NAME, null, {
            formattingTasks
                .sortedByDescending { it.expression.textRange.startOffset }
                .forEach { task ->
                    if (task.expression.isValid) {
                        injectionSqlFormatter.processFormattingTask(task, removeSpace)
                    }
                }
        })
    }
}
