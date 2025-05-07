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
package org.domaframework.doma.intellij.inspection.sql.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.nextLeafs
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationTestDataResult
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlTypes
import org.toml.lang.psi.ext.elementType

class SqlTestDataAfterBlockCommentVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : SqlVisitorBase() {
    override fun visitElement(element: PsiElement) {
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return
        if (isJavaOrKotlinFileType(visitFile) && element is PsiLiteralExpression) {
            val injectionFile = initInjectionElement(visitFile, element.project, element) ?: return
            injectionFile.accept(this)
            super.visitElement(element)
        }
        if (isInjectionSqlFile(visitFile)) {
            element.acceptChildren(this)
        }
    }

    override fun visitBlockComment(element: SqlBlockComment) {
        super.visitBlockComment(element)
        if (hasOtherBindVariable(element)) return

        val nextElement = element.nextSibling ?: return
        if (isSqlLiteral(nextElement)) return
        if (isMatchListTestData(element)) return

        val result = ValidationTestDataResult(element, shortName)
        result.highlightElement(holder)
    }

    /**
     * Check to Exist other bind variable in the block comment
     */
    private fun hasOtherBindVariable(element: PsiElement): Boolean {
        val directive =
            PsiTreeUtil.getChildOfType(element, SqlElForDirective::class.java)
                ?: PsiTreeUtil.getChildOfType(element, SqlElIfDirective::class.java)
                ?: PsiTreeUtil.getChildOfType(element, SqlElElseifDirective::class.java)
        val otherDirective =
            PsiTreeUtil
                .getChildrenOfType(element, PsiElement::class.java)
                ?.find {
                    it.elementType == SqlTypes.EL_END ||
                        it.elementType == SqlTypes.HASH ||
                        it.elementType == SqlTypes.EL_POPULATE ||
                        it.elementType == SqlTypes.EL_ELSE
                }
        if (directive != null || otherDirective != null) return true

        val content = PsiTreeUtil.getChildOfType(element, PsiComment::class.java)
        return content != null
    }

    private fun isSqlLiteral(element: PsiElement): Boolean =
        element.elementType == SqlTypes.STRING ||
            listOf("true", "false", "null").contains(element.text) ||
            element.text.matches(Regex("^\\d+$"))

    private fun isMatchListTestData(element: PsiElement): Boolean {
        val parenthesesListPattern =
            Regex(
                """^\(\s*(?:(?:"[^"]*"|'[^']*'|\d+|true|false|null)\s*(?:,\s*(?:"[^"]*"|'[^']*'|\d+|true|false|null)\s*)*)?\)$""",
            )
        val testDataText =
            element.nextLeafs
                .takeWhile { it !is PsiWhiteSpace }
                .toList()
                .joinToString("") { it.text }
        return testDataText.matches(parenthesesListPattern)
    }
}
