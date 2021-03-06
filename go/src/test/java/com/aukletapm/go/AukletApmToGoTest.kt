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

import com.aukletapm.go.AukletApmToGo.Companion.createInstance
import org.mockito.Mockito.*
import org.testng.annotations.Test
import java.text.SimpleDateFormat
import kotlin.test.*


/**
 * @author Eric Xu
 * @date 13/03/2018
 */
class AukletApmToGoTest {

    val aukletApmToGoService = createInstance("Test Service")


    @Test
    fun testDestroy() {
        val mockComponent = mock(LineChart::class.java)
        `when`(mockComponent.name).thenReturn("test")
        aukletApmToGoService.addComponent(mockComponent)
        verify(mockComponent, atLeastOnce()).init()
        aukletApmToGoService.removeComponent("test")
        verify(mockComponent, atLeastOnce()).destroy()
    }


    @Test(expectedExceptions = [IllegalStateException::class])
    fun indexPageHasNotInitialized() {
        aukletApmToGoService.handle()
    }

    @Test(dependsOnMethods = ["indexPageHasNotInitialized"])
    fun testHandle() {
        assertEquals("Test Service", aukletApmToGoService.serviceName)
        val indexPage = aukletApmToGoService.startIndexPage("Welcome")

                .startList("jvm_arguments", "JVM arguments")
                .setContentLoader {
                    listOf(
                            AukletApmToGo.KeyValue("key1", "value1"),
                            AukletApmToGo.KeyValue("key2", "value2")
                    )
                }
                .endList()

                .startList("will_encounter_exception_when_data_loading")
                .setContentLoader {
                    throw IllegalStateException("some error occured")
                }
                .endList()

                .startPieChart("pie", "Pie")
                .setContentLoader {

                    val dataset1 = mapOf<String, Double>(
                            "Azerbaijan" to 123.1,
                            "Azerbaijan" to 123.1,
                            "Chile" to 13.3,
                            "Finland" to 123.7
                    )

                    val dataset2 = mapOf<String, Double>(
                            "Azerbaijan" to 123.1,
                            "Bahrain" to 12.0,
                            "Ecuador" to 293.123,
                            "Finland" to 123.7
                    )

                    AukletApmToGo.PieChartData.Builder()
                            .dataset("dataset1", dataset1)
                            .dataset("dataset2", dataset2)
                            .data("dataset1", "Cuba", 123.1)
                            .data("Cuba", 123.1)
                            .build()

                }

                .endPieChart()

                .startLineChart("line")
                .formatLabel { SimpleDateFormat("yyyy-MM-dd").format(it) }
                .loadData {
                    listOf()
                }
                .endLineChart()


        indexPage.endPage()
        val page = aukletApmToGoService.handle()
        assertEquals(indexPage, page)
        assertTrue(page.components[0] is AukletApmToGo.AukletApmToGoList)
        val data = page.components[0].load(null)
        checkLoadedData(data)
    }

    @Test(dependsOnMethods = ["testHandle"])
    fun setPieChart() {
        val result = aukletApmToGoService.load("pie", null)
        if (result is AukletApmToGo.PieChartData) {
            assertEquals(3, result.datasets.size)
            assertEquals(6, result.labels.size)
            assertEquals(123.1, result.datasets[0].data[0])
            assertEquals(123.7, result.datasets[1].data[2])
            assertEquals("dataset2", result.datasets[1].label)
        } else {
            assert(false)
        }
    }


    @Test(dependsOnMethods = ["testHandle"])
    fun testLoad() {
        checkLoadedData(aukletApmToGoService.load("jvm_arguments", null))
        val indexPage = aukletApmToGoService.getPage("index")
        assertNotNull(indexPage)
    }

    @Test(dependsOnMethods = ["testHandle"], expectedExceptions = [IllegalStateException::class])
    fun pageIsNotFound() {
        aukletApmToGoService.getPage("invalid page name")
    }

    @Test(dependsOnMethods = ["testHandle"])
    fun handleWithBlankId() {
        val indexPage = aukletApmToGoService.handle("")
        assertEquals(aukletApmToGoService.handle(), indexPage)
        assertEquals(aukletApmToGoService.handle(), aukletApmToGoService.handle("index"))
    }

    @Test(dependsOnMethods = ["testHandle"])
    fun testLoadWithRequest() {
        val request = LoadDataRequest()
        request.items.add(LoadDataRequestItem("jvm_arguments"))
        request.items.add(LoadDataRequestItem("name_not_exists"))
        request.items.add(LoadDataRequestItem("will_encounter_exception_when_data_loading"))
        val response = aukletApmToGoService.load(request)
        val item = response.items.findLast { it.name == "jvm_arguments" }
        assertNotNull(item)

        val nullItem = response.items.findLast { it.name == "name_not_exists" }
        assertNotNull(nullItem)
        assertEquals("Component [name_not_exists] didn't exist", nullItem!!.errorMessage)

        val exceptionItem = response.items.findLast { it.name == "will_encounter_exception_when_data_loading" }
        println(exceptionItem)
    }

    private fun checkLoadedData(data: Any) {
        if (data is List<*>) {
            assertEquals(2, data.size)
            val value1 = data[0]
            if (value1 is AukletApmToGo.KeyValue) {
                assertEquals("key1", value1.key)
                assertEquals("value1", value1.value)
            } else {
                assertFalse(true, "Wrong value type")
            }

            val value2 = data[1]
            if (value2 is AukletApmToGo.KeyValue) {
                assertEquals("key2", value2.key)
                assertEquals("value2", value2.value)
            } else {
                assertFalse(true, "Wrong value type")
            }

        } else {
            assertFalse(true, "Wrong returned data")
        }
    }

    @Test
    fun addComponents() {
        val component1 = object : Component("test", "test") {
            override fun load(args: Any?): Any {
                return listOf<String>();
            }
        }

        val component2 = object : Component("test", "test") {
            override fun load(args: Any?): Any {
                return listOf<String>();
            }
        }

        val component3 = object : Component("test1", "test") {
            override fun load(args: Any?): Any {
                return listOf<String>();
            }
        }

        val aukletApmTogo = AukletApmToGo.createInstance("test")
                .startIndexPage("Index")
                .addComponents(listOf(component1, component2, component3))
                .endPage()

        val result = aukletApmTogo.handle()
        assertEquals(2, result.components.size)
    }


}