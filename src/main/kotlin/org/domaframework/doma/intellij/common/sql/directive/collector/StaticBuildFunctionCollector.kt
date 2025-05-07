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
package org.domaframework.doma.intellij.common.sql.directive.collector

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import org.domaframework.doma.intellij.common.sql.directive.DomaFunction

class StaticBuildFunctionCollector(
    private val project: Project,
    private val bind: String,
) : StaticDirectiveHandlerCollector() {
    public override fun collect(): List<LookupElement>? =
        listOf(
            DomaFunction(
                "escape",
                getJavaLangString(),
                listOf(
                    getPsiTypeByClassName("java.lang.CharSequence"),
                    getPsiTypeByClassName("java.lang.Char"),
                ),
            ),
            DomaFunction(
                "prefix",
                getJavaLangString(),
                listOf(
                    getPsiTypeByClassName("java.lang.CharSequence"),
                    getPsiTypeByClassName("java.lang.Char"),
                ),
            ),
            DomaFunction(
                "infix",
                getJavaLangString(),
                listOf(
                    getPsiTypeByClassName("java.lang.CharSequence"),
                    getPsiTypeByClassName("java.lang.Char"),
                ),
            ),
            DomaFunction(
                "suffix",
                getJavaLangString(),
                listOf(
                    getPsiTypeByClassName("java.lang.CharSequence"),
                    getPsiTypeByClassName("java.lang.Char"),
                ),
            ),
            DomaFunction(
                "roundDownTimePart",
                getPsiTypeByClassName("java.util.Date"),
                listOf(getPsiTypeByClassName("java.util.Date")),
            ),
            DomaFunction(
                "roundDownTimePart",
                getPsiTypeByClassName("java.sql.Date"),
                listOf(getPsiTypeByClassName("java.util.Date")),
            ),
            DomaFunction(
                "roundDownTimePart",
                getPsiTypeByClassName("java.sql.Timestamp"),
                listOf(getPsiTypeByClassName("java.sql.Timestamp")),
            ),
            DomaFunction(
                "roundDownTimePart",
                getPsiTypeByClassName("java.time.LocalDateTime"),
                listOf(getPsiTypeByClassName("java.time.LocalDateTime")),
            ),
            DomaFunction(
                "roundUpTimePart",
                getPsiTypeByClassName("java.util.Date"),
                listOf(getPsiTypeByClassName("java.sql.Date")),
            ),
            DomaFunction(
                "roundUpTimePart",
                getPsiTypeByClassName("java.sql.Timestamp"),
                listOf(getPsiTypeByClassName("java.sql.Timestamp")),
            ),
            DomaFunction(
                "roundUpTimePart",
                getPsiTypeByClassName("java.time.LocalDate"),
                listOf(getPsiTypeByClassName("java.time.LocalDate")),
            ),
            DomaFunction(
                "isEmpty",
                getPsiTypeByClassName("boolean"),
                listOf(getPsiTypeByClassName("java.lang.CharSequence")),
            ),
            DomaFunction(
                "isNotEmpty",
                getPsiTypeByClassName("boolean"),
                listOf(getPsiTypeByClassName("java.lang.CharSequence")),
            ),
            DomaFunction(
                "isBlank",
                getPsiTypeByClassName("boolean"),
                listOf(getPsiTypeByClassName("java.lang.CharSequence")),
            ),
            DomaFunction(
                "isNotBlank",
                getPsiTypeByClassName("boolean"),
                listOf(getPsiTypeByClassName("java.lang.CharSequence")),
            ),
        ).filter {
            it.name.startsWith(bind.substringAfter("@"))
        }.map {
            LookupElementBuilder
                .create("${it.name}()")
                .withPresentableText(it.name)
                .withTailText(
                    "(${
                        it.parameters.joinToString(",") { param ->
                            param.toString().replace("PsiType:", "")
                        }
                    })",
                    true,
                ).withTypeText(it.returnType.presentableText)
        }

    private fun getJavaLangString(): PsiType =
        PsiType.getJavaLangString(
            PsiManager.getInstance(project),
            GlobalSearchScope.allScope(project),
        )

    private fun getPsiTypeByClassName(className: String): PsiType =
        PsiType.getTypeByName(className, project, GlobalSearchScope.allScope(project))
}
