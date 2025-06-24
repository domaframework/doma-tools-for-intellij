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
package org.domaframework.doma.intellij.inspection.dao.processor

import org.domaframework.doma.intellij.common.util.DomaClassName

/**
 * Represents a strategy parameter for Doma DAO method inspections.
 *
 * Provides methods to determine if the parameter is a stream or collect type
 * in the context of select operations.
 *
 * @property fieldName The name of the field.
 * @property isSelectType True if the parent class is a select type.
 */
class StrategyParam(
    val fieldName: String = "",
    parentClassName: String?,
) {
    private val isSelectType: Boolean = parentClassName == DomaClassName.SELECT_TYPE.className

    /**
     * Checks if the parameter represents a stream type.
     * @return True if the field is STREAM and the parent is select type.
     */
    fun isStream(): Boolean = fieldName == "STREAM" && isSelectType

    /**
     * Checks if the parameter represents a collect type.
     * @return True if the field is COLLECT and the parent is select type.
     */
    fun isCollect(): Boolean = fieldName == "COLLECT" && isSelectType
}
