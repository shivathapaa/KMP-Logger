package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

/**
 * A [LogSink] that prints log events to the standard output (console) using [println].
 *
 * @property logFormatter The formatter used to convert log events into strings.
 */
class ConsoleSink(
    private val logFormatter: LogEventFormatter = LogFormatters.default(false)
) : LogSink {
    override fun emit(event: LogEvent) {
        val output = logFormatter.format(event)
        println(output)
    }

    private fun formatMap(map: Map<String, Any?>): String {
        return map.entries.joinToString(", ") { (k, v) -> "$k=$v" }
    }
}