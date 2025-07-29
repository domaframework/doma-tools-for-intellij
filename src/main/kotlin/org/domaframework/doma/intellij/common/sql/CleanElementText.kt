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
package org.domaframework.doma.intellij.common.sql

import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.common.util.StringUtil.SINGLE_SPACE

/**
 * Exclude extra strings and block symbols added by IntelliJ operations a
 * nd format them into necessary elements
 */
fun cleanString(str: String): String {
    val intelliKIdeaRuleZzz = "IntellijIdeaRulezzz"
    return StringUtil
        .replaceBlockCommentStartEnd(str)
        .replace(intelliKIdeaRuleZzz, "")
        // TODO: Temporary support when using operators.
        //  Remove the "== a" element because it is attached to the end.
        //  Make it possible to obtain the equilateral elements of the left side individually.
        .substringBefore(SINGLE_SPACE)
}
