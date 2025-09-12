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
import kotlin.test.Ignore

/**
 * Test class for annotation option parameter inspection.
 * Tests include/exclude options with parent class properties.
 */
@Ignore
class AnnotationOptionParameterInspectionTest : DomaSqlTest() {
    /**
     * Since error highlight tags for annotation options cannot be set in the test data, verify manually.
     * There are no automated test cases, so perform manual checks using the following as a reference.
     *
     * Here is an updated summary of the test case coverage based on the revised method documentation. This can be used as a test case overview document.
     * Relation: [AnnotationOptionTestInValidDao]
     * - Error check when specifying fields not defined in the parameter type with `include` option.
     * - Error check when specifying fields not defined in the parameter type with `exclude` option.
     * - Error check for specifying fields not defined in immutable Entity with `MultiInsert` (also for fields not defined in parameter type).
     * - Error check for specifying fields not defined in mutable Entity with `MultiInsert`.
     * - Error check for specifying fields not defined in the parameter type with batch annotations.
     * - Error when ending with an embedded property.
     * - Error when specifying incorrect properties in an embedded class.
     * - Error check for invalid field specification in `Returning` option.
     * - Error check for invalid field specification in `Returning` option for mutable Entity.
     * - Error check for specifying fields not defined in embedded property.
     * - Error when specifying further properties from a primitive type.
     * - Error check for specifying parent class properties in subclass with `@Entity`.
     * - Error check for specifying parent class properties in subclass without `@Entity`.
     * - Error check for specifying fields from a parent class that is not an Entity.
     */
}
