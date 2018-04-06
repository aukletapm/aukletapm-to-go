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

package com.aukletapm.go.module

import com.aukletapm.go.AukletApmToGo
import com.aukletapm.go.Component
import com.aukletapm.go.LineChart
import com.aukletapm.go.Module
import java.lang.management.ManagementFactory


/**
 *
 * @author Eric Xu
 * @date 31/03/2018
 */
class JvmModule : Module {
    override fun components(): List<Component> {

        val threadMXBean = ManagementFactory.getThreadMXBean()
        val threadCountLineChart = LineChart.newBuilder("jvm_thread_count")
                .description("Thread Count")
                .loadData {
                    listOf(LineChart.LoadData("Thread Count", threadMXBean.threadCount.toDouble()))
                }
                .build()

        val heapLineChart = LineChart.newBuilder("jvm_heap")
                .description("Heap(MB)")
                .loadData {
                    listOf(
                            LineChart.LoadData("Total Memory", Runtime.getRuntime().totalMemory().toDouble() / 1000 / 1000),
                            LineChart.LoadData("Max Memory", Runtime.getRuntime().maxMemory().toDouble() / 1000 / 1000),
                            LineChart.LoadData("Free Memory", Runtime.getRuntime().freeMemory().toDouble() / 1000 / 1000)
                    )
                }
                .build()

        val aukletApmToGoList = AukletApmToGo.AukletApmToGoList("jvm_system_properties", "System Properties").setContentLoader {
            System.getProperties().map {
                AukletApmToGo.KeyValue(it.key.toString(), it.value.toString())
            }
        }

        return listOf(threadCountLineChart, heapLineChart, aukletApmToGoList)
    }
}