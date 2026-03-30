package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.StructuredLogger

/**
 * Default implementation of [StructuredLogger].
 *
 * Captures the current time, active [LogContext], and caller-supplied
 * attributes on every log call, assembles a [LogEvent], and forwards it
 * through the [LogPipeline] for filtering and sink dispatch.
 *
 * Instances are obtained via [dev.shivathapaa.logger.api.LoggerFactory.get]
 * and should not be constructed directly.
 *
 * @property name The logger name, typically the class or module name used
 *   as the tag in log output.
 * @property pipeline The pipeline responsible for filtering and dispatching
 *   [LogEvent]s to configured sinks.
 */
internal class DefaultLogger(
    private val name: String,
    private val pipeline: LogPipeline
) : StructuredLogger {

    /**
     * Assembles and submits a [LogEvent] to the pipeline.
     *
     * Captures the current timestamp, thread name, active context, and
     * caller-supplied attributes, then forwards the assembled event to the
     * pipeline for level filtering and sink dispatch.
     *
     * @param level The severity level of the log.
     * @param throwable An optional exception associated with the log.
     * @param attrs A builder block for attaching structured key-value attributes.
     * @param message A lambda returning the log message string.
     */
    override fun log(
        level: LogLevel,
        throwable: Throwable?,
        attrs: AttrBuilder.() -> Unit,
        message: () -> String
    ) {
        if (level == LogLevel.OFF) return

        val event = LogEvent(
            timestamp = currentTimeMillis(),
            level = level,
            loggerName = name,
            message = message(),
            throwable = throwable,
            attributes = AttrBuilder().apply(attrs).build(),
            context = LogContextHolder.current(),
            thread = currentThreadName()
        )

        pipeline.process(event)
    }
}