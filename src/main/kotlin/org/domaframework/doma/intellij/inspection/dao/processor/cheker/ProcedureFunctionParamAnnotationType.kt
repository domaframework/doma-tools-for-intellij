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
package org.domaframework.doma.intellij.inspection.dao.processor.cheker

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.DomaClassName

enum class ProcedureFunctionParamAnnotationType(
    val fqdn: String,
    val requireType: String,
    val checkParamType: (
        psiDaoMethod: PsiDaoMethod,
        identifier: PsiElement,
        paramType: PsiType,
        project: Project,
        shortName: String,
        holder: ProblemsHolder,
    ) -> Unit,
) {
    In("org.seasar.doma.In", "", { psiDaoMethod, identifier, paramType, project, shortName, holder ->
        ProcedureFunctionInParamAnnotationTypeChecker(psiDaoMethod).checkParam(identifier, paramType, project, shortName, holder)
    }),
    InOut(
        "org.seasar.doma.InOut",
        DomaClassName.REFERENCE.className,
        { psiDaoMethod, identifier, paramType, project, shortName, holder ->
            ProcedureFunctionInOutParamAnnotationTypeChecker(
                InOut,
                psiDaoMethod,
            ).checkParam(identifier, paramType, project, shortName, holder)
        },
    ),
    Out(
        "org.seasar.doma.Out",
        DomaClassName.REFERENCE.className,
        { psiDaoMethod, identifier, paramType, project, shortName, holder ->
            ProcedureFunctionInOutParamAnnotationTypeChecker(
                Out,
                psiDaoMethod,
            ).checkParam(identifier, paramType, project, shortName, holder)
        },
    ),
    ResultSet(
        "org.seasar.doma.ResultSet",
        DomaClassName.LIST.className,
        { psiDaoMethod, identifier, paramType, project, shortName, holder ->
            ProcedureFunctionResultSetParamAnnotationTypeChecker(psiDaoMethod).checkParam(identifier, paramType, project, shortName, holder)
        },
    ),
}
