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

import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Matchers.anyString
import org.mockito.Mockito
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

/**
 * @author Eric Xu
 * @date 27/03/2018
 */
class MySqlServiceTest {

    private lateinit var mySqlService: MySqlService

    @BeforeTest
    fun init() {
        val mockJdbcTemplate = Mockito.mock(JdbcTemplate::class.java)
        Mockito.`when`(mockJdbcTemplate.queryForObject(anyString(), any(RowMapper::class.java)))
                .thenReturn(MySqlService.Status("10"))
        mySqlService = MySqlService(mockJdbcTemplate)

    }

    @Test
    fun testComUpdate() {
        assertEquals(mySqlService.comUpdate(), 10)
    }

    @Test
    fun testComSelect() {
        assertEquals(mySqlService.comSelect(), 10)
    }

    @Test
    fun testComInsert() {
        assertEquals(mySqlService.comInsert(), 10)
    }

    @Test
    fun testComDelete() {
        assertEquals(mySqlService.comDelete(), 10)
    }

    @Test
    fun testComRollback() {
        assertEquals(mySqlService.comRollback(), 10)
    }

    @Test
    fun testBytesReceived() {
        assertEquals(mySqlService.bytesReceived(), 10)
    }

    @Test
    fun testBytesSent() {
        assertEquals(mySqlService.bytesSent(), 10)
    }

    @Test
    fun testSlowQueries() {
        assertEquals(mySqlService.slowQueries(), 10)
    }

}