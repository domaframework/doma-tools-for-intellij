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
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.contributor.sql.processor.SqlCompletionDirectiveBlockProcessor
import org.domaframework.doma.intellij.contributor.sql.processor.SqlCompletionOtherBlockProcessor
import org.domaframework.doma.intellij.contributor.sql.processor.SqlCompletionParameterArgsBlockProcessor
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.findNodeParent
import org.domaframework.doma.intellij.extension.psi.findStaticField
import org.domaframework.doma.intellij.extension.psi.findStaticMethod
import org.domaframework.doma.intellij.extension.psi.searchParameter
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElIfDirective
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
            val bindText = cleanString(pos.text)

            val handler = DirectiveCompletion(originalFile, bindText, pos, caretNextText, result)
            val directiveSymbols = DirectiveCompletion.directiveSymbols
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

    private fun getAccessElementTextBlocks(targetElement: PsiElement): List<PsiElement> {
        if (PsiTreeUtil.prevLeaf(targetElement)?.elementType != SqlTypes.EL_IDENTIFIER &&
            targetElement.elementType != SqlTypes.EL_IDENTIFIER &&
            PsiTreeUtil.prevLeaf(targetElement)?.elementType != SqlTypes.DOT
        ) {
            return emptyList()
        }

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
            val processor = SqlCompletionDirectiveBlockProcessor()
            val blockElements = processor.generateBlock(targetElement)
            if (blockElements != null) return blockElements
        }

        // If the parent has field access, get its child element
        // Completion for the first argument in SqlElParameters.
        var parameterParent: PsiElement? =
            PsiTreeUtil.getParentOfType(targetElement, SqlElParameters::class.java)
        val processor = SqlCompletionParameterArgsBlockProcessor(targetElement)
        if (parameterParent != null && parameterParent.children.size <= 1) {
            return processor.generateFirstArgsBlock(parameterParent)
        } else {
            // Completion for subsequent arguments in SqlElParameters.
            val secondParameterParent = processor.getSecondArgsParent()
            val nextElementType = PsiTreeUtil.nextLeaf(targetElement, true)?.elementType
            if (secondParameterParent != null) {
                val parameterArg =
                    targetElement.prevLeafs.firstOrNull { it is PsiErrorElement } ?: targetElement
                blocks =
                    processor.generateSecondArgsBlock(parameterArg)
            }
        }

        // If the element has no parent-child relationship,
        // create a list that also adds itself at the end.
        if (blocks.isEmpty()) {
            val processor = SqlCompletionOtherBlockProcessor()
            blocks = processor.generateBlock(targetElement)
        }
        return blocks
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
        val project = originalFile.project
        val daoMethod = findDaoMethod(originalFile)
        val searchText = cleanString(getSearchElementText(position))
        var topElementType: PsiType? = null
        if (elements.isEmpty() && daoMethod != null) {
            getElementTypeByFieldAccess(originalFile, position, elements, daoMethod, result)
            return
        }
        val top = elements.first()
        val topText = cleanString(getSearchElementText(top))

        var isBatchAnnotation = false
        // In function-parameter elements, apply the same processing as normal field access.
        val parameter = top.findNodeParent(SqlTypes.EL_PARAMETERS)
        if (parameter == null) {
            val staticDirective = top.findNodeParent(SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR)
            staticDirective?.let {
                topElementType = getElementTypeByStaticFieldAccess(project, it, topText)
            }
            if (topElementType == null) {
                val fqdn =
                    StringUtil.getSqlElClassText(
                        PsiPatternUtil
                            .getBindSearchWord(originalFile, top, " "),
                    )
                topElementType = getElementTypeByPrevSqlElClassWords(project, fqdn, topText)
            }
        }

        if (daoMethod == null) return

        val psiDaoMethod = PsiDaoMethod(project, daoMethod)
        if (topElementType == null) {
            isBatchAnnotation = psiDaoMethod.daoType.isBatchAnnotation()
            if (isFieldAccessByForItem(
                    top,
                    elements,
                    searchText,
                    caretNextText,
                    isBatchAnnotation,
                    result,
                )
            ) {
                return
            }
            topElementType =
                getElementTypeByFieldAccess(originalFile, position, elements, daoMethod, result)
                    ?: return
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
        project: Project,
        staticDirective: PsiElement,
        topText: String,
    ): PsiType? {
        val clazz =
            getRefClazz(project) {
                staticDirective.children
                    .firstOrNull { it is SqlElClass }
                    ?.text
                    ?: ""
            } ?: return null

        return clazz.findStaticField(topText)?.type
            ?: clazz.findStaticMethod(topText)?.returnType
    }

    /**
     * Retrieves the class type from the previous element class words.
     * If the class is not found, returns null.
     * @param project The current project.
     * @param fqdn The fully qualified class name.
     * @param topText The name of the static property search that is being called first.
     * @return The class type of the static property, or null if not found.
     */
    private fun getElementTypeByPrevSqlElClassWords(
        project: Project,
        fqdn: String,
        topText: String,
    ): PsiType? {
        val clazz = getRefClazz(project) { fqdn } ?: return null
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
            result.addElement(LookupElementBuilder.create("${it.item.text}${ForDirectiveUtil.HAS_NEXT_PREFIX}"))
            result.addElement(LookupElementBuilder.create("${it.item.text}${ForDirectiveUtil.INDEX_PREFIX}"))
        }
    }

    private fun getRefClazz(
        project: Project,
        fqdnGetter: () -> String,
    ): PsiClass? = project.getJavaClazz(fqdnGetter())

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
}
