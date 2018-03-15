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
import com.aukletapm.go.LoadDataResponse

/**
 *
 * @author Eric Xu
 * @date 27/02/2018
 */
class Response(
        val serviceName: String = "",
        var error: Boolean = false,
        var errorMessage: String? = null,
        var component: AukletApmToGo.Page? = null,
        var loadResponse: LoadDataResponse? = null
)