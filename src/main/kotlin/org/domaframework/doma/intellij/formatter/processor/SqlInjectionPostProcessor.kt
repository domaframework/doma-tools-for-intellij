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
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.formatter.visitor.DaoInjectionSqlVisitor

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
        if (!shouldProcessFile(source)) {
            return rangeToReformat
        }

        val manager = InjectedLanguageManager.getInstance(source.project)
        if (manager.isInjectedFragment(source)) {
            processInjectedFragment(source, manager)
        } else {
            processRegularFile(source)
        }

        return rangeToReformat
    }

    private fun shouldProcessFile(source: PsiFile): Boolean {
        val manager = InjectedLanguageManager.getInstance(source.project)
        val isInjectedSql = if (isSupportFileType(source)) manager.isInjectedFragment(source) else false
        val isDaoFile = isJavaOrKotlinFileType(source) && getDaoClass(source) != null

        return isInjectedSql || isDaoFile
    }

    private fun processInjectedFragment(
        source: PsiFile,
        manager: InjectedLanguageManager,
    ) {
        val host = manager.getInjectionHost(source) as? PsiLiteralExpression ?: return
        val hostDaoFile = host.containingFile
        val originalText = host.value?.toString() ?: return

        val visitor = DaoInjectionSqlVisitor(hostDaoFile, source.project)
        val formattingTask = DaoInjectionSqlVisitor.FormattingTask(host, originalText)

        visitor.replaceHostStringLiteral(formattingTask) { text ->
            processDocumentText(text)
        }
    }

    private fun processRegularFile(source: PsiFile) {
        val visitor = DaoInjectionSqlVisitor(source, source.project)
        source.accept(visitor)

        visitor.processAll { text ->
            processDocumentText(text)
        }
    }
}
