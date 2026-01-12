package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

internal object CompactLogFormatter : LogEventFormatter {
    override fun format(event: LogEvent): String = buildString {
        append(event.level.name)
        append(" | ")
        append(event.loggerName)

        event.message?.let {
            append(" - ")
            append(it)
        }

        if (event.attributes.isNotEmpty()) {
            append(" ")
            append(formatAttributes(event.attributes))
        }

        event.throwable?.let {
            append("\n")
            append(it.stackTraceToString())
        }
    }

    private fun formatAttributes(attrs: Map<String, Any?>): String =
        attrs.entries.joinToString(
            prefix = "[",
            postfix = "]",
            separator = " "
        ) { (k, v) -> "$k=$v" }
}