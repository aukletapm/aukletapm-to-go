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

import com.aukletapm.go.Component
import com.aukletapm.go.LineChart
import com.aukletapm.go.Module
import com.sun.management.UnixOperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.nio.file.FileSystems
import java.nio.file.Files


/**
 *
 * @author Eric Xu
 * @date 31/03/2018
 */
class OsModule : Module {

    override fun components(): List<Component> {

        val spaceLineChart = LineChart.newBuilder("os_file")
                .description("Unallocated Space(MB)")
                .loadData {
                    FileSystems.getDefault().rootDirectories.map { Files.getFileStore(it) }.map {
                        LineChart.LoadData(it.name(), it.unallocatedSpace / 1024 / 1024 * 1.0)
                    }
                }
                .build()


        val cpuLineChart = LineChart.newBuilder("os_cpu")
                .description("CPU %")
                .loadData {
                    val os = ManagementFactory.getOperatingSystemMXBean()
                    if (os is UnixOperatingSystemMXBean) {
                        listOf(
                                LineChart.LoadData("Process Cpu Load", os.processCpuLoad * 100),
                                LineChart.LoadData("System Cpu Load", os.systemCpuLoad * 100)
                        )
                    } else {
                        listOf()
                    }
                }
                .build()

        return listOf(spaceLineChart, cpuLineChart)
    }
}