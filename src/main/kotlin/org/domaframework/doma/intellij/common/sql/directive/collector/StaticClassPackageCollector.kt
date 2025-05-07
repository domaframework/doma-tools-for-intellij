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
package org.domaframework.doma.intellij.common.sql.directive.collector

import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiPackage
import com.intellij.psi.search.GlobalSearchScope
import org.domaframework.doma.intellij.common.psi.PsiPatternUtil
import org.domaframework.doma.intellij.common.sql.directive.ICON_MAP
import org.domaframework.doma.intellij.common.sql.directive.StaticClassPackageSearchResult

class StaticClassPackageCollector(
    private val element: PsiElement,
    private val module: Module,
) : StaticDirectiveHandlerCollector() {
    public override fun collect(): List<LookupElement>? {
        val file = element.containingFile ?: return null
        val packageNames = mutableSetOf<StaticClassPackageSearchResult>()

        val prevPackageNames =
            PsiPatternUtil.getBindSearchWord(file, element, "@").split(".")
        val psiFacade = JavaPsiFacade.getInstance(module.project) ?: return null
        val topPackages = psiFacade.findPackage("")?.subPackages
        packageNames.addAll(searchPackage(module, topPackages, prevPackageNames))

        return packageNames.mapNotNull { pkg ->
            val icon = ICON_MAP[pkg.fileType] ?: AllIcons.FileTypes.Unknown
            LookupElementBuilder
                .create(pkg.createText)
                .withPresentableText(pkg.qualifiedName)
                .withTailText("(${pkg.packageName})", true)
                .withIcon(icon)
                .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
        }
    }

    private fun searchPackage(
        module: Module,
        topPackages: Array<out PsiPackage?>?,
        prevPackageNames: List<String> = emptyList(),
    ): MutableSet<StaticClassPackageSearchResult> {
        var subPackagesParent: PsiPackage? = null
        var parentPackages: Array<out PsiPackage?>? = topPackages

        prevPackageNames.dropLast(1).forEach { pkg ->
            subPackagesParent =
                parentPackages
                    ?.find { it?.name?.contains(pkg) == true }
            parentPackages =
                subPackagesParent?.subPackages
        }

        val packages = mutableSetOf<StaticClassPackageSearchResult>()

        val targetSubPackages =
            parentPackages
                ?.mapNotNull { subPkg -> subPkg }
        targetSubPackages?.let { pkg ->
            packages.addAll(
                pkg.map {
                    StaticClassPackageSearchResult(
                        it.qualifiedName,
                        it.name ?: "",
                        it.qualifiedName,
                        "package",
                    )
                },
            )
        }

        val files =
            subPackagesParent
                ?.getFiles(GlobalSearchScope.allScope(module.project))
                ?.mapNotNull { it }
                ?: emptyList()
        files.forEach { file ->
            createPackageFileResult(file)?.let { packages.addAll(it) }
        }
        return packages
    }

    private fun createPackageFileResult(file: PsiFile): List<StaticClassPackageSearchResult>? {
        val psiJavaFile = file as? PsiJavaFile ?: return emptyList()
        val foundClasses = mutableListOf<PsiClass>()
        psiJavaFile.classes.forEach { topClazz ->
            visitClass(topClazz, foundClasses)
        }

        return foundClasses.map { clazz ->
            val clazzName = clazz.name ?: ""
            val packageName = clazz.qualifiedName?.replace(".$clazzName", "") ?: clazzName
            StaticClassPackageSearchResult(
                packageName,
                clazzName,
                "$packageName.$clazzName",
                createFileType(clazz),
            )
        }
    }

    private fun visitClass(
        clazz: PsiClass,
        foundClasses: MutableList<PsiClass>,
    ) {
        foundClasses += clazz
        clazz.innerClasses.forEach { nested ->
            visitClass(nested, foundClasses)
        }
    }

    private fun createFileType(type: PsiClass): String =
        when {
            type.isEnum -> "enum"
            type.isAnnotationType -> "annotation"
            type.isInterface -> "interface"
            type.isRecord -> "record"
            else -> type.containingFile?.fileType?.name ?: ""
        }.toString()
}
