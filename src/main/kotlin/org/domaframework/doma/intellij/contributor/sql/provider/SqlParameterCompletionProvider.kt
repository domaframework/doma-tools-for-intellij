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
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.domaframework.doma.intellij.common.sql.cleanString
import org.domaframework.doma.intellij.common.sql.directive.DirectiveCompletion
import org.domaframework.doma.intellij.common.sql.validator.SqlElForItemFieldAccessorChildElementValidator
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.findNodeParent
import org.domaframework.doma.intellij.extension.psi.findSelfBlocks
import org.domaframework.doma.intellij.extension.psi.findStaticField
import org.domaframework.doma.intellij.extension.psi.findStaticMethod
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz
import org.domaframework.doma.intellij.extension.psi.isNotWhiteSpace
import org.domaframework.doma.intellij.extension.psi.searchParameter
import org.domaframework.doma.intellij.extension.psi.searchStaticField
import org.domaframework.doma.intellij.extension.psi.searchStaticMethod
import org.domaframework.doma.intellij.inspection.ForDirectiveInspection
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
import org.jetbrains.kotlin.idea.base.util.module

class SqlParameterCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val startTime = System.nanoTime()

        var isDirective = false
        try {
            val originalFile = parameters.originalFile
            val project = originalFile.project
            val pos = parameters.originalPosition ?: return
            val bindText =
                cleanString(pos.text)
                    .substringAfter("/*")
                    .substringBefore("*/")

            val handler = DirectiveCompletion(originalFile, bindText, pos, project, result)
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
                        child.add(child)
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
        result: CompletionResultSet,
    ) {
        val searchText = cleanString(getSearchElementText(position))
        var topElementType: PsiType? = null
        if (elements.isEmpty()) {
            getElementTypeByFieldAccess(originalFile, elements, result)
            return
        }
        val top = elements.first()

        val topText = cleanString(getSearchElementText(top))
        val prevWord = PsiPatternUtil.getBindSearchWord(originalFile, elements.last(), " ")
        if (prevWord.startsWith("@") && prevWord.endsWith("@")) {
            setStaticFieldAccess(top, prevWord, topText, result)
            return
        }
        if (top.parent !is PsiFile && top.parent?.parent !is PsiDirectory) {
            val staticDirective = top.findNodeParent(SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR)
            staticDirective?.let {
                topElementType = getElementTypeByStaticFieldAccess(top, it, topText) ?: return
            }
        }

        if (topElementType == null) {
            if (isFieldAccessByForItem(top, elements, searchText, result)) return
            topElementType =
                getElementTypeByFieldAccess(originalFile, elements, result) ?: return
        }

        val fieldAccessorChildElementValidator =
            SqlElForItemFieldAccessorChildElementValidator(
                elements,
                PsiParentClass(topElementType),
            )

        var psiParentClass = PsiParentClass(topElementType)
        var parentProperties: Array<PsiField> = psiParentClass.clazz?.allFields ?: emptyArray()
        var parentMethods: Array<PsiMethod> = psiParentClass.clazz?.allMethods ?: emptyArray()

        val validateResult =
            fieldAccessorChildElementValidator.validateChildren(
                complete = { parent ->
                    parentProperties =
                        parent.searchField(searchText)?.toTypedArray() ?: emptyArray()
                    parentMethods = parent.searchMethod(searchText)?.toTypedArray() ?: emptyArray()
                    setFieldsAndMethodsCompletionResultSet(
                        parentProperties,
                        parentMethods,
                        result,
                    )
                },
            )

        if (validateResult is ValidationPropertyResult) {
            val parent = validateResult.parentClass ?: return
            parent.searchField(searchText)?.let {
                parentProperties = it.toTypedArray()
            } ?: { parentProperties = emptyArray() }
            val methods = parent.searchMethod(searchText)
            parentMethods = methods?.toTypedArray() ?: emptyArray()
            setFieldsAndMethodsCompletionResultSet(parentProperties, parentMethods, result)
        }
    }

    private fun setStaticFieldAccess(
        top: PsiElement,
        prevWord: String,
        topText: String,
        result: CompletionResultSet,
    ) {
        val clazz = getRefClazz(top) { prevWord.replace("@", "") } ?: return
        val matchFields = clazz.searchStaticField(topText)
        val matchMethod = clazz.searchStaticMethod(topText)

        // When you enter here, it is the top element, so return static fields and methods.
        setFieldsAndMethodsCompletionResultSet(matchFields, matchMethod, result)
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
        elements: List<PsiElement>,
        result: CompletionResultSet,
    ): PsiType? {
        val daoMethod = findDaoMethod(originalFile) ?: return null
        val topText = cleanString(getSearchElementText(elements.firstOrNull()))
        val matchParams = daoMethod.searchParameter(topText)
        val findParam = matchParams.find { it.name == topText }
        if (elements.size <= 1 && findParam == null) {
            matchParams.map { match ->
                result.addElement(LookupElementBuilder.create(match.name))
            }
            return null
        }
        if (findParam == null) {
            return null
        }
        val immediate = findParam.getIterableClazz(daoMethod.getDomaAnnotationType())
        return immediate.type
    }

    private fun getRefClazz(
        top: PsiElement,
        fqdnGetter: () -> String,
    ): PsiClass? = top.module?.getJavaClazz(true, fqdnGetter())

    private fun setFieldsAndMethodsCompletionResultSet(
        fields: Array<PsiField>,
        methods: Array<PsiMethod>,
        result: CompletionResultSet,
    ) {
        result.addAllElements(fields.map { param -> VariableLookupItem(param) })
        methods.forEach { method ->
            val lookupElm =
                LookupElementBuilder
                    .create("${method.name}()")
                    .withPresentableText(method.name)
                    .withTailText(method.parameterList.text, true)
                    .withTypeText(method.returnType?.presentableText ?: "")
            result.addElement(lookupElm)
        }
    }

    private fun isFieldAccessByForItem(
        top: PsiElement,
        elements: List<PsiElement>,
        positionText: String,
        result: CompletionResultSet,
    ): Boolean {
        val file = top.containingFile ?: return false
        val daoMethod = findDaoMethod(file) ?: return false
        val forDeclaration = ForDirectiveInspection(daoMethod)
        val forItem = forDeclaration.getForItem(top)
        if (forItem != null) {
            val validateResult = forDeclaration.validateFieldAccessByForItem(elements)
            if (validateResult is ValidationCompleteResult) {
                val validator =
                    SqlElForItemFieldAccessorChildElementValidator(
                        elements,
                        validateResult.parentClass,
                    )
                val forValidationResult = validator.validateChildren(1)?.parentClass ?: return false

                val parentClass = forValidationResult
                val searchWord = cleanString(positionText)
                setFieldsAndMethodsCompletionResultSet(
                    parentClass.searchField(searchWord)?.toTypedArray() ?: emptyArray(),
                    parentClass.searchMethod(searchWord)?.toTypedArray() ?: emptyArray(),
                    result,
                )
                return true
            }
        }
        return false
    }
}
