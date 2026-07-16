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
 * @property boundContext Context carried by this logger instance and merged into every
 *   event it emits, on top of the ambient context. Empty unless [withContext] was used.
 */
internal class DefaultLogger(
    private val name: String,
    private val pipeline: LogPipeline,
    private val boundContext: LogContext = LogContext()
) : StructuredLogger {

    /**
     * Assembles and submits a [LogEvent] to the pipeline.
     *
     * The policy check runs before [message] is evaluated, so message
     * construction is skipped entirely for suppressed log levels.
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
        if (!pipeline.wouldProcess(level, name)) return

        val event = LogEvent(
            timestamp = currentTimeMillis(),
            level = level,
            loggerName = name,
            message = message(),
            throwable = throwable,
            attributes = AttrBuilder().apply(attrs).build(),
            // Bound context wins over ambient: binding is explicit, whereas ambient context
            // is best-effort off JVM/Android and must never override a deliberate value.
            context = resolveContext(),
            thread = currentThreadName()
        )

        pipeline.process(event)
    }

    private fun resolveContext(): LogContext {
        val ambient = LogContextHolder.current()
        return when {
            boundContext.values.isEmpty() -> ambient
            ambient.values.isEmpty() -> boundContext
            else -> ambient.merge(boundContext)
        }
    }

    /**
     * Returns a copy of this logger carrying [boundContext] merged with [ctx]; later values
     * win on collision. Merging into the event happens after the level filter in [log], so
     * binding costs nothing for suppressed levels.
     */
    internal fun withBoundContext(ctx: LogContext): DefaultLogger =
        DefaultLogger(name, pipeline, boundContext.merge(ctx))
}