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
package org.domaframework.doma.intellij.setting

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import org.domaframework.doma.intellij.common.CommonPathParameterUtil

class DomaToolsModuleRootListener(
    private val project: Project,
) : ModuleRootListener {
    override fun rootsChanged(event: ModuleRootEvent) {
        updateModuleDirectoryCache(project)
    }

    fun updateModuleDirectoryCache(project: Project) {
        val modules = ModuleManager.getInstance(project).modules
        modules
            .filter { module -> module.name != project.name }
            .forEach { module ->
                CommonPathParameterUtil.refreshModulePaths(module)
            }
    }
}
