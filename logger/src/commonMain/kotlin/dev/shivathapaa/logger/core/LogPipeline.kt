package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.sink.LogSink

/**
 * Manages the flow of log events from the logger to the configured sinks.
 * It uses a [LogPolicy] to filter events and then emits them to all registered [LogSink]s.
 *
 * @property policy The policy used to filter log events.
 * @property sinks The list of sinks to which log events are emitted.
 */
internal class LogPipeline(
    private val policy: LogPolicy,
    private val sinks: List<LogSink>
) {
    /**
     * Processes a log event by checking it against the policy and emitting it to sinks if allowed.
     * If the log level is [LogLevel.FATAL], it flushes all sinks and throws a [RuntimeException].
     *
     * @param event The log event to process.
     */
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