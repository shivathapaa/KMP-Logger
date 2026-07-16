package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.AttrBuilder
import dev.shivathapaa.logger.core.DefaultLogger
import dev.shivathapaa.logger.core.LogContext

/**
 * A logger providing structured logging capabilities.
 *
 * It allows logging messages with different [LogLevel]s, optional exceptions,
 * and structured attributes.
 */
class Logger internal constructor(
    private val delegate: StructuredLogger
) {
    /**
     * Logs a verbose message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun verbose(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.VERBOSE, throwable, attrs, message)

    /**
     * Logs a debug message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun debug(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.DEBUG, throwable, attrs, message)

    /**
     * Logs an info message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun info(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.INFO, throwable, attrs, message)

    /**
     * Logs a warning message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun warn(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.WARN, throwable, attrs, message)

    /**
     * Logs an error message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun error(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.ERROR, throwable, attrs, message)

    /**
     * Logs a fatal message. This will likely cause the application to terminate after processing.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun fatal(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.FATAL, throwable, attrs, message)

    /**
     * Returns a logger that attaches [ctx] to every event it emits.
     *
     * The context travels with the logger object, so unlike ambient propagation
     * (`withLogContext` from `logger-coroutines`) it is correct on **every** platform and
     * under any dispatcher, including multi-threaded ones on Kotlin/Native.
     *
     * ```kotlin
     * suspend fun handle(requestId: String) {
     *     val log = LoggerFactory.get("Api")
     *         .withContext(LogContext(mapOf("requestId" to requestId)))
     *
     *     log.info { "Starting" }                    // carries requestId
     *     withContext(Dispatchers.Default) {
     *         log.debug { "Working" }                // still carries requestId - it is a field
     *     }
     * }
     * ```
     *
     * Bound values win over ambient context on key collision, and repeated calls merge with
     * later values winning. This logger is not modified.
     *
     * @param ctx The context to attach to every event emitted by the returned logger.
     * @return A new [Logger] carrying [ctx].
     */
    fun withContext(ctx: LogContext): Logger = when {
        ctx.values.isEmpty() -> this
        delegate is DefaultLogger -> Logger(delegate.withBoundContext(ctx))
        // Loggers are only ever created by LoggerFactory over a DefaultLogger, so this is
        // unreachable today; fail loudly rather than silently dropping the context if that
        // ever changes.
        else -> error("withContext is not supported for ${delegate::class.simpleName}")
    }

    /**
     * Convenience overload of [withContext] for building a context inline.
     *
     * ```kotlin
     * val log = LoggerFactory.get("Api").withContext("requestId" to id, "userId" to 42)
     * ```
     */
    fun withContext(vararg values: Pair<String, Any?>): Logger =
        withContext(LogContext(values.toMap()))
}
