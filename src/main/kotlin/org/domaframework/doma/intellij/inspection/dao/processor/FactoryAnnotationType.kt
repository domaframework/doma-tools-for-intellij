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
package org.domaframework.doma.intellij.inspection.dao.processor

/**
 * Enum representing factory annotation types for Doma DAO methods.
 *
 * Each entry defines the fully qualified annotation name, the expected return type,
 * and the required parameter count for the factory method.
 *
 * @property fqdn The fully qualified name of the annotation.
 * @property returnType The expected return type for the factory.
 * @property paramCount The number of parameters required by the factory method.
 */
enum class FactoryAnnotationType(
    val fqdn: String,
    val returnType: String,
    val paramCount: Int = 0,
) {
    ArrayFactory("org.seasar.doma.ArrayFactory", "java.sql.Array", 1),
    BlobFactory("org.seasar.doma.BlobFactory", "java.sql.Blob"),
    ClobFactory("org.seasar.doma.ClobFactory", "java.sql.Clob"),
    NClobFactory("org.seasar.doma.NClobFactory", "java.sql.NClob"),
    SQLXMLFactory("org.seasar.doma.SQLXMLFactory", "java.sql.SQLXML"),
    ;

    fun matchFactoryAnnotation(fqName: String): Boolean = this.fqdn == fqName
}
