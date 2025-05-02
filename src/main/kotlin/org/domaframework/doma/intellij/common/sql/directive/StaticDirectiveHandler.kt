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
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.VariableLookupItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.extension.getContentRoot
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.jetbrains.kotlin.idea.util.getSourceRoot

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
        }
        if (handleResult) return true

        if (PsiTreeUtil.nextLeaf(element)?.elementType == SqlTypes.AT_SIGN ||
            element.elementType == SqlTypes.AT_SIGN
        ) {
            handleResult =
                staticClassPath(
                    result,
                ) { file, root ->
                    val rootChildren = root.children
                    if (PsiTreeUtil.prevLeaf(element)?.elementType == SqlTypes.AT_SIGN) {
                        return@staticClassPath rootChildren.map {
                            LookupElementBuilder
                                .create(it.name)
                                .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                        }
                    } else {
                        val prevPackageNames =
                            PsiPatternUtil.getBindSearchWord(file, element, "@").split(".")
                        val topPackage =
                            rootChildren.firstOrNull { it.name == prevPackageNames.firstOrNull() }
                                ?: return@staticClassPath null
                        var nextPackage: VirtualFile? =
                            topPackage
                        if (prevPackageNames.size > 2) {
                            for (packageName in prevPackageNames.drop(1).dropLast(1)) {
                                if (nextPackage == null) break
                                nextPackage =
                                    nextPackage.children.firstOrNull {
                                        it.name == cleanString(packageName)
                                    }
                            }
                        }
                        return@staticClassPath nextPackage
                            ?.children
                            ?.filter {
                                it.name.contains(cleanString(prevPackageNames.lastOrNull() ?: ""))
                            }?.map {
                                val packageName = prevPackageNames.joinToString(".").plus(it.nameWithoutExtension)
                                val suggestName =
                                    it.nameWithoutExtension
                                if (!isJavaOrKotlinFileType(it)) {
                                    suggestName.plus(".")
                                }
                                LookupElementBuilder
                                    .create(packageName)
                                    .withPresentableText(suggestName)
                                    .withTailText("($packageName)", true)
                                    .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                            }
                    }
                }
        }
        if (handleResult) return true

        if (element.prevSibling?.elementType == SqlTypes.AT_SIGN) {
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
                .getChildOfType(element.prevSibling, SqlElClass::class.java)
        val fqdn =
            PsiTreeUtil.getChildrenOfTypeAsList(clazzRef, PsiElement::class.java).joinToString("") { it.text }
        val candidates = processor(fqdn, bindText) ?: return false
        result.addAllElements(candidates.field)
        candidates.methods.map { m -> result.addElement(m) }
        return true
    }

    private fun staticClassPath(
        result: CompletionResultSet,
        processor: (PsiFile, VirtualFile) -> List<LookupElement>?,
    ): Boolean {
        val file = originalFile.containingFile ?: return false
        val virtualFile = file.virtualFile ?: return false
        val root =
            project
                .getContentRoot(virtualFile)
                ?.children
                ?.firstOrNull()
                ?.getSourceRoot(project)
                ?: return false
        val candidates = processor(file, root) ?: return false
        result.addAllElements(candidates)
        return true
    }

    private fun builtInDirectiveHandler(
        element: PsiElement,
        result: CompletionResultSet,
        processor: (String) -> List<LookupElement>?,
    ): Boolean {
        if (BindDirectiveUtil.getDirectiveType(element) == DirectiveType.BUILT_IN) {
            val prefix = getBindSearchWord(element, bindText)
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
