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
package org.domaframework.doma.intellij.common.util

import com.intellij.openapi.project.Project
import org.domaframework.doma.intellij.setting.state.DomaToolsFormatEnableSettings

object DomaToolsSettingUtil {
    /**
     * Checks if SQL formatting is enabled in the project settings.
     *
     * @param project The project to check.
     * @return `true` if SQL formatting is enabled, `false` otherwise.
     */
    fun isEnableFormat(project: Project): Boolean {
        val setting = DomaToolsFormatEnableSettings.getInstance(project)
        val isEnableFormat = setting.state.isEnableSqlFormat
        return isEnableFormat
    }
}
