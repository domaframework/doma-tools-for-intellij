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

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil
import org.domaframework.doma.intellij.formatter.visitor.DaoInjectionSqlVisitor
import org.domaframework.doma.intellij.formatter.visitor.FormattingTask

/**
 * Post-processor for SQL injection formatting.
 *
 * This processor handles SQL formatting in two contexts:
 * 1. **File formatting**: When formatting entire DAO files (Java/Kotlin) containing SQL annotations
 * 2. **Code formatting**: When formatting injected SQL fragments within string literals
 *
 * The context is determined by checking:
 * - If the source is an injected fragment (`InjectedLanguageManager.isInjectedFragment()` returns true),
 *   it's being called from code formatting for a specific SQL string literal
 * - If the source is a regular DAO file (Java/Kotlin with @Dao annotation),
 *   it's being called from file formatting to process all SQL strings in the file
 */
class SqlInjectionPostProcessor : SqlPostProcessor() {
    override fun processElement(
        element: PsiElement,
        settings: CodeStyleSettings,
    ): PsiElement = element

    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings,
    ): TextRange {
        if (!isProcessFile(source)) {
            return rangeToReformat
        }

        val manager = InjectedLanguageManager.getInstance(source.project)
        if (manager.isInjectedFragment(source)) {
            processInjectedFragment(source)
        } else {
            processRegularFile(source)
        }

        return rangeToReformat
    }

    private fun isProcessFile(source: PsiFile): Boolean {
        val manager = InjectedLanguageManager.getInstance(source.project)
        val isInjectedSql = if (isSupportFileType(source)) manager.isInjectedFragment(source) else false
        val isDaoFile = isJavaOrKotlinFileType(source) && getDaoClass(source) != null

        return isInjectedSql || isDaoFile
    }

    /**
     * Processes all SQL injections in a DAO file during file formatting.
     * This is called when formatting an entire DAO file containing SQL annotations.
     */
    private fun processInjectedFragment(source: PsiFile) {
        val host = InjectionSqlUtil.getLiteralExpressionHost(source) ?: return
        val originalText = host.value?.toString() ?: return

        val injectionFormatter = InjectionSqlFormatter(source.project)
        val formattingTask = FormattingTask(host, originalText, host.isTextBlock)

        injectionFormatter.convertExpressionToTextBlock(formattingTask.expression)
        injectionFormatter.processFormattingTask(formattingTask) { text ->
            processDocumentText(text)
        }
    }

    /**
     * Processes injected SQL fragments during code formatting.
     * This is called when formatting a specific SQL string literal within a DAO file.
     */
    private fun processRegularFile(source: PsiFile) {
        val visitor = DaoInjectionSqlVisitor(source.project)
        source.accept(visitor)
        visitor.processAllTextBlock()
        source.accept(visitor)
        visitor.processAllReFormat { text ->
            processDocumentText(text)
        }
    }
}
