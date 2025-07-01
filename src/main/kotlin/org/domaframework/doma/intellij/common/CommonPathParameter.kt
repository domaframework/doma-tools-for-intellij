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

import com.intellij.compiler.CompilerConfiguration
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import org.domaframework.doma.intellij.common.CommonPathParameterUtil.refreshModulePaths
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import java.util.concurrent.ConcurrentHashMap

val RESOURCES_META_INF_PATH: String
    get() = "META-INF"

/**
 * A utility for caching directory information on a per-module basis.
 */
object CommonPathParameterUtil {
    /**
     * Holds directory information for a module.
     *
     * @property moduleBasePaths The base path of the module.
     * @property moduleSourceDirectories List of source directories.
     * @property moduleResourceDirectories List of resource directories.
     * @property moduleTestSourceDirectories List of test source directories.
     * @property moduleTestResourceDirectories List of test resource directories.
     */
    data class ModulePaths(
        val moduleBasePaths: List<VirtualFile>,
        val moduleSourceDirectories: List<VirtualFile>,
        val moduleResourceDirectories: List<VirtualFile>,
        val moduleTestSourceDirectories: List<VirtualFile>,
        val moduleTestResourceDirectories: List<VirtualFile>,
    )

    // Cache for each module's directory information.
    private val modulePathCache = ConcurrentHashMap<Int, ModulePaths>()

    /**
     * Returns the directory information for the specified module (uses cache if available).
     * If the module's directory structure has changed, call [refreshModulePaths] to update the cache.
     *
     * @param module The module to retrieve directory information for.
     * @return The cached or newly computed ModulePaths.
     */
    fun getModulePaths(module: Module): ModulePaths = modulePathCache[module.hashCode()] ?: refreshModulePaths(module)

    /**
     * Checks if a given path is a generated directory based on annotation processor settings.
     *
     * @param module The module to check.
     * @param path The path to check.
     * @return True if the path is a generated directory, false otherwise.
     */
    private fun isGeneratedDirectory(
        module: Module,
        path: String,
    ): Boolean {
        val project: Project = module.project
        val compilerConfiguration = CompilerConfiguration.getInstance(project).getAnnotationProcessingConfiguration(module)
        val annotationProcessingConfiguration = compilerConfiguration.getGeneratedSourcesDirectoryName(false)

        // Check if the path matches any of the generated source directories
        return path.contains("/build/$annotationProcessingConfiguration/")
    }

    /**
     * Refreshes the directory information for the specified module and updates the cache.
     * Call this method when the module's directory structure changes.
     *
     * @param module The module to refresh.
     * @return The updated ModulePaths.
     */
    fun refreshModulePaths(module: Module): ModulePaths {
        var basePath = mutableListOf<VirtualFile>()
        val sourceDirs = mutableListOf<VirtualFile>()
        val resourceDirs = mutableListOf<VirtualFile>()
        val testSourceDirs = mutableListOf<VirtualFile>()
        val testResourceDirs = mutableListOf<VirtualFile>()

        val moduleManager = ModuleRootManager.getInstance(module)
        moduleManager.contentEntries.forEach { entry ->
            val entryFile = entry.file
            if (entryFile != null && !isGeneratedDirectory(module, entryFile.path)) {
                entry.file?.let { basePath.add(it) }
                entry.sourceFolders.forEach { folder ->
                    val file = folder.file
                    if (file != null) {
                        when (folder.rootType) {
                            JavaSourceRootType.SOURCE ->
                                if (!sourceDirs.contains(file)) {
                                    sourceDirs.add(file)
                                }

                            JavaSourceRootType.TEST_SOURCE ->
                                if (!testSourceDirs.contains(file)) {
                                    testSourceDirs.add(file)
                                }

                            JavaResourceRootType.RESOURCE ->
                                if (!resourceDirs.contains(file)) {
                                    resourceDirs.add(file)
                                }

                            JavaResourceRootType.TEST_RESOURCE ->
                                if (!testResourceDirs.contains(file)) {
                                    testResourceDirs.add(file)
                                }
                        }
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
        modulePathCache[module.hashCode()] = paths
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
