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
package org.domaframework.doma.intellij.formatter.builder

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.domaframework.doma.intellij.formatter.block.SqlFileBlock
import org.domaframework.doma.intellij.psi.SqlTypes
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.domaframework.doma.intellij.setting.state.DomaToolsFormatEnableSettings

/***
 * Because the code formatter (which runs only when explicitly invoked by the user) is enabled by default,
 * the block-generation routine for formatting should always run.
 * Automatic indentation on Line breaks, however, is executed only when the corresponding configuration flag is set.
 */
class SqlFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val codeStyleSettings = formattingContext.codeStyleSettings
        val setting = DomaToolsFormatEnableSettings.getInstance()
        val isEnableFormat = setting.state.isEnableSqlFormat == true
        val formatMode = formattingContext.formattingMode
        val spacingBuilder = createSpaceBuilder(codeStyleSettings)
        val customSpacingBuilder = createCustomSpacingBuilder()

        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                formattingContext.containingFile,
                SqlFileBlock(
                    formattingContext.node,
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    customSpacingBuilder,
                    spacingBuilder,
                    isEnableFormat,
                    formatMode,
                ),
                codeStyleSettings,
            )
    }

    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder =
        SpacingBuilder(settings, SqlLanguage.INSTANCE)
            .around(SqlTypes.DOT)
            .spacing(0, 0, 0, false, 0)
            .after(SqlTypes.COMMA)
            .spacing(1, 1, 0, false, 0)
            .before(SqlTypes.LINE_COMMENT)
            .spacing(1, 1, 0, false, 0)
            .before(SqlTypes.BLOCK_COMMENT)
            .spacing(0, 0, 1, false, 0)
            .before(SqlTypes.KEYWORD)
            .spacing(1, 1, 0, false, 0)
            .before(SqlTypes.LEFT_PAREN)
            .spacing(1, 1, 0, false, 0)
            .after(SqlTypes.LEFT_PAREN)
            .spacing(0, 0, 0, false, 0)
            .around(SqlTypes.WORD)
            .spacing(1, 1, 0, false, 0)
            .around(SqlTypes.NUMBER)
            .spacing(1, 1, 0, false, 0)
            .before(SqlTypes.RIGHT_PAREN)
            .spacing(0, 0, 0, false, 0)
            .after(SqlTypes.RIGHT_PAREN)
            .spacing(1, 1, 0, false, 0)
            .around(SqlTypes.PLUS)
            .spacing(1, 1, 0, false, 0)
            .around(SqlTypes.MINUS)
            .spacing(1, 1, 0, false, 0)
            .around(SqlTypes.ASTERISK)
            .spacing(1, 1, 0, false, 0)
            .around(SqlTypes.SLASH)
            .spacing(1, 1, 0, false, 0)
            .before(SqlTypes.FUNCTION_NAME)
            .spacing(1, 1, 0, false, 0)

    private fun createCustomSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.FUNCTION_NAME,
                SqlTypes.LEFT_PAREN,
                SqlCustomSpacingBuilder.nonSpacing,
            ).withSpacing(
                SqlTypes.FUNCTION_NAME,
                SqlTypes.OTHER,
                SqlCustomSpacingBuilder.normalSpacing,
            ).withSpacing(
                SqlTypes.NUMBER,
                SqlTypes.COMMA,
                SqlCustomSpacingBuilder.nonSpacing,
            ).withSpacing(
                SqlTypes.STRING,
                SqlTypes.COMMA,
                SqlCustomSpacingBuilder.nonSpacing,
            ).withSpacing(
                SqlTypes.WORD,
                SqlTypes.LEFT_PAREN,
                SqlCustomSpacingBuilder.nonSpacing,
            ).withSpacing(
                SqlTypes.LEFT_PAREN,
                SqlTypes.WORD,
                SqlCustomSpacingBuilder.nonSpacing,
            ).withSpacing(
                SqlTypes.WORD,
                SqlTypes.RIGHT_PAREN,
                SqlCustomSpacingBuilder.nonSpacing,
            ).withSpacing(
                SqlTypes.ASTERISK,
                TokenType.WHITE_SPACE,
                SqlCustomSpacingBuilder.nonSpacing,
            )
            // Table And Column Rules
            // WORD And OTHER Rules
            .withSpacing(
                SqlTypes.WORD,
                SqlTypes.WORD,
                SqlCustomSpacingBuilder.normalSpacing,
            ).withSpacing(
                SqlTypes.OTHER,
                SqlTypes.OTHER,
                SqlCustomSpacingBuilder.normalSpacing,
            ).withSpacing(
                SqlTypes.OTHER,
                SqlTypes.WORD,
                SqlCustomSpacingBuilder.normalSpacing,
            )
}
