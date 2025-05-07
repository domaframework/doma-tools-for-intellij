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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.event.InputEvent

open class PluginLoggerUtil {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        fun countLoggingByAction(
            className: String,
            actionName: String,
            inputEvent: InputEvent?,
            start: Long,
        ) {
            logging(className, actionName, inputEvent?.javaClass?.simpleName, start)
        }

        fun countLogging(
            className: String,
            actionName: String,
            inputName: String,
            start: Long,
        ) {
            logging(className, actionName, inputName, start)
        }

        private fun logging(
            className: String,
            actionName: String,
            inputName: String?,
            start: Long,
        ) {
            val duration = (System.nanoTime() - start) / 1_000_000F
            logger.info(
                "\"{}\",\"{}\",\"{}\",{}",
                className,
                inputName ?: "null",
                actionName,
                duration,
            )
        }
    }
}
