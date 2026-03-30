package dev.shivathapaa.logger.api

import kotlin.concurrent.Volatile

/**
 * Simple logging API for quick, tag-based logging.
 *
 * All calls are routed through [LoggerFactory], so sinks, formatters, and level
 * filtering configured via [LoggerFactory.install] apply equally to both APIs.
 *
 * For structured logging with attributes and context, use [Logger] from
 * [LoggerFactory] instead.
 *
 * Usage:
 * ```
 * // Direct logging with default tag
 * Log.i("App started")
 * Log.e("Error occurred", throwable = exception)
 *
 * // With custom tag
 * Log.i("User logged in", tag = "Auth")
 *
 * // Using class-based tag
 * class MyViewModel {
 *     private val log = Log.withClassTag<MyViewModel>()
 *
 *     fun doSomething() {
 *         log.d("Doing something")
 *     }
 * }
 *
 * // Using custom tag wrapper
 * val log = Log.withTag("NetworkModule")
 * log.d("Connecting to server")
 * ```
 */
object Log {
    @Volatile
    private var defaultTag: String = KMP_LOGGER_DEFAULT_TAG

    /**
     * Sets the default tag used for all [Log] calls that do not specify an explicit tag.
     * Call this once during application initialization.
     *
     * @param tag The default tag to apply.
     */
    fun setDefaultTag(tag: String) {
        defaultTag = tag
    }

    /**
     * Logs a VERBOSE message.
     *
     * @param message The message to log.
     * @param tag The tag to identify the log source. Defaults to the current default tag.
     */
    fun v(message: String, tag: String = defaultTag) {
        LoggerFactory.get(tag).verbose { message }
    }

    /**
     * Logs a DEBUG message.
     *
     * @param message The message to log.
     * @param tag The tag to identify the log source. Defaults to the current default tag.
     */
    fun d(message: String, tag: String = defaultTag) {
        LoggerFactory.get(tag).debug { message }
    }

    /**
     * Logs an INFO message.
     *
     * @param message The message to log.
     * @param tag The tag to identify the log source. Defaults to the current default tag.
     */
    fun i(message: String, tag: String = defaultTag) {
        LoggerFactory.get(tag).info { message }
    }

    /**
     * Logs a WARN message.
     *
     * @param message The message to log.
     * @param tag The tag to identify the log source. Defaults to the current default tag.
     * @param throwable An optional exception associated with the warning.
     */
    fun w(message: String, tag: String = defaultTag, throwable: Throwable? = null) {
        LoggerFactory.get(tag).warn(throwable) { message }
    }

    /**
     * Logs an ERROR message.
     *
     * @param message The message to log.
     * @param tag The tag to identify the log source. Defaults to the current default tag.
     * @param throwable An optional exception associated with the error.
     */
    fun e(message: String, tag: String = defaultTag, throwable: Throwable? = null) {
        LoggerFactory.get(tag).error(throwable) { message }
    }

    /**
     * Logs a FATAL message and throws a [RuntimeException] after all sinks have been flushed.
     *
     * This method never returns normally. Use only for unrecoverable errors.
     *
     * @param message The message to log.
     * @param tag The tag to identify the log source. Defaults to the current default tag.
     * @param throwable An optional exception to attach as the cause.
     */
    fun fatal(message: String, tag: String = defaultTag, throwable: Throwable? = null) {
        LoggerFactory.get(tag).fatal(throwable) { message }
    }

    /**
     * Returns a [LogWrapper] whose tag is derived from the simple name of class [T].
     *
     * Usage:
     * ```
     * class MyViewModel {
     *     private val log = Log.withClassTag<MyViewModel>()
     *
     *     fun init() {
     *         log.d("ViewModel initialized")
     *     }
     * }
     * ```
     */
    inline fun <reified T> withClassTag(): LogWrapper {
        return LogWrapper(T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG)
    }

    /**
     * Returns a [LogWrapper] that uses [tag] on every call.
     *
     * Usage:
     * ```
     * val log = Log.withTag("NetworkModule")
     * log.d("Request started")
     * log.e("Request failed", throwable = exception)
     * ```
     */
    fun withTag(tag: String): LogWrapper {
        return LogWrapper(tag)
    }
}

/**
 * A fixed-tag wrapper around [Log].
 *
 * Obtained via [Log.withTag] or [Log.withClassTag]. All calls delegate to the
 * corresponding [Log] method with the wrapper's tag.
 */
class LogWrapper(private val tag: String) {

    /**
     * Logs a VERBOSE message with this wrapper's tag.
     */
    fun v(message: String) {
        Log.v(message, tag)
    }

    /**
     * Logs a DEBUG message with this wrapper's tag.
     */
    fun d(message: String) {
        Log.d(message, tag)
    }

    /**
     * Logs an INFO message with this wrapper's tag.
     */
    fun i(message: String) {
        Log.i(message, tag)
    }

    /**
     * Logs a WARN message with this wrapper's tag.
     *
     * @param throwable An optional exception associated with the warning.
     */
    fun w(message: String, throwable: Throwable? = null) {
        Log.w(message, tag, throwable)
    }

    /**
     * Logs an ERROR message with this wrapper's tag.
     *
     * @param throwable An optional exception associated with the error.
     */
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(message, tag, throwable)
    }

    /**
     * Logs a FATAL message with this wrapper's tag and throws after flushing all sinks.
     *
     * This method never returns normally.
     *
     * @param throwable An optional exception to attach as the cause.
     */
    fun fatal(message: String, throwable: Throwable? = null) {
        Log.fatal(message, tag, throwable)
    }

    /**
     * Returns the tag used by this wrapper.
     */
    fun getTag(): String = tag
}


/**
 * Extension function to log a VERBOSE message using the simple name of [T] as the tag.
 */
inline fun <reified T> T.loggerV(message: String) {
    Log.v(message, T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG)
}

/**
 * Extension function to log a DEBUG message using the simple name of [T] as the tag.
 */
inline fun <reified T> T.loggerD(message: String) {
    Log.d(message, T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG)
}

/**
 * Extension function to log an INFO message using the simple name of [T] as the tag.
 */
inline fun <reified T> T.loggerI(message: String) {
    Log.i(message, T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG)
}

/**
 * Extension function to log a WARN message using the simple name of [T] as the tag.
 *
 * @param throwable An optional exception associated with the warning.
 */
inline fun <reified T> T.loggerW(message: String, throwable: Throwable? = null) {
    Log.w(message, T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG, throwable)
}

/**
 * Extension function to log an ERROR message using the simple name of [T] as the tag.
 *
 * @param throwable An optional exception associated with the error.
 */
inline fun <reified T> T.loggerE(message: String, throwable: Throwable? = null) {
    Log.e(message, T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG, throwable)
}

/**
 * Extension function to log a FATAL message using the simple name of [T] as the tag.
 *
 * This function never returns normally.
 *
 * @param throwable An optional exception to attach as the cause.
 */
inline fun <reified T> T.loggerFatal(message: String, throwable: Throwable? = null) {
    Log.fatal(message, T::class.simpleName ?: KMP_LOGGER_DEFAULT_TAG, throwable)
}

/** The default tag used when no explicit tag is provided to [Log]. */
const val KMP_LOGGER_DEFAULT_TAG = "App"