/*
 * Copyright [2018] [AukletAPM]
 *
 *     Author: Eric Xu
 *     Email: eric.xu@aukletapm.com
 *     WebURL: https://github.com/aukletapm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aukletapm.go.spring

import com.mongodb.CommandResult
import org.springframework.data.mongodb.core.MongoTemplate

/**
 *
 * @author Eric Xu
 * @date 24/03/2018
 */
class MongoService(private val mongoTemplate: MongoTemplate) {

    enum class StatusTableLoadMode {
        NORMAL, RECURSION
    }

    fun statusAsMap(path: String? = null, mode: StatusTableLoadMode = StatusTableLoadMode.NORMAL): Map<String, Any> {
        val status = status() ?: return mapOf()

        if (path == null && mode == StatusTableLoadMode.NORMAL) {
            return status.filter {
                it.value !is LinkedHashMap<*, *>
            }
        }

        if (path == null && mode == StatusTableLoadMode.RECURSION) {
            return loadData(status, mode)
        }

        path!!
        val paths = path.split(".").filter { !it.isNullOrBlank() }
        if (paths.isEmpty()) {
            throw IllegalStateException("Invalid path [$path]")
        }
        var currentValue: LinkedHashMap<*, *> = status
        paths.forEach {
            val value = currentValue[it]
            if (value is LinkedHashMap<*, *>) {
                currentValue = value
            } else {
                throw IllegalStateException("The end of path must be an object.")
            }
        }
        return loadData(currentValue, mode)
    }

    fun getStatusValue(pathList: List<String>, mode: StatusTableLoadMode = StatusTableLoadMode.NORMAL): Map<String, Any> {
        val status = status()
        return if (status == null) {
            mapOf()
        } else {
            loadData(status, mode).filter { pathList.contains(it.key) }
        }
    }

    private fun loadData(obj: Any, mode: StatusTableLoadMode = StatusTableLoadMode.NORMAL): Map<String, Any> {
        return loadData(null, obj, mode)
    }

    private fun status(): CommandResult? {
        return mongoTemplate.executeCommand("""
            { serverStatus: 1 }
        """.trimIndent())
    }

    private fun loadData(parent: String?, obj: Any, mode: StatusTableLoadMode = StatusTableLoadMode.NORMAL): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (obj is LinkedHashMap<*, *>) {
            obj.forEach {
                if (it.value is LinkedHashMap<*, *>) {
                    if (mode == StatusTableLoadMode.RECURSION) {
                        val data = loadData(it.key.toString(), it.value)
                        result.putAll(data)
                    }
                } else {
                    var key = it.key.toString()
                    if (parent != null) {
                        key = parent + "." + key
                    }
                    result.put(key, it.value)
                }

            }
        }
        return result
    }


}