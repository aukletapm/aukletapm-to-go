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
import com.aukletapm.go.LineChart
import com.aukletapm.go.LoadDataRequest
import com.mongodb.CommandResult
import org.mockito.Matchers
import org.mockito.Mockito
import org.springframework.data.mongodb.core.MongoTemplate
import org.testng.annotations.Test

/**
 * @author Eric Xu
 * @date 27/03/2018
 */
class AukletApmToGoMongoTest {
    private val result = linkedMapOf<Any, Any>(
            "network" to linkedMapOf<Any, Any>(
                    "bytesIn" to 1,
                    "bytesOut" to "bbb"
            ),
            "connections" to linkedMapOf<Any, Any>(
                    "current" to 3,
                    "available" to "aaa"
            )
    )

    @Test
    fun test() {

        val mockMongoTemplate = Mockito.mock(MongoTemplate::class.java)
        val mockResult = Mockito.mock(CommandResult::class.java)
        val mongoService = MongoService(mockMongoTemplate)

        Mockito.`when`(mockMongoTemplate.executeCommand(Matchers.anyString())).thenReturn(mockResult)

        Mockito.`when`(mockResult.forEach { }).then {
            result.entries
        }

        Mockito.`when`(mockResult[Matchers.anyString()]).then { invocation ->
            result[invocation.arguments[0]]
        }

        Mockito.`when`((mockResult as LinkedHashMap<*, *>)[Matchers.anyString()]).then { invocation ->
            result[invocation.arguments[0]]
        }


        val components = AukletApmToGoMongo.newComponentsBuilder(mockMongoTemplate)
                .addStatusTable("mongoStatus", "Mongo Status")
                .addLineChart("mongoNetwork1", "Mongo Network 1", LineChart.ValueMode.DIFFERENCE, "network.bytesIn", "network.bytesOut")
                .addLineChart("mongoNetwork", "Mongo Network", "network.bytesIn", "network.bytesOut")
                .addPieChart("mongoConnections", "Mongo Connections", "connections.current", "connections.available")
                .build()

        val aukletApmToGo = AukletApmToGo
                .createInstance("")
                .startIndexPage("")
                .addComponents(components)
                .endPage()
        val request = LoadDataRequest()
        request
                .addComponent("mongoStatus")
                .addComponent("mongoNetwork")
                .addComponent("mongoConnections")

        Thread.sleep(2000)
        aukletApmToGo.load(request)
    }

}
