package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

/**
 * A formatter that provides a detailed and highly readable representation of [LogEvent]s.
 * It supports optional timestamps, thread names, and pretty-printed maps for attributes and context.
 *
 * @property includeTimestamp Whether to include the event timestamp.
 * @property includeThread Whether to include the thread name.
 * @property prettyPrint Whether to use multi-line formatting for attributes and context maps.
 * @property showEmoji Whether to include log level emojis.
 * @property timeFormatter A function to convert a timestamp (ms) to a formatted string.
 */
internal class PrettyLogFormatter(
    private val includeTimestamp: Boolean,
    private val includeThread: Boolean,
    private val prettyPrint: Boolean,
    private val showEmoji: Boolean,
    private val timeFormatter: (Long) -> String = { it.toString() }
) : LogEventFormatter {

    override fun format(event: LogEvent): String = buildString {

        if (includeTimestamp) {
            event.timestamp?.let {
                append(timeFormatter(it))
                append(" ")
            }
        }

        val emoji = if (showEmoji) "${event.level.emoji} " else ""
        append(emoji)

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
