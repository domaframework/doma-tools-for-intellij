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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import java.util.concurrent.ConcurrentHashMap

object DomaCompileConfigUtil {
    /**
     * Cache: key=file path, value=Pair<Properties, last update time>
     */
    private val configCache = ConcurrentHashMap<String, Pair<java.util.Properties, Long>>()

    /**
     * Get the value of the specified key from doma.compile.config
     * @param project active project
     * @param resourcePaths the path to the resource directories
     * @param key the key to retrieve the value for
     * @return the value associated with the key, or null if not found
     */
    fun getConfigValue(
        project: Project,
        resourcePaths: List<VirtualFile>,
        key: String,
    ): String? {
        resourcePaths.forEach { resourcePath ->
            if (resourcePath.isValid) {
                val configVFile = resourcePath.findChild("doma.compile.config")
                val cacheKey = "${project.basePath}/${resourcePath.path}/doma.compile.config"
                val lastModified = configVFile?.timeStamp ?: 0L
                val cached = configCache[cacheKey]

                val props =
                    if (cached == null || cached.second != lastModified) {
                        val loadedProps =
                            configVFile?.inputStream?.use { input ->
                                java.util.Properties().apply { load(input) }
                            } ?: java.util.Properties()
                        configCache[cacheKey] = loadedProps to lastModified
                        loadedProps
                    } else {
                        cached.first
                    }
                val propProperty = props.getProperty(key)
                if (propProperty != null) return propProperty
            } else {
                CommonPathParameterUtil.clearCache()
            }
        }
        return null
    }
}
