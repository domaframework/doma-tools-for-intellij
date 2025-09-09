package org.domaframework.doma.intellij.common.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr
import org.domaframework.doma.intellij.psi.SqlElParameters

 class MethodParamContext(val methodIdExp: PsiElement, val methodParams: SqlElParameters?) {
     companion object {
         fun of(method: PsiElement): MethodParamContext {
             return MethodParamContext(
                 setMethodIdExp(method),
                 setParameter(method)
             )
         }
         private fun setParameter(method: PsiElement): SqlElParameters? {
             return if (method is SqlElFunctionCallExpr) {
                 method.elParameters
             } else {
                 PsiTreeUtil.nextLeaf(method)?.parent as? SqlElParameters
             }
         }

         private fun setMethodIdExp(method: PsiElement): PsiElement {
             return if (method is SqlElFunctionCallExpr) {
                 method.elIdExpr
             } else {
                 method
             }
         }
     }
 }
