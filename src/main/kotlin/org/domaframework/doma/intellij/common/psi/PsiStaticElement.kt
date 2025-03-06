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
package org.domaframework.doma.intellij.common.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.psi.SqlElExpr
import org.jetbrains.kotlin.idea.base.util.module

/**
 * Directive information for static property references
 */
class PsiStaticElement(
    elExprList: List<SqlElExpr>? = null,
    originalFile: PsiFile,
) {
    private var fqdn = elExprList?.joinToString(".") { e -> e.text } ?: ""
    private val module = originalFile.module

    constructor(elExprNames: String, file: PsiFile) : this(null, file) {
        fqdn =
            elExprNames
                .substringAfter("@")
                .substringBefore("@")
    }

    fun getRefClazz(): PsiClass? = module?.getJavaClazz(true, fqdn)
}
