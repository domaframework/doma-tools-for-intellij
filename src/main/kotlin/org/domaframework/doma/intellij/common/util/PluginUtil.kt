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

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

const val PLUGIN_ID = "org.domaframework.doma.intellij"
const val PLUGIN_VERSION = "2.4.1"

open class PluginUtil {
    companion object {
        fun getVersion(): String {
            val pluginId = PluginId.getId(PLUGIN_ID)
            return PluginManagerCore.getPlugin(pluginId)?.let {
                return it.version
            } ?: PLUGIN_VERSION
        }
    }
}
