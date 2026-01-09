package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.sink.LogSink

class LoggerConfig internal constructor(
    val minLevel: LogLevel,
    val overrides: Map<String, LogLevel>,
    val sinks: List<LogSink>
) {
    class Builder {
        private var minLevel: LogLevel = LogLevel.INFO
        private val overrides = mutableMapOf<String, LogLevel>()
        private val sinks = mutableListOf<LogSink>()

        fun minLevel(level: LogLevel) = apply {
            this.minLevel = level
        }

        fun override(tag: String, level: LogLevel) = apply {
            overrides[tag] = level
        }

        fun addSink(sink: LogSink) = apply {
            sinks += sink
        }

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
