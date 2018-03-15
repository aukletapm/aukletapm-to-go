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

import com.aukletapm.go.LoadDataRequest

/**
 * HTTP request body
 *
 * @param type  Query type, 1 means access component, 2 means load component data
 * @param c Compnent name. It is required when the type is 1
 * @param loadDataRequest It is required when the type is 2
 * @param version Client version
 *
 * @author Eric Xu
 * @date 27/02/2018
 */
class Query(
        var type: Int? = null,
        var c: String? = null,
        var version: String? = null,
        var loadDataRequest: LoadDataRequest? = null
)