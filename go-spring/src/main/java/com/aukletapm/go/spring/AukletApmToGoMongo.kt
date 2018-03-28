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

import com.aukletapm.go.AukletApmToGo
import com.aukletapm.go.Component
import com.aukletapm.go.LineChart
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import java.text.SimpleDateFormat

/**
 *
 * @author Eric Xu
 * @date 27/03/2018
 */
class AukletApmToGoMongo {

    companion object {
        fun newComponentsBuilder(mongoTemplate: MongoTemplate): ComponentsBuilder {
            return ComponentsBuilder(mongoTemplate)
        }
    }

    class ComponentsBuilder(mongoTemplate: MongoTemplate) {

        private val log = LoggerFactory.getLogger(AukletApmToGoMongo::class.java)

        private val mongoService = MongoService(mongoTemplate)

        private val components = mutableListOf<Component>()

        @JvmOverloads
        fun addStatusTable(name: String, description: String, path: String? = null, mode: MongoService.StatusTableLoadMode = MongoService.StatusTableLoadMode.RECURSION): ComponentsBuilder {
            components.add(AukletApmToGo.AukletApmToGoList(name, description).setContentLoader {
                mongoService.statusAsMap(path, mode).map {
                    AukletApmToGo.KeyValue(it.key, it.value.toString())
                }
            })
            return this
        }


        fun addLineChart(
                name: String,
                description: String,
                vararg pathList: String
        ): ComponentsBuilder {
            return addLineChart(name, description, LineChart.ValueMode.NORMAL, MongoService.StatusTableLoadMode.RECURSION, *pathList)
        }

        fun addLineChart(
                name: String,
                description: String,
                valueMode: LineChart.ValueMode,
                vararg pathList: String
        ): ComponentsBuilder {
            return addLineChart(name, description, valueMode, MongoService.StatusTableLoadMode.RECURSION, *pathList)
        }

        fun addLineChart(
                name: String,
                description: String,
                valueMode: LineChart.ValueMode = LineChart.ValueMode.NORMAL,
                mode: MongoService.StatusTableLoadMode = MongoService.StatusTableLoadMode.RECURSION,
                vararg pathList: String
        ): ComponentsBuilder {
            val builder = LineChart
                    .newBuilder(name)
                    .description(description)
                    .valueMode(valueMode)
                    .loadData {
                        val values = mongoService.getStatusValue(pathList.toList(), mode)
                        values.map {
                            var data = 0.0
                            if (it.value != null) {
                                try {
                                    data = it.value.toString().toDouble()
                                } catch (e: Exception) {
                                    log.warn("Exception while read the data with ${it.key}", e)
                                }
                            }
                            LineChart.LoadData(it.key, data)
                        }
                    }
            components.add(builder.build())
            return this
        }

        fun addPieChart(name: String,
                        description: String,
                        vararg pathList: String
        ): ComponentsBuilder {
            return addPieChart(name, description, *pathList, mode = MongoService.StatusTableLoadMode.RECURSION);
        }

        fun addPieChart(name: String,
                        description: String,
                        vararg pathList: String,
                        mode: MongoService.StatusTableLoadMode = MongoService.StatusTableLoadMode.RECURSION
        ): ComponentsBuilder {

            components.add(AukletApmToGo.PieChart(name, description).setContentLoader {
                val builder = AukletApmToGo.PieChartData.Builder()
                val values = mongoService.getStatusValue(pathList.toList(), mode)
                values.forEach {
                    try {
                        builder.data(it.key, it.value.toString().toDouble())
                    } catch (e: Exception) {
                        log.warn("Exception while read the data with ${it}", e)
                    }
                }
                builder.build()
            })
            return this
        }

        fun build(): List<Component> {
            return components.toList()
        }

    }

}