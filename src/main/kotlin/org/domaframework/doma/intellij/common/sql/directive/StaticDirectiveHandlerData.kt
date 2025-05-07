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

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.VariableLookupItem
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiType

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

data class StaticClassPackageSearchResult(
    val packageName: String,
    val qualifiedName: String,
    val createText: String,
    val fileType: String,
)

val ICON_MAP =
    mapOf(
        "enum" to AllIcons.Nodes.Enum,
        "annotation" to AllIcons.Nodes.Annotationtype,
        "interface" to AllIcons.Nodes.Interface,
        "record" to AllIcons.Nodes.Record,
        "package" to AllIcons.Nodes.Package,
        "JAVA" to AllIcons.FileTypes.Java,
        "CLASS" to AllIcons.Nodes.Class,
    )
