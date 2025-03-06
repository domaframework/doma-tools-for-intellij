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

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeafs
import org.domaframework.doma.intellij.extension.psi.isNotWhiteSpace
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

enum class DirectiveType {
    PERCENT,
    STATIC,
    BUILT_IN,
    NOT_DIRECTIVE,
}

object BindDirectiveUtil {
    /**
     * Determine which directive the block to validate belongs to
     */
    fun getDirectiveType(element: PsiElement): DirectiveType {
        // During input, "%" is recognized as an error token,
        // so search by simple string comparison instead of element type.
        element.prevLeafs
            .firstOrNull { p ->
                (
                    p.text == "%" ||
                        p.prevSibling?.text == "%" ||
                        p is SqlElStaticFieldAccessExpr ||
                        p.elementType == SqlTypes.EL_AT_SIGN
                ) ||
                    p.elementType == SqlTypes.BLOCK_COMMENT_START ||
                    !p.isNotWhiteSpace()
            }?.let {
                return when {
                    !it.isNotWhiteSpace() -> DirectiveType.NOT_DIRECTIVE
                    it.text == "%" || it.prevSibling?.text == "%" -> DirectiveType.PERCENT
                    it is SqlElStaticFieldAccessExpr -> DirectiveType.STATIC
                    it.elementType == SqlTypes.EL_AT_SIGN -> DirectiveType.BUILT_IN
                    else -> DirectiveType.NOT_DIRECTIVE
                }
            }

        return DirectiveType.NOT_DIRECTIVE
    }
}
