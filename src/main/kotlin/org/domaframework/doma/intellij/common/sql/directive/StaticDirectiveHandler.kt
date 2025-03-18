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
package org.domaframework.doma.intellij.common.sql.directive

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.VariableLookupItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class StaticDirectiveHandler(
    private val originalFile: PsiElement,
    private val element: PsiElement,
    private val result: CompletionResultSet,
    private val bindText: String,
    private val project: Project,
) : DirectiveHandler(originalFile) {
    /**
     * Function information displayed with code completion for built-in functions
     */
    data class DomaFunction(
        val name: String,
        val returnType: PsiType,
        val parameters: List<PsiType>,
    )

    /**
     * Show parameters in code completion for fields and methods
     */
    data class CompletionSuggest(
        val field: List<VariableLookupItem>,
        val methods: List<LookupElement>,
    )

    override fun directiveHandle(): Boolean {
        var handleResult = false
        if (element.prevSibling is SqlElStaticFieldAccessExpr) {
            handleResult =
                staticDirectiveHandler(element, result) { fqdn, bind ->
                    val psiStaticElement = PsiStaticElement(fqdn, originalFile.containingFile)
                    val javaClass =
                        psiStaticElement.getRefClazz() ?: return@staticDirectiveHandler null
                    val parentClazz = PsiParentClass(javaClass.psiClassType)
                    parentClazz.let { clazz ->
                        val fields =
                            clazz.searchStaticField(bind)?.map { f -> VariableLookupItem(f) }
                        val methods =
                            clazz.searchStaticMethod(bind)?.map { m ->
                                LookupElementBuilder
                                    .create("${m.name}()")
                                    .withPresentableText(m.name)
                                    .withTailText(m.parameterList.text, true)
                                    .withTypeText(m.returnType?.presentableText ?: "")
                            }
                        CompletionSuggest(fields ?: emptyList(), methods ?: emptyList())
                    }
                }
        } else if (element.prevSibling?.elementType == SqlTypes.AT_SIGN) {
            // Built-in function completion
            handleResult =
                builtInDirectiveHandler(element, result) { bind ->
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
                }
        }
        return handleResult
    }

    private fun staticDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
        processor: (String, String) -> CompletionSuggest?,
    ): Boolean {
        val clazzRef =
            PsiTreeUtil
                .getChildOfType(element.prevSibling, SqlElClass::class.java) // getStaticFieldAccessClazzRef(element) ?: return false
        val fqdn =
            PsiTreeUtil.getChildrenOfTypeAsList(clazzRef, PsiElement::class.java).joinToString("") { it.text }
        val candidates = processor(fqdn, bindText) ?: return false
        result.addAllElements(candidates.field)
        candidates.methods.map { m -> result.addElement(m) }
        return true
    }

    private fun builtInDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
        processor: (String) -> List<LookupElement>?,
    ): Boolean {
        if (BindDirectiveUtil.getDirectiveType(element) == DirectiveType.BUILT_IN) {
            val prefix = getBindSearchWord(element, "@")
            val candidates = processor(prefix)
            candidates?.let { it1 -> result.addAllElements(it1) }
            return true
        }
        return false
    }

    private fun getJavaLangString(): PsiType =
        PsiType.getJavaLangString(
            PsiManager.getInstance(project),
            GlobalSearchScope.allScope(project),
        )

    private fun getPsiTypeByClassName(className: String): PsiType =
        PsiType.getTypeByName(className, project, GlobalSearchScope.allScope(project))
}
