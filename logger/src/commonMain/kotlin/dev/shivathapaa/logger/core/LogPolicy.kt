package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel

/**
 * Defines which log events are allowed based on their log level and logger name.
 *
 * @property minLevel The default minimum log level for all loggers.
 * @property overrides A map of logger names to their specific minimum log levels.
 */
internal class LogPolicy(
    private val minLevel: LogLevel,
    private val overrides: Map<String, LogLevel>
) {
    /**
     * Determines whether the given log event is allowed by this policy.
     *
     * @param event The log event to check.
     * @return true if the event's log level meets or exceeds the effective minimum level for its logger name.
     */
    fun allows(event: LogEvent): Boolean {
        if (minLevel == LogLevel.OFF) return false

        val effectiveLevel =
            overrides[event.loggerName] ?: minLevel

        return event.level.priority >= effectiveLevel.priority
    }
}