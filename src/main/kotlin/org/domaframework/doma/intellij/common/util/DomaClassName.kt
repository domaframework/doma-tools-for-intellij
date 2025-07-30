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
package org.domaframework.doma.intellij.common.util

enum class DomaClassName(
    val className: String,
) {
    OPTIONAL("java.util.Optional"),
    OPTIONAL_INT("java.util.OptionalInt"),
    OPTIONAL_DOUBLE("java.util.OptionalDouble"),
    OPTIONAL_LONG("java.util.OptionalLong"),

    INTEGER("java.lang.Integer"),
    DOUBLE("java.lang.Double"),
    LONG("java.lang.Long"),

    MAP("java.util.Map"),
    LIST("java.util.List"),
    ITERABLE("java.lang.Iterable"),

    DOMAIN("org.seasar.doma.Domain"),
    BI_FUNCTION("java.util.function.BiFunction"),
    CONFIG("org.seasar.doma.jdbc.Config"),
    PREPARED_SQL("org.seasar.doma.jdbc.PreparedSql"),
    VOID("java.lang.Void"),
    RETURNING("org.seasar.doma.Returning"),
    REFERENCE("org.seasar.doma.jdbc.Reference"),
    SELECT_OPTIONS("org.seasar.doma.jdbc.SelectOptions"),

    STRING("java.lang.String"),
    OBJECT("java.lang.Object"),
    BIG_DECIMAL("java.math.BigDecimal"),
    BIG_INTEGER("java.math.BigInteger"),
    LOCAL_DATE("java.time.LocalDate"),
    LOCAL_TIME("java.time.LocalTime"),
    LOCAL_DATE_TIME("java.time.LocalDateTime"),
    SQL_DATE("java.sql.Date"),
    SQL_TIME("java.sql.Time"),
    SQL_TIMESTAMP("java.sql.Timestamp"),
    SQL_ARRAY("java.sql.Array"),
    SQL_BLOB("java.sql.Blob"),
    SQL_CLOB("java.sql.Clob"),
    SQL_XML("java.sql.SQLXML"),
    UTIL_DATE("java.util.Date"),

    BYTE("java.lang.Byte"),
    SHORT("java.lang.Short"),
    FLOAT("java.lang.Float"),
    BOOLEAN("java.lang.Boolean"),

    JAVA_FUNCTION("java.util.function.Function"),
    JAVA_COLLECTOR("java.util.stream.Collector"),
    JAVA_STREAM("java.util.stream.Stream"),
    SELECT_TYPE("org.seasar.doma.SelectType"),

    ENTITY("org.seasar.doma.Entity"),
    DATATYPE("org.seasar.doma.DataType"),
    ;

    fun isTargetClassNameStartsWith(paramTypeCanonicalNames: String): Boolean = paramTypeCanonicalNames.startsWith(this.className)

    fun getGenericParamCanonicalText(vararg genericParas: String): String = "${this.className}<${genericParas.joinToString(", ")}>"

    companion object {
        fun isOptionalWrapperType(paramTypeCanonicalName: String): Boolean =
            paramTypeCanonicalName in
                listOf(
                    OPTIONAL_INT.className,
                    OPTIONAL_DOUBLE.className,
                    OPTIONAL_LONG.className,
                )
    }
}
