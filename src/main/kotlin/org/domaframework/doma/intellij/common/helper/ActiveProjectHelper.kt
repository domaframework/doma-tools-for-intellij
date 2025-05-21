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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFocusManager

object ActiveProjectHelper {
    private var activeProject: Project? = null

    /**
     * Sets the current active project.
     *
     * @param value the project to set as active, or null to clear
     */
    fun setCurrentActiveProject(value: Project?) {
        activeProject = value
    }

    /**
     * Returns the current active project.
     *
     * If called from the EDT, tries to get the project with UI focus.
     * Otherwise, returns the first open project or the last set active project.
     *
     * @return the current active [Project], or null if none is available
     */
    fun getCurrentActiveProject(): Project? {
        // If EDT, get from focus
        if (ApplicationManager.getApplication().isDispatchThread) {
            getActiveUIProject()?.let { return it }
        }
        // If not EDT, the top of openProjects
        ProjectManager
            .getInstance()
            .openProjects
            .firstOrNull()
            ?.let { return it }

        return activeProject
    }

    /**
     * Returns the project that currently has UI focus, if any.
     *
     * @return the [Project] with UI focus, or null if none
     */
    private fun getActiveUIProject(): Project? {
        val openProjects: Array<out Project> = ProjectManager.getInstance().openProjects
        for (project in openProjects) {
            if (IdeFocusManager.getInstance(project).focusOwner != null) {
                return project
            }
        }
        return null
    }
}
