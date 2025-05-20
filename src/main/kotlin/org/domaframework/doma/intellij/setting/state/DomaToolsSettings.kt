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
package org.domaframework.doma.intellij.common

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType

val RESOURCES_META_INF_PATH: String
    get() = "META-INF"

class CommonPathParameter(
    module: Module?,
) {
    /**
     * module base path ex)Absolute path of "/src/main"
     */
    var moduleBasePath: VirtualFile? = null

    /**
     * module source directories ex) Absolute path of "/src/main/java","/src/main/kotlin"
     */
    var moduleSourceDirectories: MutableList<VirtualFile> = mutableListOf()

    /**
     * module resource directory ex)  Absolute path of "/src/main/resources"
     */
    var moduleResourceDirectories: MutableList<VirtualFile> = mutableListOf()

    var moduleTestSourceDirectories: MutableList<VirtualFile> = mutableListOf()
    var moduleTestResourceDirectories: MutableList<VirtualFile> = mutableListOf()

    init {
        setModuleSourcesFiles(module)
    }

    private fun setModuleSourcesFiles(module: Module?) {
        if (module == null) return

        val modulemanager = ModuleRootManager.getInstance(module)

        moduleSourceDirectories.clear()
        modulemanager?.contentEntries?.firstOrNull()?.let { entry ->
            moduleBasePath = entry.file
            entry.sourceFolders.map { folder ->
                val file = folder.file
                if (file != null) {
                    println("file:${file.name}")
                    when (folder.rootType) {
                        JavaSourceRootType.SOURCE -> {
                            moduleSourceDirectories.add(file)
                            println("moduleSourceDirectory:$file")
                        }

                        JavaSourceRootType.TEST_SOURCE -> {
                            moduleTestSourceDirectories.add(file)
                            println("moduleTestSourceDirectory:$file")
                        }

                        JavaResourceRootType.RESOURCE -> {
                            moduleResourceDirectories.add(file)
                            println("moduleResourceDirectory:$file")
                        }

                        JavaResourceRootType.TEST_RESOURCE -> {
                            moduleTestResourceDirectories.add(file)
                            println("moduleTestResourceDirectory:$file")
                        }
                    }
                }
            }
        }
    }

    fun isTest(file: VirtualFile): Boolean {
        val testSource =
            moduleTestSourceDirectories.firstOrNull { testSource ->
                file.path.contains(testSource.path)
            }
        if (testSource != null) return true

        return moduleTestResourceDirectories.firstOrNull { testSource ->
            file.path.contains(testSource.path)
        } != null
    }

    fun getResources(file: VirtualFile): MutableList<VirtualFile> =
        if (isTest(file)) {
            moduleTestResourceDirectories
        } else {
            moduleResourceDirectories
        }

    fun getSources(file: VirtualFile): MutableList<VirtualFile> =
        if (isTest(file)) {
            moduleTestSourceDirectories
        } else {
            moduleSourceDirectories
        }
}
