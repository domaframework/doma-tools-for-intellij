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
package org.domaframework.doma.intellij.common.helper

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFocusManager

object ActiveProjectHelper {
    val activeProject: Project?
        get() {
            val openProjects: Array<out Project> = ProjectManager.getInstance().openProjects
            var activeProject: Project? = null

            for (project in openProjects) {
                if (IdeFocusManager.getInstance(project).focusOwner != null) {
                    activeProject = project
                    break
                }
            }

            return activeProject
        }
}
