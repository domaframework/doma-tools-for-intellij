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
package org.domaframework.doma.intellij.contributor.sql.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.VariableLookupItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import com.intellij.util.ProcessingContext
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.directive.DirectiveCompletion
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.common.util.SqlCompletionUtil.createMethodLookupElement
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.findNodeParent
import org.domaframework.doma.intellij.extension.psi.findSelfBlocks
import org.domaframework.doma.intellij.extension.psi.findStaticField
import org.domaframework.doma.intellij.extension.psi.findStaticMethod
import org.domaframework.doma.intellij.extension.psi.isNotWhiteSpace
import org.domaframework.doma.intellij.extension.psi.searchParameter
import org.domaframework.doma.intellij.extension.psi.searchStaticField
import org.domaframework.doma.intellij.extension.psi.searchStaticMethod
import org.domaframework.doma.intellij.psi.SqlElAndExpr
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElEqExpr
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElGeExpr
import org.domaframework.doma.intellij.psi.SqlElGtExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlElLeExpr
import org.domaframework.doma.intellij.psi.SqlElLtExpr
import org.domaframework.doma.intellij.psi.SqlElNeExpr
import org.domaframework.doma.intellij.psi.SqlElOrExpr
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlParameterCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val startTime = System.nanoTime()

        var isDirective = false
        val offset = parameters.editor.caretModel.currentCaret.offset
        val range =
            com.intellij.openapi.util
                .TextRange(offset, offset + 1)
        val caretNextText = parameters.editor.document.getText(range)
        try {
            val originalFile = parameters.originalFile
            val pos = parameters.originalPosition ?: return
            val bindText =
                cleanString(pos.text)
                    .substringAfter("/*")
                    .substringBefore("*/")

            val handler = DirectiveCompletion(originalFile, bindText, pos, caretNextText, result)
            val directiveSymbols = listOf("%", "#", "^", "@")
            directiveSymbols.forEach {
                if (!isDirective) {
                    isDirective = handler.directiveHandle(it)
                    if (isDirective) {
                        PluginLoggerUtil.countLogging(
                            this::class.java.simpleName,
                            "CompletionDirectiveBy$it",
                            "Completion",
                            startTime,
                        )
                        return@forEach
                    }
                }
            }

            if (!isDirective) {
                // Check when performing code completion on the right side
                val prevElm =
                    pos.prevLeafs.firstOrNull {
                        it.isNotWhiteSpace() &&
                            it.elementType != SqlTypes.DOT &&
                            it !is PsiErrorElement
                    }
                if (!pos.isNotWhiteSpace() && !isRightFactor(prevElm)) return
                val originalPosition = parameters.originalPosition ?: return
                val blockElements = getAccessElementTextBlocks(originalPosition)
                generateCompletionList(
                    blockElements,
                    pos,
                    originalFile,
                    caretNextText,
                    result,
                )
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "CompletionDirectiveByVariable",
                    "Completion",
                    startTime,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            result.stopHere()
        }
    }

    /**
     * Check to enable code completion even in the case of the right side
     */
    private fun isRightFactor(prevElm: PsiElement?) =
        (
            prevElm is SqlElEqExpr ||
                prevElm?.elementType == SqlTypes.EL_IDENTIFIER ||
                prevElm is SqlElGeExpr ||
                prevElm is SqlElGtExpr ||
                prevElm is SqlElLeExpr ||
                prevElm is SqlElLtExpr ||
                prevElm is SqlElNeExpr ||
                prevElm is SqlElOrExpr ||
                prevElm is SqlElAndExpr ||
                prevElm?.elementType == SqlTypes.EL_PLUS ||
                prevElm?.elementType == SqlTypes.EL_MINUS ||
                prevElm?.elementType == SqlTypes.ASTERISK ||
                prevElm?.elementType == SqlTypes.SLASH ||
                prevElm?.elementType == SqlTypes.EL_PERCENT ||
                prevElm?.isNotWhiteSpace() == true
        )

    private fun getAccessElementTextBlocks(targetElement: PsiElement): List<PsiElement> {
        var blocks: List<PsiElement> = emptyList()
        // If the immediate parent is a for, if, elseif directive,
        // get the field access element list from its own forward element.
        val parent =
            PsiTreeUtil.findFirstParent(targetElement) {
                it.elementType != SqlTypes.EL_ID_EXPR &&
                    it.elementType != SqlTypes.EL_IDENTIFIER
            }
        if (parent is SqlElForDirective ||
            parent is SqlElIfDirective ||
            parent is SqlElElseifDirective
        ) {
            val prevElms =
                targetElement.findSelfBlocks()
            if (prevElms.isNotEmpty()) {
                return prevElms
            }
        }

        // If the parent has field access, get its child element
        if (parent is SqlElFieldAccessExpr) {
            blocks =
                PsiTreeUtil
                    .getChildrenOfTypeAsList(parent, SqlElIdExpr::class.java)
                    .filter { it.parent !is SqlElClass }
                    .toList()
            if (blocks.isEmpty()) {
                val parent =
                    PsiTreeUtil.findFirstParent(parent) {
                        it !is PsiDirectory &&
                            it !is PsiFile &&
                            it is SqlElFieldAccessExpr
                    }

                blocks =
                    PsiTreeUtil
                        .getChildrenOfTypeAsList(parent, SqlElIdExpr::class.java)
                        .filter {
                            targetElement.startOffsetInParent >= it.startOffsetInParent
                        }.toList()
            }
        } else {
            // Completion for the first argument
            var parameterParent: PsiElement? =
                PsiTreeUtil.getParentOfType(targetElement, SqlElParameters::class.java)
            if (parameterParent != null) {
                val children = mutableListOf<PsiElement>()
                parameterParent.children.forEach { child ->
                    if (child is SqlElFieldAccessExpr) {
                        children.addAll(child.children)
                    } else {
                        children.add(child)
                    }
                }
                return children
            } else {
                // Completion for subsequent arguments
                parameterParent =
                    targetElement.prevLeafs
                        .takeWhile {
                            it.isNotWhiteSpace() &&
                                it.elementType != SqlTypes.LEFT_PAREN
                        }.firstOrNull {
                            PsiTreeUtil.getParentOfType(
                                it,
                                SqlElParameters::class.java,
                            ) != null
                        }
                if (parameterParent != null) {
                    val validateResult = targetElement.prevLeafs.firstOrNull { it is PsiErrorElement }
                    if (validateResult != null) {
                        parameterParent = (validateResult.parent as? SqlElParameters)
                        val children = mutableListOf<PsiElement>()
                        parameterParent
                            ?.children
                            ?.reversed()
                            ?.takeWhile {
                                it.nextSibling?.elementType != SqlTypes.COMMA
                            }?.forEach { child ->
                                if (child is SqlElFieldAccessExpr) {
                                    children.addAll(child.children)
                                } else {
                                    children.add(child)
                                }
                            }
                        blocks = children.reversed().takeWhile { it.nextSibling?.elementType != SqlTypes.COMMA }
                    }
                }
            }
        }

        // If the element has no parent-child relationship,
        // create a list that also adds itself at the end.
        if (blocks.isEmpty()) {
            val prevElms =
                targetElement.findSelfBlocks()
            if (prevElms.isNotEmpty()) {
                blocks = prevElms
            }
        }
        return blocks.sortedBy { it.textOffset }
    }

    /**
     * Determine the argument type from the input string,
     * recursively obtain the Entity field, and return it as a code completion list.
     */
    private fun generateCompletionList(
        elements: List<PsiElement>,
        position: PsiElement,
        originalFile: PsiFile,
        caretNextText: String,
        result: CompletionResultSet,
    ) {
        val daoMethod = findDaoMethod(originalFile)
        val searchText = cleanString(getSearchElementText(position))
        var topElementType: PsiType? = null
        if (elements.isEmpty() && daoMethod != null) {
            getElementTypeByFieldAccess(originalFile, position, elements, daoMethod, result)
            return
        }
        val top = elements.first()
        val topText = cleanString(getSearchElementText(top))
        val prevWord = PsiPatternUtil.getBindSearchWord(originalFile, elements.last(), " ")
        if (prevWord.startsWith("@") && prevWord.endsWith("@")) {
            setCompletionStaticFieldAccess(top, prevWord, caretNextText, topText, result)
            return
        }

        var isBatchAnnotation = false
        if (top.parent !is PsiFile && top.parent?.parent !is PsiDirectory) {
            val staticDirective = top.findNodeParent(SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR)
            staticDirective?.let {
                topElementType = getElementTypeByStaticFieldAccess(top, it, topText) ?: return
            }
        }

        if (daoMethod == null) return
        val project = originalFile.project
        val psiDaoMethod = PsiDaoMethod(project, daoMethod)
        if (topElementType == null) {
            isBatchAnnotation = psiDaoMethod.daoType.isBatchAnnotation()
            if (isFieldAccessByForItem(top, elements, searchText, caretNextText, isBatchAnnotation, result)) return
            topElementType =
                getElementTypeByFieldAccess(originalFile, position, elements, daoMethod, result) ?: return
        }

        setCompletionFieldAccess(
            topElementType,
            originalFile.project,
            isBatchAnnotation,
            elements,
            searchText,
            caretNextText,
            result,
        )
    }

    private fun getSearchElementText(elm: PsiElement?): String =
        if (elm is SqlElIdExpr || elm.elementType == SqlTypes.EL_IDENTIFIER) {
            elm?.text ?: ""
        } else {
            ""
        }

    /**
     * Retrieves the referenced class from a static field access element
     * and searches for a field or method matching the specified identifier name.
     * If no match is found, returns null.
     */
    private fun getElementTypeByStaticFieldAccess(
        top: PsiElement,
        staticDirective: PsiElement,
        topText: String,
    ): PsiType? {
        val clazz =
            getRefClazz(top) {
                staticDirective.children
                    .firstOrNull { it is SqlElClass }
                    ?.text
                    ?: ""
            } ?: return null

        return clazz.findStaticField(topText)?.type
            ?: clazz.findStaticMethod(topText)?.returnType
    }

    /**
     * Retrieves the DAO method parameters that match the text of the top element.
     * If the element list contains one or fewer items,
     * the DAO method parameters are registered as suggestions and this method returns null.
     * If there are additional elements, it returns the class type of the top element.
     */
    private fun getElementTypeByFieldAccess(
        originalFile: PsiFile,
        position: PsiElement,
        elements: List<PsiElement>,
        daoMethod: PsiMethod,
        result: CompletionResultSet,
    ): PsiType? {
        val topElement = elements.firstOrNull()
        val topText = cleanString(getSearchElementText(topElement))
        val matchParams = daoMethod.searchParameter(topText)
        val findParam = matchParams.find { it.name == topText }
        if (elements.size <= 1 && findParam == null) {
            matchParams.map { match ->
                result.addElement(LookupElementBuilder.create(match.name))
            }
            // Add ForDirective Items
            val parentForDirectiveExpr =
                PsiTreeUtil.getParentOfType(position, SqlElForDirective::class.java)
                    ?: PsiTreeUtil.getChildOfType(position.parent, SqlElForDirective::class.java)
            val forDirectives =
                ForDirectiveUtil.getForDirectiveBlocks(position).filter {
                    PsiTreeUtil.getParentOfType(it.item, SqlElForDirective::class.java) !=
                        parentForDirectiveExpr
                }
            addForDirectiveSuggestions(forDirectives, result)
            return null
        }
        if (findParam == null) {
            return null
        }
        val immediate = findParam.type
        return PsiClassTypeUtil.convertOptionalType(immediate, originalFile.project)
    }

    private fun addForDirectiveSuggestions(
        forDirectives: List<ForDirectiveUtil.BlockToken>,
        result: CompletionResultSet,
    ) {
        forDirectives.forEach {
            result.addElement(LookupElementBuilder.create(it.item.text))
            result.addElement(LookupElementBuilder.create("${it.item.text}_has_next"))
            result.addElement(LookupElementBuilder.create("${it.item.text}_index"))
        }
    }

    private fun getRefClazz(
        top: PsiElement,
        fqdnGetter: () -> String,
    ): PsiClass? = top.project.getJavaClazz(fqdnGetter())

    private fun setFieldsAndMethodsCompletionResultSet(
        caretNextText: String,
        fields: Array<PsiField>,
        methods: Array<PsiMethod>,
        result: CompletionResultSet,
    ) {
        result.addAllElements(fields.map { param -> VariableLookupItem(param) })
        methods.forEach { method ->
            val lookupElm =
                LookupElementBuilder
                    .create(createMethodLookupElement(caretNextText, method))
                    .withPresentableText(method.name)
                    .withTailText(method.parameterList.text, true)
                    .withTypeText(method.returnType?.presentableText ?: "")
            result.addElement(lookupElm)
        }
    }

    /**
     * When accessing a field starting from a for item, refer to the defined type and call the property.
     */
    private fun isFieldAccessByForItem(
        top: PsiElement,
        elements: List<PsiElement>,
        searchWord: String,
        caretNextText: String,
        isBatchAnnotation: Boolean = false,
        result: CompletionResultSet,
    ): Boolean {
        val project = top.project
        val forDirectiveBlocks = ForDirectiveUtil.getForDirectiveBlocks(top)
        val forItem = ForDirectiveUtil.findForItem(top, forDirectives = forDirectiveBlocks) ?: return false

        val forItemClassType = ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectiveBlocks, forItem) ?: return false
        val specifiedClassType = ForDirectiveUtil.resolveForDirectiveItemClassTypeBySuffixElement(top.text)
        val topClassType =
            if (specifiedClassType != null) {
                PsiParentClass(specifiedClassType)
            } else {
                forItemClassType
            }

        val result =
            ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                elements,
                project,
                topClassType,
                isBatchAnnotation = isBatchAnnotation,
                shortName = "",
                dropLastIndex = 1,
                complete = { lastType ->
                    setFieldsAndMethodsCompletionResultSet(
                        caretNextText,
                        lastType.searchField(searchWord)?.toTypedArray() ?: emptyArray(),
                        lastType.searchMethod(searchWord)?.toTypedArray() ?: emptyArray(),
                        result,
                    )
                },
            )
        return result is ValidationCompleteResult
    }

    private fun setCompletionFieldAccess(
        topElementType: PsiType,
        project: Project,
        isBatchAnnotation: Boolean,
        elements: List<PsiElement>,
        searchWord: String,
        caretNextText: String,
        result: CompletionResultSet,
    ) {
        var psiParentClass = PsiParentClass(topElementType)

        // FieldAccess Completion
        ForDirectiveUtil.getFieldAccessLastPropertyClassType(
            elements,
            project,
            psiParentClass,
            isBatchAnnotation = isBatchAnnotation,
            shortName = "",
            dropLastIndex = 1,
            complete = { lastType ->
                setFieldsAndMethodsCompletionResultSet(
                    caretNextText,
                    lastType.searchField(searchWord)?.toTypedArray() ?: emptyArray(),
                    lastType.searchMethod(searchWord)?.toTypedArray() ?: emptyArray(),
                    result,
                )
            },
        )
    }

    private fun setCompletionStaticFieldAccess(
        top: PsiElement,
        prevWord: String,
        caretNextText: String,
        topText: String,
        result: CompletionResultSet,
    ) {
        val clazz = getRefClazz(top) { prevWord.replace("@", "") } ?: return
        val matchFields = clazz.searchStaticField(topText)
        val matchMethod = clazz.searchStaticMethod(topText)

        // When you enter here, it is the top element, so return static fields and methods.
        setFieldsAndMethodsCompletionResultSet(caretNextText, matchFields, matchMethod, result)
    }
}
