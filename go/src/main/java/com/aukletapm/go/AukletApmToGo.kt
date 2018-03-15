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

import org.slf4j.LoggerFactory

/**
 *
 * @author Eric Xu
 * @date 13/03/2018
 */
class AukletApmToGo {

    private val log = LoggerFactory.getLogger("com.aukletapm.go.service")

    val serviceName: String
    private val components = mutableMapOf<String, Component>()
    private val pages = mutableMapOf<String, Page>()

    constructor(serviceName: String) {
        this.serviceName = serviceName
    }

    fun addComponent(component: Component) {
        components.put(component.name, component)
    }

    companion object {
        fun createInstance(serviceName: String): AukletApmToGo {
            return AukletApmToGo(serviceName)
        }
    }

    private var indexPage: Page? = null

    fun handle(id: String? = null): Page {
        if (id.isNullOrBlank()) {
            return indexPage ?: throw IllegalStateException("Index page is not found")
        }
        return getPage(id!!)
    }

    fun getPage(name: String): Page {
        val page = pages.get(name) ?: throw  IllegalStateException("Page is not found by specific name $name")
        return page
    }

    fun startIndexPage(title: String): Page {
        val page = Page("index", this)
        addPage(page)
        page.title = title
        return page
    }

    fun addPage(page: AukletApmToGo.Page) {
        pages.put(page.name, page)
    }

    class Page {
        private val aukletApmToGo: AukletApmToGo
        val name: String

        var title: String? = null

        val components: MutableList<Component> = mutableListOf()

        constructor(name: String, aukletApmToGo: AukletApmToGo) {
            this.aukletApmToGo = aukletApmToGo
            this.name = name
        }

        fun addComponent(component: Component) {
            components.add(component)
            aukletApmToGo.addComponent(component)
        }

        fun startList(name: String, description: String? = null): AukletApmToGoList {
            return AukletApmToGoList(name, description, this)
        }

        fun endPage(): AukletApmToGo {
            if ("index" == name) {
                aukletApmToGo.indexPage = this
            }
            aukletApmToGo.addPage(this)
            return aukletApmToGo
        }
    }


    abstract class Component(val name: String, val type: String, val description: String? = null) {
        abstract fun load(args: Any?): Any
    }

    class AukletApmToGoList(name: String, description: String?, page: Page) : Component(name, "List", description) {

        private val page: Page = page

        private var contentLoader: ((Any?) -> Any)? = null

        fun setContentLoader(contentLoader: (Any?) -> Any): AukletApmToGoList {
            this.contentLoader = contentLoader
            return this
        }

        override fun load(args: Any?): Any {
            val loader = this.contentLoader ?: throw NullPointerException("Content loader is null.")
            return loader(args)
        }

        fun endList(): Page {
            page.addComponent(this)
            return page
        }

    }

    abstract class ListItem(val type: String)

    class KeyValue(val key: String, val value: String) : ListItem("key_value")

    fun getComponent(componentName: String): Component {
        return components.get(componentName) ?: throw ComponentNotFoundException(componentName)
    }

    fun load(componentName: String, args: Any?): Any {
        return getComponent(componentName).load(args)
    }

    fun load(request: LoadDataRequest): LoadDataResponse {
        val loadDataResponse = LoadDataResponse()
        loadDataResponse.items = request.items.map {
            val response = LoadDataResponseItem()
            response.name = it.name
            try {
                val component = getComponent(checkNotNull(it.name, { "Item name was null." }))
                response.data = component.load(it.args)
            } catch (e: ComponentNotFoundException) {
                log.warn("Component {} is not found.", e.name)
                response.error = true
                response.errorMessage = e.message
            } catch (e: Exception) {
                response.error = true
                response.errorMessage = e.message
                log.error("Exception while loading data of component.", e)
            }
            response
        }
        return loadDataResponse
    }
}