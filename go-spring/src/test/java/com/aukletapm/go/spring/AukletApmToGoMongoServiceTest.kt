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
import org.mockito.Matchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.data.mongodb.core.MongoTemplate
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test
import kotlin.test.assertTrue

/**
 * @author Eric Xu
 * @date 27/03/2018
 */
class AukletApmToGoMongoServiceTest {

    private val result = linkedMapOf<Any, Any>(
            "a" to "a",
            "b" to "b",
            "c" to linkedMapOf<Any, Any>(
                    "d" to 1
            )
    )

    @Test(expectedExceptionsMessageRegExp = "The end of path must be an object.", expectedExceptions = [IllegalStateException::class])
    fun test() {
        val mockMongoTemplate = mock(MongoTemplate::class.java)
        val mockResult = mock(CommandResult::class.java)
        val mongoService = MongoService(mockMongoTemplate)

        `when`(mockMongoTemplate.executeCommand(Matchers.anyString())).thenReturn(mockResult)

        `when`(mockResult.forEach { }).then {
            result.entries
        }

        `when`(mockResult[Matchers.anyString()]).then { invocation ->
            result[invocation.arguments[0]]
        }

        `when`((mockResult as LinkedHashMap<*, *>)[Matchers.anyString()]).then { invocation ->
            result[invocation.arguments[0]]
        }

        mongoService.statusAsMap("c.d")
    }


    @Test
    fun testStatusAsMap() {
        val mockMongoTemplate = mock(MongoTemplate::class.java)
        val mockResult = mock(CommandResult::class.java)
        val mongoService = MongoService(mockMongoTemplate)
        `when`(mockMongoTemplate.executeCommand(Matchers.anyString())).thenReturn(mockResult)
        `when`(mockResult.forEach { }).then {
            result.entries
        }

        `when`(mockResult[Matchers.anyString()]).then { invocation ->
            result[invocation.arguments[0]]
        }

        `when`((mockResult as LinkedHashMap<*, *>)[Matchers.anyString()]).then { invocation ->
            result[invocation.arguments[0]]
        }

        var status = mongoService.statusAsMap()
        assertEquals(status.get("a"), "a")
        assertEquals(status.get("b"), "b")
        assertNull(status.get("c.d"))

        status = mongoService.statusAsMap(mode = MongoService.StatusTableLoadMode.RECURSION)
        assertEquals(status.get("a"), "a")
        assertEquals(status.get("b"), "b")
        assertEquals(status.get("c.d"), 1)

        val getStatusValueResult = mongoService.getStatusValue(listOf("a", "b", "c.d"))
        assertEquals(getStatusValueResult["a"], "a")
        assertEquals(getStatusValueResult["b"], "b")
        assertNull(getStatusValueResult["c.d"])

        val getStatusValueResultRecursion = mongoService.getStatusValue(listOf("a", "b", "c.d"), MongoService.StatusTableLoadMode.RECURSION)
        assertEquals(getStatusValueResultRecursion["a"], "a")
        assertEquals(getStatusValueResultRecursion["b"], "b")
        assertEquals(getStatusValueResultRecursion["c.d"], 1)


        val statusCd = mongoService.statusAsMap("c")
        assertEquals(statusCd["d"], 1)
    }

    @Test(expectedExceptions = [java.lang.IllegalStateException::class], expectedExceptionsMessageRegExp = "Invalid path \\[\\]")
    fun emptyPath() {
        val mockMongoTemplate = mock(MongoTemplate::class.java)
        val mockResult = mock(CommandResult::class.java)
        val mongoService = MongoService(mockMongoTemplate)
        `when`(mockMongoTemplate.executeCommand(Matchers.anyString())).thenReturn(mockResult)
        mongoService.statusAsMap("")
    }

    @Test()
    fun statusReturnsNull() {
        val mockMongoTemplate = mock(MongoTemplate::class.java)
        val mongoService = MongoService(mockMongoTemplate)
        `when`(mockMongoTemplate.executeCommand(Matchers.anyString())).thenReturn(null)
        assertTrue(mongoService.getStatusValue(listOf()).isEmpty())
    }

}