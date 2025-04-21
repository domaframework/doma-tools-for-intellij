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
package org.domaframework.doma.intellij.contributor.sql

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.domaframework.doma.intellij.contributor.sql.provider.SqlParameterCompletionProvider
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.jetbrains.kotlin.idea.completion.or

/**
 * Code completion of SQL bind variables with Dao method arguments
 * Valid only for SQL files or @Sql in Dao
 */
open class SqlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PsiPatternUtil
                .createPattern(PsiComment::class.java)
                .andOr(
                    PsiPatternUtil
                        .createPattern(PsiComment::class.java)
                        .inFile(
                            PlatformPatterns
                                .psiFile()
                                .withLanguage(SqlLanguage.INSTANCE),
                        ),
                    PsiPatternUtil
                        .createPattern(PsiComment::class.java)
                        .and(PsiPatternUtil.isMatchFileExtension("java")),
                )
                // Support for directive elements
                .or(
                    PlatformPatterns
                        .psiElement(PsiElement::class.java)
                        .andOr(
                            PsiPatternUtil
                                .createDirectivePattern()
                                .inFile(
                                    PlatformPatterns
                                        .psiFile()
                                        .withName(StandardPatterns.string().endsWith(".sql")),
                                ),
                            PsiPatternUtil
                                .createDirectivePattern()
                                .and(PsiPatternUtil.isMatchFileExtension("java")),
                        ),
                ),
            SqlParameterCompletionProvider(),
        )
    }
}
