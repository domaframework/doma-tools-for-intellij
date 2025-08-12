package org.domaframework.doma.intellij.formatter.processor

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.codeStyle.CodeStyleManager
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.formatter.visitor.FormattingTask

class InjectionSqlFormatter(
    private val project: Project,
) {
    companion object {
        private const val TEMP_FILE_PREFIX = "temp_format"
        private const val SQL_FILE_EXTENSION = ".sql"
        private const val TRIPLE_QUOTE = "\"\"\""
        private const val WRITE_COMMAND_NAME = "Format Injected SQL"
        private const val BASE_INDENT = "\t\t\t"
        private val COMMENT_START_REGEX = Regex("^[ \t]*/[*][ \t]*\\*")
    }

    private val injectionManager by lazy { InjectedLanguageManager.getInstance(project) }
    private val documentManager by lazy { PsiDocumentManager.getInstance(project) }
    private val codeStyleManager by lazy { CodeStyleManager.getInstance(project) }
    private val fileTypeManager by lazy { FileTypeManager.getInstance() }
    private val elementFactory by lazy {
        com.intellij.psi.JavaPsiFacade
            .getElementFactory(project)
    }

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

        // format(task.isOriginalTextBlock, task.formattedText, injectionFile, removeSpace)
        val formattedText =
            if (!task.isOriginalTextBlock) {
                val result =
                    SqlFormatPreProcessor().updateDocument(injectionFile, injectionFile.textRange)
                result.document?.text ?: return
            } else {
                task.formattedText
            }
        replaceHostStringLiteral(FormattingTask(task.expression, formattedText, task.isOriginalTextBlock), removeSpace)
    }

    fun format(
        sqlFile: PsiFile,
        removeSpace: (String) -> String,
    ) {
        val result = SqlFormatPreProcessor().updateDocument(sqlFile, sqlFile.textRange)
        val formattedText = result.document?.text ?: return

        val tmpFormatted = formatAsTemporarySqlFile(formattedText)
        val document = documentManager.getDocument(sqlFile) ?: return
        document.replaceString(
            sqlFile.textRange.startOffset,
            sqlFile.textRange.endOffset,
            tmpFormatted,
        )
        documentManager.commitDocument(document)
        removeSpace(document.text)
    }

    private fun removeIndentLines(sqlText: String): String =
        sqlText.lines().joinToString(StringUtil.LINE_SEPARATE) { line ->
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
        val normalizedText = normalizeIndentation(literalText)

        val newLiteral = elementFactory.createExpressionFromText(normalizedText, task.expression)
        return newLiteral.text
    }

    /**
     * Creates a Java text block (triple-quoted string) from formatted SQL.
     */
    private fun createFormattedLiteralText(formattedSqlText: String): String =
        buildString {
            append(TRIPLE_QUOTE)
            append(StringUtil.LINE_SEPARATE)
            append(formattedSqlText)
            append(TRIPLE_QUOTE)
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
                        BASE_INDENT +
                            line.removePrefix(
                                BASE_INDENT,
                            )
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

    private fun convertToTextBlock(content: String): String = "\"\"\"\n${content.replace("\"\"\"", "\\\"\\\"\\\"")}${TRIPLE_QUOTE}"
}
