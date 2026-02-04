package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.sink.LogSink

/**
 * Configuration for the logger.
 *
 * @property minLevel The default minimum log level.
 * @property overrides Specific log level overrides for different logger names.
 * @property sinks The list of sinks where logs will be sent.
 */
class LoggerConfig internal constructor(
    val minLevel: LogLevel,
    val overrides: Map<String, LogLevel>,
    val sinks: List<LogSink>
) {
    /**
     * A builder class for creating instances of [LoggerConfig].
     */
    class Builder {
        private var minLevel: LogLevel = LogLevel.INFO
        private val overrides = mutableMapOf<String, LogLevel>()
        private val sinks = mutableListOf<LogSink>()

        /**
         * Sets the default minimum log level.
         */
        fun minLevel(level: LogLevel) = apply {
            this.minLevel = level
        }

        /**
         * Overrides the log level for a specific logger tag (name).
         */
        fun override(tag: String, level: LogLevel) = apply {
            overrides[tag] = level
        }

        /**
         * Adds a sink to which log events will be emitted.
         */
        fun addSink(sink: LogSink) = apply {
            sinks += sink
        }

        /**
         * Builds the [LoggerConfig] instance.
         * Throws an [IllegalArgumentException] if no sinks are added.
         */
        fun build(): LoggerConfig {
            require(sinks.isNotEmpty()) {
                "LoggerConfig must have at least one LogSink. E.g., DefaultLogSink(), ConsoleSink(),..."
            }

            return LoggerConfig(
                minLevel = minLevel,
                overrides = overrides.toMap(),
                sinks = sinks.toList()
            )
        }
    }
}
