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

package com.aukletapm.go

import com.sun.management.UnixOperatingSystemMXBean
import org.mockito.Mockito.mock
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.lang.management.ManagementFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


/**
 * @author Eric Xu
 * @date 20/03/2018
 */
class LineChartTest {

    private lateinit var lineChart: LineChart
    private val os = ManagementFactory.getOperatingSystemMXBean()

    @BeforeMethod
    fun init() {
        lineChart = LineChart.Builder("test", mock(AukletApmToGo.Page::class.java))
                .description("Test")
                .interval(2)
                .intervalUnit(TimeUnit.NANOSECONDS)
                .accuracy(ChronoUnit.MINUTES)
                .maxSize(10)
                .formatLabel {
                    SimpleDateFormat("yyyy-MM-dd HH:mm").format(it)
                }
                .loadData {
                    if (os is UnixOperatingSystemMXBean) {
                        listOf(
                                LineChart.LoadData("processCpuLoad", os.processCpuLoad),
                                LineChart.LoadData("systemCpuLoad", os.systemCpuLoad)
                        )
                    } else {
                        listOf()
                    }
                }
                .build()
    }

    @Test
    fun labelMerge() {
        lineChart.update("a", 10.0, Timestamp.valueOf(LocalDateTime.of(2000, 12, 12, 12, 12, 12)).time)
        lineChart.update("b", 11.0, Timestamp.valueOf(LocalDateTime.of(2000, 12, 12, 12, 12, 12)).time)
        val result = lineChart.load(null)
        if (result is LineChart.LineChartData) {
            assertEquals(1, result.labels.size)
            assertEquals("2000-12-12 12:12", result.labels[0])
            assertEquals(2, result.datasets.size)
            assertEquals("a", result.datasets[0].label)
            assertEquals("b", result.datasets[1].label)
        } else {
            assert(false)
        }
    }

    @Test
    fun labelNotMerge() {
        lineChart.update("a", 10.0, Timestamp.valueOf(LocalDateTime.of(2000, 12, 12, 12, 12, 12)).time)
        lineChart.update("b", 11.0, Timestamp.valueOf(LocalDateTime.of(2000, 12, 12, 12, 13, 12)).time)
        lineChart.update("b", 13.0, Timestamp.valueOf(LocalDateTime.of(2000, 12, 12, 12, 13, 12)).time)
        val result = lineChart.load(null)
        if (result is LineChart.LineChartData) {
            assertEquals(2, result.labels.size)
            assertEquals("2000-12-12 12:12", result.labels[0])
            assertEquals("2000-12-12 12:13", result.labels[1])
            assertEquals(2, result.datasets.size)
            assertEquals("a", result.datasets[0].label)
            assertEquals("b", result.datasets[1].label)
            assertEquals(10.0, result.datasets[0].data[0])
            assertEquals(13.0, result.datasets[1].data[0])
        } else {
            assert(false)
        }
    }

    @Test
    fun testGenerateLabel() {
        for (i in 1..12)
            lineChart.update("processCpuLoad", 13.0, Timestamp.valueOf(LocalDateTime.of(2000, 12, 12, 12, i, 12)).time)
        val result = lineChart.load(null)
        if (result is LineChart.LineChartData) {
            assertEquals(10, result.labels.size)
        } else {
            assert(false)
        }
    }

    @Test
    fun testStart() {
        lineChart.init()
        Thread.sleep(100)
        lineChart.destroy()
    }


    @Test
    fun testValueMode() {
        val lineChart = LineChart
                .newBuilder("test")
                .valueMode(LineChart.ValueMode.DIFFERENCE)
                .loadData {
                    listOf()
                }.build()

        lineChart.update("", 10.0, Date.from(LocalDateTime.parse("2007-12-03T10:15:30").atZone(ZoneId.systemDefault()).toInstant()).time)
        lineChart.update("", 12.0, Date.from(LocalDateTime.parse("2007-12-03T10:15:30").atZone(ZoneId.systemDefault()).toInstant()).time)
        lineChart.update("", 12.4, Date.from(LocalDateTime.parse("2007-12-03T10:16:30").atZone(ZoneId.systemDefault()).toInstant()).time)

        val result = lineChart.load(null)
        if (result is LineChart.LineChartData) {
            org.testng.Assert.assertEquals(2.0, result.datasets.get(0).data[0], 0.001)
            org.testng.Assert.assertEquals(0.4, result.datasets.get(0).data[1], 0.001)
        }

    }


}