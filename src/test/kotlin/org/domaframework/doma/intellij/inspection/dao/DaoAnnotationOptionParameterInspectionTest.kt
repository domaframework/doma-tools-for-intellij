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
package org.domaframework.doma.intellij.inspection.dao

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.inspection.dao.inspector.DaoAnnotationOptionParameterInspection

class DaoAnnotationOptionParameterInspectionTest : DomaSqlTest() {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(DaoAnnotationOptionParameterInspection())
        addEntityJavaFile("Department.java")
        addDaoJavaFile("inspection/option/AnnotationOptionTestDao.java")
    }

    fun testAnnotationOptionParameter() {
        val clazz = myFixture.findClass("doma.example.dao.inspection.option.AnnotationOptionTestDao")
        myFixture.testHighlighting(false, false, false, clazz.containingFile.virtualFile)
    }
}
