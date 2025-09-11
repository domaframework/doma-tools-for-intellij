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
package org.domaframework.doma.intellij.action.sql

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.action.dao.AbstractConvertSqlFileToAnnotationAction
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isSupportFileType

/**
 * Intention action to convert SQL file to @Sql annotation from SQL files
 */
class ConvertSqlFileToAnnotationFromSqlAction : AbstractConvertSqlFileToAnnotationAction() {
    override fun getFamilyName(): String = MessageBundle.message("convert.sql.file.to.annotation.from.sql.family")

    override fun getText(): String = MessageBundle.message("convert.sql.file.to.annotation.from.sql.text")

    override fun isTargetFile(element: PsiElement): Boolean {
        val file = element.containingFile ?: return false
        return isSupportFileType(file)
    }

    override fun getDaoMethod(element: PsiElement): PsiMethod? = findDaoMethod(element.containingFile)

    override fun getActionName(): String = "convertSqlFileToAnnotationFromSql"
}
