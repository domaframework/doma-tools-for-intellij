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
package org.domaframework.doma.intellij.common.config

import com.intellij.openapi.module.Module
import org.domaframework.doma.intellij.extension.getResourcesFile
import java.util.concurrent.ConcurrentHashMap

object DomaCompileConfigUtil {
    const val EXPRESSION_FUNCTIONS_NAME = "org.seasar.doma.expr.ExpressionFunctions"

    /**
     * Cache: key=file path, value=Pair<Properties, last update time>
     */
    private val configCache = ConcurrentHashMap<String, Pair<java.util.Properties, Long>>()

    /**
     * Get the value of the specified key from doma.compile.config
     * @param module the module to retrieve the configuration from
     * @param isTest true if the configuration is for test sources, false for main sources
     * @param key the key to retrieve the value for
     * @return the value associated with the key, or null if not found
     */
    fun getConfigValue(
        module: Module,
        isTest: Boolean,
        key: String,
    ): String? {
        val settingFileName = "doma.compile.config"
        val settingFile = module.getResourcesFile(settingFileName, isTest)
        val cacheKey = "${settingFile?.path}/$settingFileName"
        val lastModified = settingFile?.timeStamp ?: 0L
        val cached = configCache[cacheKey]

        val props =
            if (cached == null || cached.second != lastModified) {
                val loadedProps =
                    settingFile?.inputStream?.use { input ->
                        java.util.Properties().apply { load(input) }
                    } ?: java.util.Properties()
                configCache[cacheKey] = loadedProps to lastModified
                loadedProps
            } else {
                cached.first
            }
        val propProperty = props.getProperty(key)
        return propProperty
    }
}
