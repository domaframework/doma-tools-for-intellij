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
    )

    companion object {
        private const val TEMP_FILE_PREFIX = "temp_format"
        private const val SQL_FILE_EXTENSION = ".sql"
        private const val TRIPLE_QUOTE = "\"\"\""
        private const val WRITE_COMMAND_NAME = "Format Injected SQL"
        private const val BASE_INDENT = "\t\t\t"
        private val COMMENT_START_REGEX = Regex("^[ \t]*/[*][ \t]*\\*")
        private val COMMENT_END_REGEX = Regex("\\*/.*$")
    }

    private val formattingTasks = mutableListOf<FormattingTask>()

    override fun visitLiteralExpression(expression: PsiLiteralExpression) {
        super.visitLiteralExpression(expression)
        val injected: PsiFile? = InjectionSqlUtil.initInjectionElement(element, project, expression)
        if (injected != null) {
            // Format SQL and store the task
            val originalText = expression.value?.toString() ?: return
            val removeIndent = removeIndentLines(originalText)
            formattingTasks.add(FormattingTask(expression, removeIndent))
        }
    }

    private fun removeIndentLines(sqlText: String): String {
        val lines = sqlText.lines()

        var blockComment = false
        val removeIndentLines =
            lines.map { line ->
                if (blockComment) {
                    if (COMMENT_END_REGEX.containsMatchIn(line)) {
                        blockComment = false
                    }
                    "$SINGLE_SPACE${line.dropWhile { it.isWhitespace() }}"
                } else {
                    val baseLine =
                        if (COMMENT_START_REGEX.containsMatchIn(line)) {
                            blockComment = true
                            // Exclude spaces between `/*` and the comment content element,
                            // as IntelliJ IDEA's Java formatter may insert a space there during formatting.
                            line.replace(COMMENT_START_REGEX, "/**")
                        } else {
                            line
                        }
                    baseLine.dropWhile { it.isWhitespace() }
                }
            }

        return removeIndentLines.joinToString(StringUtil.LINE_SEPARATE)
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
        sqlPostProcessorProcess: (String, Boolean) -> String,
    ) {
        try {
            // Keep the current top line indent
            val formattedLiteral = createFormattedLiteral(task, sqlPostProcessorProcess)
            replaceInDocument(task.expression, formattedLiteral)
        } catch (_: Exception) {
            // Silently ignore formatting failures
        }
    }

    private fun createFormattedLiteral(
        task: FormattingTask,
        sqlPostProcessorProcess: (String, Boolean) -> String,
    ): String {
        // Retrieve the same formatted string as when formatting a regular SQL file.
        val formattedSql = formatAsTemporarySqlFile(task.formattedText)
        val cleanedText = sqlPostProcessorProcess(formattedSql, false)
        // Generate text aligned with the literal element using the formatted string.
        val newLiteralText = createFormattedLiteralText(cleanedText)
        val normalizedText = normalizeIndentation(newLiteralText)

        val elementFactory =
            com.intellij.psi.JavaPsiFacade
                .getElementFactory(project)
        val newLiteral = elementFactory.createExpressionFromText(normalizedText, task.expression)
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
    private fun normalizeIndentation(sqlText: String): String {
        val lines = sqlText.lines()
        if (lines.isEmpty()) return sqlText

        val literalSeparator = lines.first()
        val contentLines = lines.drop(1)

        val normalizedLines =
            contentLines.map { line ->
                when {
                    line.isBlank() -> line
                    line.startsWith(BASE_INDENT) -> BASE_INDENT + line.removePrefix(BASE_INDENT)
                    else -> BASE_INDENT + line
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
