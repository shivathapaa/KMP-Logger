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
     * Determines whether a log event with the given level and logger name is
     * allowed by this policy.
     *
     * @param level The severity level to check.
     * @param loggerName The name of the logger originating the event.
     * @return `true` if [level] meets or exceeds the effective minimum level
     *   for [loggerName].
     */
    fun allows(level: LogLevel, loggerName: String): Boolean {
        // Overrides are resolved first: a per-logger override outranks minLevel in both
        // directions, so minLevel(OFF) + override(name, DEBUG) still lets `name` through.
        val effectiveLevel = overrides[loggerName] ?: minLevel

        if (effectiveLevel == LogLevel.OFF) return false

        return level >= effectiveLevel
    }

    /**
     * Determines whether the given log event is allowed by this policy.
     *
     * @param event The log event to check.
     * @return `true` if the event's level meets or exceeds the effective minimum
     *   level for its logger name.
     */
    fun allows(event: LogEvent): Boolean = allows(event.level, event.loggerName)
}