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
package org.domaframework.doma.intellij.formatter.processor

import com.intellij.application.options.CodeStyle
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.formatter.visitor.FormattingTask

class InjectionSqlFormatter(
    private val project: Project,
) {
    companion object {
        private const val TEMP_FILE_PREFIX = "temp_format"
        private const val SQL_FILE_EXTENSION = ".sql"
        private const val WRITE_COMMAND_NAME = "Format Injected SQL"
        private val COMMENT_START_REGEX = Regex("^[ \t]*/[*][ \t]*\\*")

        fun createSpaceIndent(project: Project): String {
            val settings = CodeStyle.getSettings(project)
            val java = settings.getIndentOptions(JavaFileType.INSTANCE)
            val indentSize = java.INDENT_SIZE
            val prefixLen = "@Sql(${StringUtil.TRIPLE_QUOTE}".length
            return StringUtil.SINGLE_SPACE.repeat(indentSize.plus(prefixLen))
        }
    }

    private var baseIndent = createSpaceIndent(project)

    private val injectionManager by lazy { InjectedLanguageManager.getInstance(project) }
    private val documentManager by lazy { PsiDocumentManager.getInstance(project) }
    private val codeStyleManager by lazy { CodeStyleManager.getInstance(project) }
    private val fileTypeManager by lazy { FileTypeManager.getInstance() }
    private val elementFactory by lazy { JavaPsiFacade.getElementFactory(project) }

    fun processFormattingTask(
        task: FormattingTask,
        removeSpace: (String) -> String,
    ) {
        // Apply PreProcessor to single-line injection SQL
        val injectionFile =
            injectionManager
                .getInjectedPsiFiles(task.expression)
                ?.firstOrNull()
                ?.first as? PsiFile ?: return

        val result =
            SqlFormatPreProcessor().updateDocumentSafely(injectionFile, injectionFile.textRange)
        val formattedText = result.document?.text ?: return
        replaceHostStringLiteral(FormattingTask(task.expression, formattedText, task.isOriginalTextBlock), removeSpace)
    }

    fun format(
        sqlFile: PsiFile,
        removeSpace: (String) -> String,
    ) {
        val result = SqlFormatPreProcessor().updateDocumentSafely(sqlFile, sqlFile.textRange)
        val document = result.document ?: return
        val tmpFormatted = formatAsTemporarySqlFile(document)
        document.replaceString(
            sqlFile.textRange.startOffset,
            sqlFile.textRange.endOffset,
            tmpFormatted,
        )
        documentManager.commitDocument(document)
        removeSpace(document.text)
    }

    private fun removeIndentLines(sqlText: String): String =
        sqlText.lines().joinToString(StringUtil.LINE_SEPARATE.toString()) { line ->
            val processedLine =
                if (COMMENT_START_REGEX.containsMatchIn(line)) {
                    // Remove spaces between /* and comment content, as IntelliJ Java formatter may insert them
                    line.replace(COMMENT_START_REGEX, "/**")
                } else {
                    line
                }
            processedLine.dropWhile { it.isWhitespace() }
        }

    /**
     * Formats SQL text by creating a temporary SQL file and applying code style.
     * Returns original text if formatting fails.
     */
    private fun formatAsTemporarySqlFile(document: Document): String =
        runCatching {
            val tempFileName = "${TEMP_FILE_PREFIX}${SQL_FILE_EXTENSION}"
            val fileType = fileTypeManager.getFileTypeByExtension("sql")
            val tempSqlFile =
                PsiFileFactory
                    .getInstance(project)
                    .createFileFromText(tempFileName, fileType, document.text)

            document.setText(tempSqlFile.text)
            documentManager.commitDocument(document)
            val psiFile = documentManager.getPsiFile(document) ?: return@runCatching tempSqlFile.text

            codeStyleManager.reformatText(psiFile, 0, tempSqlFile.textLength)
            psiFile.text
        }.getOrDefault(document.text)

    private fun formatAsTemporarySqlFile(sqlText: String): String =
        runCatching {
            val tempFileName = "${TEMP_FILE_PREFIX}${SQL_FILE_EXTENSION}"
            val fileType = fileTypeManager.getFileTypeByExtension("sql")
            val tempSqlFile =
                PsiFileFactory
                    .getInstance(project)
                    .createFileFromText(tempFileName, fileType, sqlText)

            codeStyleManager.reformatText(tempSqlFile, 0, tempSqlFile.textLength)
            tempSqlFile.text
        }.getOrDefault(sqlText)

    /**
     * Replaces the host Java string literal with formatted SQL text.
     */
    private fun replaceHostStringLiteral(
        task: FormattingTask,
        sqlPostProcessorProcess: (String) -> String,
    ) {
        runCatching {
            val formattedLiteral = createFormattedLiteral(task, sqlPostProcessorProcess)
            replaceInDocument(task.expression, formattedLiteral)
        }
    }

    private fun createFormattedLiteral(
        task: FormattingTask,
        sqlPostProcessorProcess: (String) -> String,
    ): String {
        // Format SQL to match regular SQL file formatting
        val sqlWithoutIndent = removeIndentLines(task.formattedText)
        val formattedSql = formatAsTemporarySqlFile(sqlWithoutIndent)
        val processedSql = sqlPostProcessorProcess(formattedSql)

        // Create properly aligned literal text
        val literalText = createFormattedLiteralText(processedSql)
        updateBaseIndent(task)

        val normalizedText = normalizeIndentation(literalText)

        val newLiteral = elementFactory.createExpressionFromText(normalizedText, task.expression)
        return newLiteral.text
    }

    /**
     * When formatting styles other than Java code style change the indentation of a text block,
     * reset the base indent using the number of spaces before the PsiLiteralExpression.
     */
    private fun updateBaseIndent(task: FormattingTask) {
        val input = PsiTreeUtil.prevLeaf(task.expression)?.text
        val prevTexts = countCharsToLineBreak(task.expression).plus(StringUtil.TRIPLE_QUOTE.length)
        if (input != null) {
            val matches = StringUtil.SINGLE_SPACE.repeat(prevTexts)
            baseIndent = matches
        }
    }

    private fun countCharsToLineBreak(expression: PsiLiteralExpression): Int {
        var count = 0
        var leaf = PsiTreeUtil.prevLeaf(expression)
        while (leaf != null) {
            val text = leaf.text
            for (i in text.length - 1 downTo 0) {
                val c = text[i]
                if (c == '\n' || c == '\r') {
                    return count
                }
                count++
            }
            leaf = PsiTreeUtil.prevLeaf(leaf)
        }
        return count
    }

    /**
     * Creates a Java text block (triple-quoted string) from formatted SQL.
     */
    private fun createFormattedLiteralText(formattedSqlText: String): String =
        buildString {
            append(StringUtil.TRIPLE_QUOTE)
            append(StringUtil.LINE_SEPARATE)
            append(formattedSqlText)
            append(StringUtil.TRIPLE_QUOTE)
        }

    /**
     * Normalizes indentation by removing base indent and reapplying it consistently.
     */
    private fun normalizeIndentation(sqlText: String): String {
        val lines = sqlText.lines()
        if (lines.isEmpty()) return sqlText

        val (literalSeparator, contentLines) = lines.first() to lines.drop(1)
        val normalizedLines =
            contentLines.map { line ->
                when {
                    line.isBlank() -> line
                    else ->
                        baseIndent +
                            line
                }
            }

        return buildString {
            append(literalSeparator)
            if (normalizedLines.isNotEmpty()) {
                append(StringUtil.LINE_SEPARATE)
                append(normalizedLines.joinToString(StringUtil.LINE_SEPARATE.toString()))
            }
        }
    }

    private fun replaceInDocument(
        expression: PsiLiteralExpression,
        newText: String,
    ) {
        val document = documentManager.getDocument(expression.containingFile) ?: return
        val range = expression.textRange

        document.replaceString(range.startOffset, range.endOffset, newText)
        documentManager.commitDocument(document)
    }

    fun processAllTextBlock(formattingTasks: MutableList<FormattingTask>) {
        if (formattingTasks.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(
            project,
            WRITE_COMMAND_NAME,
            null,
            {
                // Convert PsiLiteralExpression to text blocks first
                formattingTasks
                    .sortedByDescending { it.expression.textRange.startOffset }
                    .forEach { task ->
                        convertExpressionToTextBlock(task.expression)
                    }
            },
        )
    }

    fun convertExpressionToTextBlock(expression: PsiLiteralExpression) {
        if (!expression.isValid || expression.isTextBlock) return

        val oldText = expression.value?.toString() ?: return
        val newText = convertToTextBlock(oldText)
        val document = documentManager.getDocument(expression.containingFile) ?: return

        val range = expression.textRange
        document.replaceString(range.startOffset, range.endOffset, newText)
        documentManager.commitDocument(document)
    }

    private fun convertToTextBlock(content: String): String =
        buildString {
            append(StringUtil.TRIPLE_QUOTE)
                .append(StringUtil.LINE_SEPARATE)
                .append(content.replace(StringUtil.TRIPLE_QUOTE, "\\\"\\\"\\\""))
                .append(StringUtil.TRIPLE_QUOTE)
        }
}
