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
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.codeStyle.CodeStyleManager
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.common.util.StringUtil.SINGLE_SPACE
import kotlin.text.isBlank

/**
 * Visitor for processing and formatting SQL injections in DAO files.
 * Formats SQL strings embedded in Java/Kotlin string literals while preserving indentation.
 */
class DaoInjectionSqlVisitor(
    private val element: PsiFile,
    private val project: Project,
) : JavaRecursiveElementVisitor() {
    private data class FormattingTask(
        val expression: PsiLiteralExpression,
        val formattedText: String,
        val baseIndent: String,
    )

    companion object {
        private const val TEMP_FILE_PREFIX = "temp_format"
        private const val SQL_FILE_EXTENSION = ".sql"
        private const val SQL_COMMENT_PATTERN = "^[ \\t]*/\\*"
        private const val TRIPLE_QUOTE = "\"\"\""
        private const val WRITE_COMMAND_NAME = "Format Injected SQL"
    }

    private val formattingTasks = mutableListOf<FormattingTask>()

    override fun visitLiteralExpression(expression: PsiLiteralExpression) {
        super.visitLiteralExpression(expression)
        val injected: PsiFile? = InjectionSqlUtil.initInjectionElement(element, project, expression)
        if (injected != null) {
            // Format SQL and store the task
            val originalSqlText = injected.text
            val formattedSql = formatAsTemporarySqlFile(originalSqlText)
            // Keep the current top line indent
            val baseIndent = getBaseIndent(formattedSql)
            val originalText = expression.value?.toString() ?: return

            if (formattedSql != originalText) {
                formattingTasks.add(FormattingTask(expression, formattedSql, baseIndent))
            }
        }
    }

    /**
     * Extracts the base indentation from the first non-blank, non-comment line.
     */
    private fun getBaseIndent(string: String): String {
        val lines = string.lines()
        val commentRegex = Regex(SQL_COMMENT_PATTERN)

        // Skip blank lines and comment lines
        val firstContentLineIndex =
            lines.indexOfFirst { line ->
                line.isNotBlank() && !commentRegex.matches(line)
            }

        return if (firstContentLineIndex >= 0) {
            lines[firstContentLineIndex].takeWhile { it.isWhitespace() }
        } else {
            ""
        }
    }

    /**
     * Processes all collected formatting tasks in a single write action.
     * @param removeSpace Function to remove trailing spaces from formatted text
     */
    fun processAll(removeSpace: (String, Boolean) -> String) {
        if (formattingTasks.isEmpty()) return

        // Apply all formatting tasks in a single write action
        WriteCommandAction.runWriteCommandAction(project, WRITE_COMMAND_NAME, null, {
            // Sort by text range in descending order to maintain offsets
            formattingTasks
                .sortedByDescending { it.expression.textRange.startOffset }
                .forEach { task ->
                    if (task.expression.isValid) {
                        replaceHostStringLiteral(task, removeSpace)
                    }
                }
        })
    }

    /**
     * Formats SQL text by creating a temporary SQL file and applying code style.
     * Returns original text if formatting fails.
     */
    private fun formatAsTemporarySqlFile(sqlText: String): String =
        try {
            val tempFileName = "${TEMP_FILE_PREFIX}${SQL_FILE_EXTENSION}"
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension("sql")

            val tempSqlFile =
                PsiFileFactory
                    .getInstance(project)
                    .createFileFromText(tempFileName, fileType, sqlText)

            CodeStyleManager
                .getInstance(project)
                .reformatText(tempSqlFile, 0, tempSqlFile.textLength)

            tempSqlFile.text
        } catch (_: Exception) {
            sqlText // Return original text on error
        }

    /**
     * Replaces the host Java string literal with formatted SQL text.
     */
    private fun replaceHostStringLiteral(
        task: FormattingTask,
        removeSpace: (String, Boolean) -> String,
    ) {
        try {
            val formattedLiteral = createFormattedLiteral(task, removeSpace)
            replaceInDocument(task.expression, formattedLiteral)
        } catch (_: Exception) {
            // Silently ignore formatting failures
        }
    }

    private fun createFormattedLiteral(
        task: FormattingTask,
        removeSpace: (String, Boolean) -> String,
    ): String {
        val newLiteralText = createFormattedLiteralText(task.formattedText)
        val normalizedText = normalizeIndentation(newLiteralText, task.baseIndent)
        val cleanedText = removeSpace(normalizedText, false)

        val elementFactory =
            com.intellij.psi.JavaPsiFacade
                .getElementFactory(project)
        val newLiteral = elementFactory.createExpressionFromText(cleanedText, task.expression)
        return newLiteral.text
    }

    private fun replaceInDocument(
        expression: PsiLiteralExpression,
        newText: String,
    ) {
        val document =
            PsiDocumentManager
                .getInstance(project)
                .getDocument(expression.containingFile) ?: return

        val range = expression.textRange
        document.replaceString(range.startOffset, range.endOffset, newText)
    }

    /**
     * Creates a Java text block (triple-quoted string) from formatted SQL.
     */
    private fun createFormattedLiteralText(formattedSqlText: String): String {
        val lines = formattedSqlText.split(StringUtil.LINE_SEPARATE)
        return buildString {
            append(TRIPLE_QUOTE)
            append(StringUtil.LINE_SEPARATE)
            append(lines.joinToString(StringUtil.LINE_SEPARATE))
            append(TRIPLE_QUOTE)
        }
    }

    /**
     * Normalizes indentation by removing base indent and reapplying it consistently.
     */
    private fun normalizeIndentation(
        sqlText: String,
        baseIndent: String,
    ): String {
        val lines = sqlText.lines()
        if (lines.isEmpty()) return sqlText

        val literalSeparator = lines.first()
        val contentLines = lines.drop(1)

        val normalizedLines =
            contentLines.map { line ->
                when {
                    line.isBlank() -> line
                    line.startsWith(baseIndent) -> baseIndent + line.removePrefix(baseIndent)
                    else -> baseIndent + line
                }
            }

        return buildString {
            append(literalSeparator)
            if (normalizedLines.isNotEmpty()) {
                append(StringUtil.LINE_SEPARATE)
                append(normalizedLines.joinToString(StringUtil.LINE_SEPARATE))
            }
        }
    }
}
