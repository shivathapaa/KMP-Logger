package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.StructuredLogger

/**
 * Default implementation of [StructuredLogger].
 *
 * This logger captures contextual information, attributes, and messages,
 * and passes them through a [LogPipeline] for processing.
 *
 * @property name The name of the logger, typically the class name.
 * @property pipeline The pipeline responsible for processing [LogEvent]s.
 */
internal class DefaultLogger(
    private val name: String,
    private val pipeline: LogPipeline
) : StructuredLogger {

    /**
     * Logs a message with the specified log level, optional throwable, and attributes.
     *
     * @param level The severity level of the log.
     * @param throwable An optional exception associated with the log.
     * @param attrs A builder for adding structured attributes to the log.
     * @param message A lambda that returns the log message.
     */
    override fun log(
        level: LogLevel,
        throwable: Throwable?,
        attrs: AttrBuilder.() -> Unit,
        message: () -> String
    ) {
        if (level == LogLevel.OFF) return

        val ctx = LogContextHolder.current()
        val attrBuilder = AttrBuilder().apply(attrs)

        val event = LogEvent(
            timestamp = null,
            level = level,
            loggerName = name,
            message = message(),
            throwable = throwable,
            attributes = attrBuilder.build(),
            context = ctx,
            thread = currentThreadName()
        )

        pipeline.process(event)
    }

    /**
     * Retrieves the name of the current thread.
     * Currently returns a placeholder "main".
     */
    private fun currentThreadName(): String = "main" // Will work with context later
}