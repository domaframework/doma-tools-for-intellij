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

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.psi.SqlElExpr

/**
 * Directive information for static property references
 */
class PsiStaticElement(
    elExprList: List<SqlElExpr>? = null,
    private val originalFile: PsiFile,
) {
    private var fqdn = elExprList?.joinToString(".") { e -> e.text } ?: ""

    constructor(elExprNames: String, file: PsiFile) : this(null, file) {
        fqdn = StringUtil.getSqlElClassText(elExprNames)
    }

    fun getRefClazz(): PsiClass? {
        val project = originalFile.project
        return project.getJavaClazz(fqdn)
            ?: JavaPsiFacade.getInstance(originalFile.project).findClass(
                fqdn,
                GlobalSearchScope.allScope(originalFile.project),
            )
    }
}
