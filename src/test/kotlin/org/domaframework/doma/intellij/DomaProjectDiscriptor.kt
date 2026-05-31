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
@file:Suppress("ktlint:standard:filename")
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

package org.domaframework.doma.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor

class DomaProjectDescriptor : DefaultLightProjectDescriptor() {
    /**
     * Reuse a single real JDK created from the running [java.home] across every
     * test class. The entities under test reference classes such as
     * java.time.LocalDate and java.util.Optional that the lightweight mock JDK
     * does not provide, so a real JDK is required. Creating it scans the JDK
     * home, which is expensive, so the result is cached and shared instead of
     * being rebuilt in every test's setUp.
     */
    override fun getSdk(): Sdk = sharedJdk

    override fun configureModule(
        module: Module,
        model: ModifiableRootModel,
        contentEntry: ContentEntry,
    ) {
        IdeaTestUtil.setModuleLanguageLevel(module, LanguageLevel.JDK_21)
    }

    companion object {
        private val sharedJdk: Sdk by lazy {
            ApplicationManager.getApplication().runWriteAction<Sdk> {
                JavaSdk.getInstance().createJdk(
                    "Doma Test JDK",
                    System.getProperty("java.home"),
                    false,
                )
            }
        }
    }
}
