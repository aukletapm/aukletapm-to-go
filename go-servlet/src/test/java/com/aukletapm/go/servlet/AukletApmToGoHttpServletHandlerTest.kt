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

package com.aukletapm.go.servlet

import com.aukletapm.go.AukletApmToGo
import com.aukletapm.go.AukletApmToGo.Companion.createInstance
import com.jayway.jsonpath.JsonPath
import org.mockito.Matchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * @author Eric Xu
 * @date 27/02/2018
 */
class AukletApmToGoHttpServletHandlerTest {

    private lateinit var handler: AukletApmToGoHttpServletHandler

    @BeforeTest
    fun init() {
        val service = createInstance("Test")
        service.startIndexPage("Index")
                .startList("test_list", "Test list")
                .setContentLoader {
                    listOf(AukletApmToGo.KeyValue("key1", "value1"))
                }
                .endList()

                .startPieChart("pie")
                .setContentLoader {
                    AukletApmToGo.PieChartData.Builder().data("ds1", "a", 1.0).data("ds1", "b", 2.0).build()
                }

                .endPieChart()

                .endPage()
        handler = AukletApmToGoHttpServletHandler.Builder().enableCors().debug().service(service).build()
    }


    @Test
    fun whenEncounteringUnknownException() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.inputStream).thenThrow(RuntimeException())
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            println(it)
            assertEquals(true, JsonPath.parse(it).read<Boolean>("error"))
            assertEquals("java.lang.RuntimeException", JsonPath.parse(it).read<String>("errorMessage"))
        }
    }

    private fun handle(request: HttpServletRequest, response: HttpServletResponse, function: (result: String) -> Unit) {

        val mockOut = mock(ServletOutputStream::class.java)
        `when`(response.outputStream).thenReturn(mockOut)

        ByteArrayOutputStream().use { out ->

            `when`(mockOut.write(Matchers.anyObject(), Matchers.anyInt(), Matchers.anyInt())).then { invocation ->
                out.write(invocation.arguments[0] as ByteArray, invocation.arguments[1] as Int, invocation.arguments[2] as Int)
            }
            `when`(mockOut.write(Matchers.any(ByteArray::class.java))).then { invocation ->
                out.write(invocation.arguments[0] as ByteArray)
            }
            `when`(mockOut.write(Matchers.anyInt())).then { invocation ->
                out.write(invocation.arguments[0] as Int)
            }

            handler.handle(request, response)

            ByteArrayInputStream(out.toByteArray()).use {
                GZIPInputStream(it).use {
                    InputStreamReader(it, "UTF-8").use {
                        function(it.readText())
                    }
                }
            }
        }


    }

    @Test
    fun testHandle() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")

        handle(request, response) {
            verify(response, never()).addHeader("Access-Control-Allow-Origin", "*")
            verify(response, never()).addHeader("Access-Control-Allow-Methods", "*")
            verify(response, atLeast(1)).contentType = "text/html"
        }

    }


    @Test
    fun withoutBody() {
        val request = Mockito.mock(HttpServletRequest::class.java)
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            verify(response, atLeastOnce()).addHeader("Access-Control-Allow-Origin", "*")
            verify(response, atLeastOnce()).addHeader("Access-Control-Allow-Methods", "*")
            verify(response, atLeast(1)).contentType = "application/json;charset=UTF-8"

            assertEquals(true, JsonPath.parse(it).read<Boolean>("error"))
        }

    }

    @Test
    fun invalidJsonBody() {
        val content = """{
           type
            }""".trimIndent()
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.inputStream).thenReturn(MockServletInputStream(content))
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            verify(response, atLeast(1)).contentType = "application/json;charset=UTF-8"
            val error = JsonPath.parse(it).read<Boolean>("error")
            assertEquals(true, error)
        }
    }

    @Test
    fun invalidRequestType() {
        val content = """
            {
              "type" : 99,
              "c":"",
              "loadDataRequest": {
                "items" : [
                {

                "name" : "d3",
                "args" : {
                  "a":"a",
                  "b": 1
                }
              }
                ]
              }
            }
        """.trimIndent()
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.inputStream).thenReturn(MockServletInputStream(content))
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            verify(response, atLeast(1)).contentType = "application/json;charset=UTF-8"
            val errorMessage = JsonPath.parse(it).read<String>("errorMessage")
            assertEquals("Invalid type 99", errorMessage)
            val error = JsonPath.parse(it).read<Boolean>("error")
            assertEquals(true, error)
        }
    }

    @Test
    fun returnJson() {
        val content = """
            {
              "type" : 1,
              "c":"",
              "loadDataRequest": {
                "items" : [
                {

                "name" : "d3",
                "args" : {
                  "a":"a",
                  "b": 1
                }
              }
                ]
              }
            }
        """.trimIndent()
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.inputStream).thenReturn(MockServletInputStream(content))
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            verify(response, atLeast(1)).contentType = "application/json;charset=UTF-8"
        }

    }

    @Test(dependsOnMethods = ["testHandle"])
    fun getIndexPageContent() {
        val content = handler.getIndexPageContent()
        assertNotNull(content)
    }

    @Test
    fun load() {
        val content = """
            {
              "type" : 2,
              "c":"",
              "loadDataRequest": {
                "items" : [
                  {
                    "name" : "test_list",
                    "args" : {
                      "a":"a",
                      "b": 1
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.inputStream).thenReturn(MockServletInputStream(content))
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            verify(response, atLeast(1)).contentType = "application/json;charset=UTF-8"
            val name = JsonPath.parse(it).read<String>("loadResponse.items[0].name")
            assertEquals("test_list", name)

            val key = JsonPath.parse(it).read<String>("loadResponse.items[0].data[0].key")
            assertEquals("key1", key)

            val value = JsonPath.parse(it).read<String>("loadResponse.items[0].data[0].value")
            assertEquals("value1", value)
        }

    }

    @Test
    fun loadPieChart() {
        val content = """
            {
              "type" : 2,
              "c":"",
              "loadDataRequest": {
                "items" : [
                  {
                    "name" : "pie",
                    "args" : {
                      "a":"a",
                      "b": 1
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        val request = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.inputStream).thenReturn(MockServletInputStream(content))
        val response = Mockito.mock(HttpServletResponse::class.java)
        `when`(request.getHeader("Accept")).thenReturn("application/json")

        handle(request, response) {
            val name = JsonPath.parse(it).read<String>("loadResponse.items[0].name")
            assertEquals("pie", name)
        }

    }

}