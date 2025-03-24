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
package org.domaframework.doma.intellij.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.setting.SqlLanguage

@Suppress("ktlint:standard:no-consecutive-comments")
class SqlFormattingModelBuilder : FormattingModelBuilder {
    val breakLineSpacing: Spacing? = Spacing.createSpacing(0, 0, 1, false, 0)
    val mergeSpacing: Spacing? = Spacing.createSpacing(0, 0, 0, false, 0)
    val normalSpacing: Spacing? = Spacing.createSpacing(1, 1, 0, false, 0)

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val codeStyleSettings = formattingContext.codeStyleSettings
        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                formattingContext.containingFile,
                SqlBlock(
                    formattingContext.node,
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    createCustomSpacingBuilder(),
                    createSpaceBuilder(codeStyleSettings),
                ),
                codeStyleSettings,
            )
    }

    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder = SpacingBuilder(settings, SqlLanguage.INSTANCE)

    private fun createCustomSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.BLOCK_COMMENT,
                SqlTypes.BLOCK_COMMENT,
                breakLineSpacing,
            ).withSpacing(
                SqlTypes.WORD,
                SqlTypes.BLOCK_COMMENT,
                breakLineSpacing,
            ).withSpacing(
                SqlTypes.OTHER,
                SqlTypes.BLOCK_COMMENT,
                breakLineSpacing,
            ).withSpacing(
                SqlTypes.OTHER,
                SqlTypes.LINE_COMMENT,
                breakLineSpacing,
            )
            // Table And Column Rules
            .withSpacing(
                SqlTypes.DOT,
                SqlTypes.WORD,
                mergeSpacing,
            ).withSpacing(
                SqlTypes.DOT,
                SqlTypes.OTHER,
                mergeSpacing,
            ).withSpacing(
                SqlTypes.DOT,
                SqlTypes.ASTERISK,
                mergeSpacing,
            ).withSpacing(
                SqlTypes.WORD,
                SqlTypes.DOT,
                mergeSpacing,
            )
            // WORD And OTHER Rules
            .withSpacing(
                SqlTypes.WORD,
                SqlTypes.WORD,
                normalSpacing,
            ).withSpacing(
                SqlTypes.OTHER,
                SqlTypes.OTHER,
                normalSpacing,
            )
            // COMMA Rules
            .withSpacing(
                SqlTypes.WORD,
                SqlTypes.COMMA,
                breakLineSpacing,
            ).withSpacing(
                SqlTypes.OTHER,
                SqlTypes.COMMA,
                breakLineSpacing,
            ).withSpacing(
                SqlTypes.ASTERISK,
                SqlTypes.COMMA,
                breakLineSpacing,
            ).withSpacing(
                SqlTypes.COMMA,
                SqlTypes.WORD,
                normalSpacing,
            ).withSpacing(
                SqlTypes.COMMA,
                SqlTypes.OTHER,
                normalSpacing,
            )
}
