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
package org.domaframework.doma.intellij.inspection.sql

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlBindVariableValidInspector

/**
 * A test that inspects whether a bind variable's parameters are defined.
 */
class ParameterDefinedTest : DomaSqlTest() {
    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "EmployeeSummaryDao.java",
        )
        addSqlFile(
            "EmployeeSummaryDao/bindVariableForEntityAndNonEntityParentClass.sql",
            "EmployeeSummaryDao/bindVariableForNonEntityClass.sql",
            "EmployeeSummaryDao/accessStaticProperty.sql",
            "EmployeeSummaryDao/batchAnnotationResolvesClassInList.sql",
            "EmployeeSummaryDao/resolveDaoArgumentOfListType.sql",
        )
        myFixture.enableInspections(SqlBindVariableValidInspector())
    }

    /**
     * Entity class instance field, method reference test
     * + Non-Entity parent class field, method reference test
     */
    fun testBindVariableForEntityAndNonEntityParentClass() {
        val sqlFile = findSqlFile("EmployeeSummaryDao/bindVariableForEntityAndNonEntityParentClass.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableForNonEntityClass() {
        val sqlFile = findSqlFile("EmployeeSummaryDao/bindVariableForNonEntityClass.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testAccessStaticProperty() {
        val sqlFile = findSqlFile("EmployeeSummaryDao/accessStaticProperty.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBatchAnnotationResolvesClassInList() {
        val sqlFile =
            findSqlFile("EmployeeSummaryDao/batchAnnotationResolvesClassInList.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testResolveDaoArgumentOfListType() {
        val sqlFile =
            findSqlFile("EmployeeSummaryDao/resolveDaoArgumentOfListType.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }
}
