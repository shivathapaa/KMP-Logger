package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel

internal class LogPolicy(
    private val minLevel: LogLevel,
    private val overrides: Map<String, LogLevel>
) {
    fun allows(event: LogEvent): Boolean {
        if (minLevel == LogLevel.OFF) return false

        val effectiveLevel =
            overrides[event.loggerName] ?: minLevel

        return event.level.priority >= effectiveLevel.priority
    }
}