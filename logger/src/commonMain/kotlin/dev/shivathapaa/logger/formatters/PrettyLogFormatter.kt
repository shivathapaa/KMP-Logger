package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

internal class PrettyLogFormatter(
    private val includeTimestamp: Boolean = false,
    private val includeThread: Boolean = false,
    private val prettyPrint: Boolean = true,
    private val timeFormatter: (Long) -> String = { it.toString() }
) : LogEventFormatter {

    override fun format(event: LogEvent): String = buildString {

        if (includeTimestamp) {
            event.timestamp?.let {
                append(timeFormatter(it))
                append(" ")
            }
        }

        append("[")
        append(event.level.name)
        append("] ")

        if (includeThread) {
            append("[")
            append(event.thread)
            append("] ")
        }

        append(event.loggerName)

        event.message?.takeIf { it.isNotBlank() }?.let {
            append(" - ")
            append(it)
        }

        appendAttributes("Attributes", event.attributes)
        appendAttributes("Context", event.context.values)

        event.throwable?.let {
            append('\n')
            append(it.stackTraceToString())
        }
    }

    private fun StringBuilder.appendAttributes(
        label: String,
        map: Map<String, Any?>
    ) {
        if (map.isEmpty()) return

        if (prettyPrint) {
            append('\n')
            append("  ")
            append(label)
            append(":")

            map.forEach { (k, v) ->
                append('\n')
                append("    ")
                append(k)
                append(" = ")
                append(v)
            }
        } else {
            append(" | ")
            append(label.lowercase())
            append("=")
            append(formatMap(map))
        }
    }
}
