package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.sink.LogSink

internal class LogPipeline(
    private val policy: LogPolicy,
    private val sinks: List<LogSink>
) {
    fun process(event: LogEvent) {
        if (!policy.allows(event)) return

        sinks.forEach { it.emit(event) }

        if (event.level == LogLevel.FATAL) {
            sinks.forEach { it.flush() }
            fatal(event)
        }
    }

    private fun fatal(event: LogEvent) {
        throw RuntimeException("FATAL: ${event.message}", event.throwable)
    }
}