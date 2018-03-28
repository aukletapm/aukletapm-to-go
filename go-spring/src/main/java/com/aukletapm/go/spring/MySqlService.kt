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

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

/**
 *
 * @author Eric Xu
 * @date 16/03/2018
 */
class MySqlService(private val jdbcTemplate: JdbcTemplate) {

    private val mapper: RowMapper<Status> = RowMapper<Status> { rs, _ -> Status(rs!!.getString("Value")) }

    class Status(val value: String)

    fun comUpdate(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Com_update"""", mapper).value.toLong()

    fun comSelect(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Com_select"""", mapper).value.toLong()

    fun comInsert(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Com_insert"""", mapper).value.toLong()

    fun comDelete(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Com_delete"""", mapper).value.toLong()

    fun comRollback(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Com_rollback"""", mapper).value.toLong()

    fun bytesReceived(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Bytes_received"""", mapper).value.toLong()

    fun bytesSent(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Bytes_sent"""", mapper).value.toLong()

    fun slowQueries(): Long = jdbcTemplate.queryForObject("""SHOW GLOBAL STATUS LIKE "Slow_queries"""", mapper).value.toLong()

}