package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent

class ConsoleSink(
    private val includeTimestamp: Boolean = false,
    private val includeThread: Boolean = false,
    private val prettyPrint: Boolean = false
) : LogSink {
    override fun emit(event: LogEvent) {
        val output = buildString {
            if (includeTimestamp && event.timestamp != null) {
                append(event.timestamp)
                append(" ")
            }

            append("[")
            append(event.level.name)
            append("] ")

            if (includeThread) {
                append("[${event.thread}] ")
            }

            append(event.loggerName)
            append(" - ")

            append(event.message ?: "")

            if (event.attributes.isNotEmpty()) {
                if (prettyPrint) {
                    append("\n  Attributes:")
                    event.attributes.forEach { (k, v) ->
                        append("\n    $k = $v")
                    }
                } else {
                    append(" | attrs=")
                    append(formatMap(event.attributes))
                }
            }

            if (event.context.values.isNotEmpty()) {
                if (prettyPrint) {
                    append("\n  Context:")
                    event.context.values.forEach { (k, v) ->
                        append("\n    $k = $v")
                    }
                } else {
                    append(" | ctx=")
                    append(formatMap(event.context.values))
                }
            }

            event.throwable?.let {
                append("\n")
                append(it.stackTraceToString())
            }
        }

        println(output)
    }

    private fun formatMap(map: Map<String, Any?>): String {
        return map.entries.joinToString(", ") { (k, v) -> "$k=$v" }
    }
}
