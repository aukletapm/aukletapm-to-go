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
        if (component is OnInit) {
            component.init()
        }
    }

    fun removeComponent(name: String) {
        val component = components.get(name)
        components.remove(name)
        if (component != null && component is OnDestroy) {
            component.destroy()
        }
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

    open class Page {
        private val aukletApmToGo: AukletApmToGo
        val name: String

        var title: String? = null

        val components: MutableList<Component> = mutableListOf()

        constructor(name: String, aukletApmToGo: AukletApmToGo) {
            this.aukletApmToGo = aukletApmToGo
            this.name = name
        }

        fun addComponent(component: Component): Page {
            if (!components.contains(component)) {
                components.add(component)
                aukletApmToGo.addComponent(component)
            }
            return this
        }

        fun addComponents(components: List<Component>): Page {
            components.forEach {
                addComponent(it)
            }
            return this
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

        fun startPieChart(name: String, description: String? = null): PieChart {
            return PieChart(name, description, this)
        }

        fun startLineChart(name: String): LineChart.Builder {
            return LineChart.Builder(name, this)
        }

    }

    class PieChartData() {
        val labels = mutableListOf<String>()
        val datasets = mutableListOf<Dataset>()

        class Dataset(var label: String) {
            val data = mutableListOf<Double>()
        }

        class Builder {
            private val labels = mutableListOf<String>()
            private val datasets = mutableListOf<String>()
            private val values = mutableMapOf<String, Double>()

            fun data(labelOfDataset: String, labelOfData: String, value: Double): Builder {
                if (!labels.contains(labelOfData)) {
                    labels.add(labelOfData)
                }
                if (!datasets.contains(labelOfDataset)) {
                    datasets.add(labelOfDataset)
                }
                values.put(labelOfDataset + labelOfData, value)
                return this
            }

            fun data(labelOfData: String, value: Double): Builder {
                return data("default", labelOfData, value)
            }

            fun dataset(labelOfDataset: String, data: Map<String, Double>): Builder {
                data.forEach { t, u -> data(labelOfDataset, t, u) }
                return this
            }

            fun build(): PieChartData {
                val data = PieChartData()
                data.labels.addAll(labels)

                datasets.forEach { label ->
                    val dataset = Dataset(label)

                    labels.forEach { dataLabel ->
                        var value = values.get(label + dataLabel)
                        if (value == null) {
                            value = 0.0
                        }
                        dataset.data.add(value)
                    }
                    data.datasets.add(dataset)
                }

                return data
            }
        }


    }

    class PieChart(name: String, description: String? = null, private val page: Page? = null) : Component(name, "PieChart", description) {
        private var contentLoader: ((Any?) -> PieChartData)? = null

        override fun load(args: Any?): Any {
            val loader = this.contentLoader ?: throw NullPointerException("Content loader is null.")
            return loader(args)
        }

        fun setContentLoader(contentLoader: (Any?) -> PieChartData): PieChart {
            this.contentLoader = contentLoader
            return this
        }

        fun endPieChart(): Page {
            return checkNotNull(page).addComponent(this)
        }
    }


    class AukletApmToGoList(name: String, description: String? = null, page: Page? = null) : Component(name, "List", description) {

        private var page: Page? = page

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
            return checkNotNull(page).addComponent(this)
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