package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

internal object DefaultLogFormatter : LogEventFormatter {
    override fun format(event: LogEvent): String = buildString {
        append('[')
        append(event.level.name)
        append("] ")

        append(event.loggerName)

        event.message?.takeIf { it.isNotBlank() }?.let {
            append(": ")
            append(it)
        }

        event.throwable?.let {
            append('\n')
            append(it.stackTraceToString())
        }
    }
}
