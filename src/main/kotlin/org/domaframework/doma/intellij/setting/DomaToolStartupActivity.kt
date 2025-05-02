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
package org.domaframework.doma.intellij.setting

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.domaframework.doma.intellij.common.util.PluginUtil

class DomaToolStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        System.setProperty("org.domaframework.doma.intellij.log.path", PathManager.getLogPath())
        System.setProperty(
            "org.domaframework.doma.intellij.plugin.version",
            PluginUtil.getVersion(),
        )

        println("PluginVersion: ${System.getProperty("org.domaframework.doma.intellij.plugin.version")} ")
    }
}
