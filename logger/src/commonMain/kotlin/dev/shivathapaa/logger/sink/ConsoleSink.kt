package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

class ConsoleSink(private val logFormatter: LogEventFormatter = LogFormatters.default()) : LogSink {
    override fun emit(event: LogEvent) {
        val output = logFormatter.format(event)

        println(output)
    }

    private fun formatMap(map: Map<String, Any?>): String {
        return map.entries.joinToString(", ") { (k, v) -> "$k=$v" }
    }
}
