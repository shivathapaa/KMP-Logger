package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

fun interface LogEventFormatter {
    fun format(event: LogEvent): String
}