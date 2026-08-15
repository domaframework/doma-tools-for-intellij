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

const val PLUGIN_VERSION = "2.5.2-beta"

open class PluginUtil {
    companion object {
        /**
         * IntelliJ 2026.2 marked every `PluginManager` lookup of a plugin descriptor as an internal
         * API, so the version is read from [PLUGIN_VERSION]. The release build keeps that constant in
         * sync with the `pluginVersion` Gradle property (see `replaceVersionInPluginUtil`).
         */
        fun getVersion(): String = PLUGIN_VERSION
    }
}
