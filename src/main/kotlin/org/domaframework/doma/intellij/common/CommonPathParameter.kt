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
import java.util.concurrent.ConcurrentHashMap

const val RESOURCES_META_INF_PATH: String = "META-INF"

/**
 * A utility for caching directory information on a per-module basis.
 */
object CommonPathParameterUtil {
    /**
     * Holds directory information for a module.
     *
     * @property moduleBasePath The base path of the module.
     * @property moduleSourceDirectories List of source directories.
     * @property moduleResourceDirectories List of resource directories.
     * @property moduleTestSourceDirectories List of test source directories.
     * @property moduleTestResourceDirectories List of test resource directories.
     */
    data class ModulePaths(
        val moduleBasePath: VirtualFile?,
        val moduleSourceDirectories: List<VirtualFile>,
        val moduleResourceDirectories: List<VirtualFile>,
        val moduleTestSourceDirectories: List<VirtualFile>,
        val moduleTestResourceDirectories: List<VirtualFile>,
    )

    // Cache for each module's directory information.
    private val modulePathCache = ConcurrentHashMap<Module, ModulePaths>()

    /**
     * Returns the directory information for the specified module (uses cache if available).
     * If the module's directory structure has changed, call [refreshModulePaths] to update the cache.
     *
     * @param module The module to retrieve directory information for.
     * @return The cached or newly computed ModulePaths.
     */
    fun getModulePaths(module: Module): ModulePaths = modulePathCache[module] ?: refreshModulePaths(module)

    /**
     * Refreshes the directory information for the specified module and updates the cache.
     * Call this method when the module's directory structure changes.
     *
     * @param module The module to refresh.
     * @return The updated ModulePaths.
     */
    fun refreshModulePaths(module: Module): ModulePaths {
        var basePath: VirtualFile? = null
        val sourceDirs = mutableListOf<VirtualFile>()
        val resourceDirs = mutableListOf<VirtualFile>()
        val testSourceDirs = mutableListOf<VirtualFile>()
        val testResourceDirs = mutableListOf<VirtualFile>()

        val moduleManager = ModuleRootManager.getInstance(module)
        moduleManager.contentEntries.firstOrNull()?.let { entry ->
            basePath = entry.file
            entry.sourceFolders.forEach { folder ->
                val file = folder.file
                if (file != null) {
                    when (folder.rootType) {
                        JavaSourceRootType.SOURCE -> sourceDirs.add(file)
                        JavaSourceRootType.TEST_SOURCE -> testSourceDirs.add(file)
                        JavaResourceRootType.RESOURCE -> resourceDirs.add(file)
                        JavaResourceRootType.TEST_RESOURCE -> testResourceDirs.add(file)
                    }
                }
            }
        }
        val paths =
            ModulePaths(
                basePath,
                sourceDirs,
                resourceDirs,
                testSourceDirs,
                testResourceDirs,
            )
        modulePathCache[module] = paths
        return paths
    }

    /**
     * Determines if the given file belongs to a test source or test resource directory.
     *
     * @param module The module to check.
     * @param file The file to check.
     * @return True if the file is in a test directory, false otherwise.
     */
    fun isTest(
        module: Module,
        file: VirtualFile,
    ): Boolean {
        val paths = getModulePaths(module)
        if (paths.moduleTestSourceDirectories.any { file.path.contains(it.path) }) return true
        if (paths.moduleTestResourceDirectories.any { file.path.contains(it.path) }) return true
        return false
    }

    /**
     * Returns the resource directories for the given file in the specified module.
     * If the file is in a test directory, test resource directories are returned.
     *
     * @param module The module to check.
     * @param file The file to check.
     * @return List of resource directories.
     */
    fun getResources(
        module: Module,
        file: VirtualFile,
    ): List<VirtualFile> =
        if (isTest(module, file)) {
            getModulePaths(module).moduleTestResourceDirectories
        } else {
            getModulePaths(module).moduleResourceDirectories
        }

    /**
     * Returns the source directories for the given file in the specified module.
     * If the file is in a test directory, test source directories are returned.
     *
     * @param module The module to check.
     * @param file The file to check.
     * @return List of source directories.
     */
    fun getSources(
        module: Module,
        file: VirtualFile,
    ): List<VirtualFile> =
        if (isTest(module, file)) {
            getModulePaths(module).moduleTestSourceDirectories
        } else {
            getModulePaths(module).moduleSourceDirectories
        }

    /**
     * Clears the module directory cache. Call this if the module structure changes.
     */
    fun clearCache() {
        modulePathCache.clear()
    }
}
