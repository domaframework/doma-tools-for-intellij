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
package org.domaframework.doma.intellij.extension

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ResourceFileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import org.domaframework.doma.intellij.common.dao.getRelativeSqlFilePathFromDaoFilePath

/**
 * Get SQL directory corresponding to Dao file
 */
fun Module.getPackagePathFromDaoPath(daoFile: VirtualFile): VirtualFile? {
    val contentRoot = this.project.getContentRoot(daoFile)?.path
    val packagePath =
        contentRoot?.let {
            getRelativeSqlFilePathFromDaoFilePath(daoFile, this)
        } ?: ""

    return this.getResourcesSQLFile(
        packagePath,
        false,
    )
}

fun Module.getJavaClazz(
    includeTest: Boolean,
    fqdn: String,
): PsiClass? {
    val scope = GlobalSearchScope.moduleRuntimeScope(this, includeTest)
    return JavaPsiFacade
        .getInstance(this.project)
        .findClasses(fqdn, scope)
        .firstOrNull()
}

/***
 * Get SQL file corresponding to Dao file
 * @param relativePath SQL file relativePath path ex) META-INF/packageName/dao/DaoClassName/sqlFileName.sql
 * @param includeTest true: test source, false: main source
 * @return SQL file
 */
fun Module.getResourcesSQLFile(
    relativePath: String,
    includeTest: Boolean,
): VirtualFile? =
    ResourceFileUtil.findResourceFileInScope(
        relativePath.replace("//", "/"),
        this.project,
        this.getModuleScope(includeTest),
    )
