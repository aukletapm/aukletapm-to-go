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

import com.google.common.collect.EvictingQueue
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


/**
 *
 * @author Eric Xu
 * @date 20/03/2018
 */
open class LineChart(
        private val interval: Long,
        private val intervalUnit: TimeUnit,
        private val accuracy: ChronoUnit,
        private val maxSize: Int,
        private var formatLabel: ((Long) -> String),
        private var loadData: (() -> List<LoadData>),
        name: String,
        description: String?
) : Component(type = "LineChart", name = name, description = description), OnInit, OnDestroy {

    enum class ValueMode {
        NORMAL, DIFFERENCE
    }

    private val scheduler = Executors.newScheduledThreadPool(1)
    private var handler: ScheduledFuture<*>? = null
    private val labels = EvictingQueue.create<Long>(maxSize)
    private var datasets = mutableMapOf<String, EvictingQueue<Point>>()
    private val previousValue = mutableMapOf<String, Double>()
    var valueMode: ValueMode = ValueMode.NORMAL

    class Point(var time: Long, var value: Double = 0.0) {

        fun update(value: Double) {
            if (!value.isNaN()) {
                this.value = Math.max(value, this.value)
            }
        }

    }

    override fun init() {
        handler = scheduler.scheduleAtFixedRate({
            tick()
        }, 0, interval, intervalUnit)
    }

    private fun tick() {
        val time = System.currentTimeMillis()
        loadData().forEach {
            update(it.dataset, it.value, time)
        }
    }

    class LoadData(val dataset: String, val value: Double)

    override fun destroy() {
        handler?.cancel(false)
        scheduler.shutdown()
    }

    class LineChartData(val labels: List<String>, val datasets: List<Dataset>) {
        class Dataset(val label: String, val data: List<Double>)
    }

    @Synchronized
    override fun load(args: Any?): Any {
        val formatLabel = checkNotNull(formatLabel, { "formatLabel was null" })
        return LineChartData(
                labels.map {
                    formatLabel(it)
                },
                datasets.map { LineChartData.Dataset(it.key, it.value.map { it.value }) }
        )
    }

    /**
     *
     * @param now milliseconds of current time
     */
    @Synchronized
    fun update(dataset: String, value: Double, now: Long) {
        var updateValue = value
        if (valueMode == ValueMode.DIFFERENCE) {
            var previous = previousValue.get(dataset)
            if (previous == null) {
                previous = value
            }
            updateValue = value - previous
            previousValue.put(dataset, value)
        }

        val dataIndex = LocalDateTime.ofInstant(Date(now).toInstant(), ZoneId.systemDefault()).truncatedTo(accuracy).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val point: Point
        val queue = getQueue(dataset)

        if (queue.isEmpty() || dataIndex > queue.last().time) {
            point = Point(dataIndex)
            queue.add(point)
            if (!labels.contains(dataIndex)) {
                labels.add(dataIndex)
            }
        }
        queue.last().update(updateValue)
    }

    private fun getQueue(dataset: String): EvictingQueue<Point> {
        var queue = datasets.get(dataset)
        if (queue == null) {
            queue = EvictingQueue.create<Point>(maxSize)
            datasets.put(dataset, queue)
        }
        return queue!!
    }

    class Builder(private val name: String, private val page: AukletApmToGo.Page? = null) {
        private var interval: Long = 1
        private var intervalUnit: TimeUnit = TimeUnit.SECONDS
        private var accuracy: ChronoUnit = ChronoUnit.MINUTES
        private var maxSize: Int = 30
        private var description: String? = null
        private var formatLabel: ((Long) -> String)? = null
        private var loadData: (() -> List<LoadData>)? = null
        private var valueMode: ValueMode = ValueMode.NORMAL

        fun endLineChart(): AukletApmToGo.Page {
            return checkNotNull(page).addComponent(build())
        }

        fun description(description: String): Builder {
            this.description = description
            return this
        }

        fun interval(interval: Long): Builder {
            this.interval = interval
            return this
        }

        fun intervalUnit(intervalUnit: TimeUnit): Builder {
            this.intervalUnit = intervalUnit
            return this
        }

        fun accuracy(accuracy: ChronoUnit): Builder {
            this.accuracy = accuracy
            return this
        }

        fun maxSize(maxSize: Int): Builder {
            this.maxSize = maxSize
            return this
        }

        fun valueMode(valueMode: ValueMode): Builder {
            this.valueMode = valueMode
            return this
        }

        fun formatLabel(formatLabel: ((Long) -> String)): Builder {
            this.formatLabel = formatLabel
            return this
        }

        fun loadData(loadData: (() -> List<LoadData>)): Builder {
            this.loadData = loadData
            return this
        }

        fun build(): LineChart {
            val formatLabel = if (formatLabel == null) {
                { SimpleDateFormat("HH:mm").format(it) }
            } else {
                formatLabel!!
            }
            val loadData = checkNotNull(loadData, { "loadData was null" })
            if (description == null) {
                description = name
            }
            val lineChart = LineChart(interval, intervalUnit, accuracy, maxSize, formatLabel, loadData, name, description)
            lineChart.valueMode = valueMode
            return lineChart
        }

    }

    companion object {
        fun newBuilder(name: String, page: AukletApmToGo.Page? = null): Builder {
            return Builder(name, page)
        }
    }

}