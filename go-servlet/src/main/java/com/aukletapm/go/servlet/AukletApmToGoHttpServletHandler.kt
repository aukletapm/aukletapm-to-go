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
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * @author Eric Xu
 * @date 28/01/2018
 */
class AukletApmToGoHttpServletHandler {

    private val log = LoggerFactory.getLogger(AukletApmToGoHttpServletHandler::class.java)
    private lateinit var service: com.aukletapm.go.AukletApmToGo
    private var enableCors: Boolean = false
    private lateinit var mapper: ObjectMapper


    fun handle(request: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        val accept = request.getHeader("Accept")
        if (accept == null || accept.split(",").contains("text/html")) {
            renderIndexPage(httpServletResponse)
            return
        }
        if (enableCors) {
            httpServletResponse.addHeader("Access-Control-Allow-Origin", "*")
            httpServletResponse.addHeader("Access-Control-Allow-Methods", "*")
            httpServletResponse.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
        }
        val result = try {
            val query = mapper.readValue(request.inputStream, Query::class.java)
            log.debug("client version ${query.version}")
            if (1 == query.type) {
                mapper.writeValueAsString(Response(serviceName = service.serviceName, component = service.handle(query.c)))
            } else if (2 == query.type) {
                mapper.writeValueAsString(Response(loadResponse = service.load(checkNotNull(query.loadDataRequest, { "loadDataRequest was null" }))))
            } else {
                throw IllegalStateException("Invalid type ${query.type}")
            }
        } catch (e: JsonMappingException) {
            log.error("Exception while json mapping", e)
            mapper.writeValueAsString(Response(error = true, errorMessage = "Invalid request body"))
        } catch (e: Exception) {
            log.error("System encountered an error.", e)
            var errorMessage = e.message
            if (errorMessage == null) {
                errorMessage = e.toString()
            }
            mapper.writeValueAsString(Response(error = true, errorMessage = errorMessage))
        }

        write(httpServletResponse, result, "application/json;charset=UTF-8")
    }

    fun renderIndexPage(httpServletResponse: HttpServletResponse) {
        write(httpServletResponse, getIndexPageContent(), "text/html")
    }

    fun getIndexPageContent(): String {
        val result = StringBuilder()
        val resource = this.javaClass.getResourceAsStream("/index.html")
        resource.use({ inputStream ->
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                result.append(line)
                line = bufferedReader.readLine()
            }
            inputStream.close()
        })
        return result.toString()
    }

    fun write(httpServletResponse: HttpServletResponse, content: String, contentType: String) {
        httpServletResponse.contentType = contentType
        httpServletResponse.status = HttpServletResponse.SC_OK;
        httpServletResponse.writer.write(content);
        httpServletResponse.writer.flush();
        httpServletResponse.writer.close();
    }

    class Builder {
        private var service: AukletApmToGo? = null
        private var enableCors = false
        private var debug = false

        fun service(service: AukletApmToGo): Builder {
            this.service = service
            return this
        }

        fun enableCors(): Builder {
            enableCors = true
            return this
        }

        fun build(): AukletApmToGoHttpServletHandler {
            val mapper = ObjectMapper()
            if (debug) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            val handler = AukletApmToGoHttpServletHandler()
            handler.service = checkNotNull(service, { "Server was null" })
            handler.enableCors = enableCors
            handler.mapper = mapper
            return handler
        }

        fun debug(): Builder {
            debug = true
            return this
        }
    }
}