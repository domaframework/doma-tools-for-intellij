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
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.codeStyle.CodeStyleManager
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil
import org.domaframework.doma.intellij.common.util.StringUtil

class DaoInjectionSqlVisitor(
    private val element: PsiFile,
    private val project: Project,
) : JavaRecursiveElementVisitor() {
    private data class FormattingTask(
        val expression: PsiLiteralExpression,
        val formattedText: String,
    )

    companion object {
        private const val TEMP_FILE_PREFIX = "temp_format"
        private const val SQL_FILE_EXTENSION = ".sql"
    }

    private val formattingTasks = mutableListOf<FormattingTask>()

    override fun visitLiteralExpression(expression: PsiLiteralExpression) {
        super.visitLiteralExpression(expression)
        val injected: PsiFile? = InjectionSqlUtil.initInjectionElement(element, project, expression)
        if (injected != null) {
            // Format SQL and store the task
            val originalSqlText = injected.text
            val normalizedSql = normalizeIndentation(originalSqlText)
            val formattedSql = formatAsTemporarySqlFile(normalizedSql)
            val finalSql = reapplyOriginalIndentation(formattedSql, originalSqlText)

            val originalText = expression.value?.toString() ?: return
            if (finalSql != originalText) {
                formattingTasks.add(FormattingTask(expression, finalSql))
            }
        }
    }

    fun processAll(removeSpace: (String, Boolean) -> String) {
        if (formattingTasks.isEmpty()) return

        // Apply all formatting tasks in a single write action
        WriteCommandAction.runWriteCommandAction(project, "Format Injected SQL", null, {
            // Sort by text range in descending order to maintain offsets
            formattingTasks.sortedByDescending { it.expression.textRange.startOffset }.forEach { task ->
                if (task.expression.isValid) {
                    replaceHostStringLiteral(task.expression, task.formattedText, removeSpace)
                }
            }
        })
    }

    /**
     * Execute formatting as a temporary SQL file
     */
    private fun formatAsTemporarySqlFile(sqlText: String): String =
        try {
            val tempFileName = "${TEMP_FILE_PREFIX}${SQL_FILE_EXTENSION}"
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension("sql")

            val tempSqlFile =
                PsiFileFactory
                    .getInstance(project)
                    .createFileFromText(tempFileName, fileType, sqlText)

            val codeStyleManager = CodeStyleManager.getInstance(project)
            val textRange = TextRange(0, tempSqlFile.textLength)
            codeStyleManager.reformatText(tempSqlFile, textRange.startOffset, textRange.endOffset)

            tempSqlFile.text
        } catch (_: Exception) {
            sqlText
        }

    /**
     * Directly replace host Java string literal
     */
    private fun replaceHostStringLiteral(
        literalExpression: PsiLiteralExpression,
        formattedSqlText: String,
        removeSpace: (String, Boolean) -> String,
    ) {
        try {
            val normalizedSql = normalizeIndentation(formattedSqlText)
            val newLiteralText = createFormattedLiteralText(normalizedSql)
            val removeSpaceText = removeSpace(newLiteralText, false)

            // Replace PSI element
            val elementFactory =
                com.intellij.psi.JavaPsiFacade
                    .getElementFactory(project)
            val newLiteral = elementFactory.createExpressionFromText(removeSpaceText, literalExpression)
            val manager = PsiDocumentManager.getInstance(literalExpression.project)
            val document = manager.getDocument(literalExpression.containingFile) ?: return
            document.replaceString(literalExpression.textRange.startOffset, literalExpression.textRange.endOffset, newLiteral.text)
        } catch (_: Exception) {
            // Host literal replacement failed: ${e.message}
        }
    }

    private fun normalizeIndentation(sqlText: String): String {
        val lines = sqlText.lines()
        val minIndent =
            lines
                .filter { it.isNotBlank() }
                .minOfOrNull { it.indexOfFirst { char -> !char.isWhitespace() } } ?: 0

        return lines.joinToString(StringUtil.LINE_SEPARATE) { line ->
            if (line.isBlank()) line else line.drop(minIndent)
        }
    }

    /**
     * Create appropriate Java string literal from formatted SQL
     */
    private fun createFormattedLiteralText(formattedSqlText: String): String {
        val lines = formattedSqlText.split(StringUtil.LINE_SEPARATE)
        return "\"\"\"${StringUtil.LINE_SEPARATE}${lines.joinToString(StringUtil.LINE_SEPARATE)}\"\"\""
    }

    private fun reapplyOriginalIndentation(
        formattedSql: String,
        originalSql: String,
    ): String {
        val originalLines = originalSql.lines()
        val formattedLines = formattedSql.lines()

        val originalIndent =
            originalLines
                .firstOrNull { it.isNotBlank() }
                ?.takeWhile { it.isWhitespace() } ?: ""

        return formattedLines.joinToString(StringUtil.LINE_SEPARATE) { line ->
            if (line.isBlank()) line else originalIndent + line
        }
    }
}
