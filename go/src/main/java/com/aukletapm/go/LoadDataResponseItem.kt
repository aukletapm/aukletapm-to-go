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

/**
 *
 * @author Eric Xu
 * @date 20/02/2018
 */
class LoadDataResponseItem(
        var name: String? = null,
        var data: Any? = null,
        var error: Boolean = false,
        var errorMessage: String? = null
) {

    override fun toString(): String {
        return "LoadDataResponseItem(name=$name, data=$data, error=$error, errorMessage=$errorMessage)"
    }
}