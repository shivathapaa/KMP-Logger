package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.PlatformLogger

/**
 * Simple logging API for quick, non-structured logging.
 *
 * For structured logging with attributes and context, use Logger from LoggerFactory instead.
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
    private var defaultTag: String = "App"
    private val logger = PlatformLogger()

    /**
     * Set the default tag for all Log calls without explicit tag.
     * Call this once during app initialization.
     */
    fun setDefaultTag(tag: String) {
        defaultTag = tag
    }

    /**
     * Log a VERBOSE message. Most detailed, usually filtered in production.
     */
    fun v(message: String, tag: String = defaultTag) {
        logger.v(message, tag)
    }

    /**
     * Log a DEBUG message. For debugging information.
     */
    fun d(message: String, tag: String = defaultTag) {
        logger.d(message, tag)
    }

    /**
     * Log an INFO message. For general informational messages.
     */
    fun i(message: String, tag: String = defaultTag) {
        logger.i(message, tag)
    }

    /**
     * Log a WARN message. For potential issues.
     */
    fun w(message: String, tag: String = defaultTag, throwable: Throwable? = null) {
        logger.w(message, tag, throwable)
    }

    /**
     * Log an ERROR message. For errors and failures.
     */
    fun e(message: String, tag: String = defaultTag, throwable: Throwable? = null) {
        logger.e(message, tag, throwable)
    }

    /**
     * Log a FATAL message. This will crash the app after logging.
     * Use only for unrecoverable errors.
     */
    fun fatal(message: String, tag: String = defaultTag, throwable: Throwable? = null) {
        logger.wtf(message, tag, throwable)
    }

    /**
     * Create a logger wrapper with tag derived from the class name.
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
        return LogWrapper(T::class.simpleName ?: "App")
    }

    /**
     * Create a logger wrapper with a custom tag.
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
 * A wrapper around Log that uses a fixed tag.
 * Created via Log.withTag() or Log.withClassTag<T>().
 */
class LogWrapper(private val tag: String) {

    /**
     * Log a VERBOSE message with this wrapper's tag.
     */
    fun v(message: String) {
        Log.v(message, tag)
    }

    /**
     * Log a DEBUG message with this wrapper's tag.
     */
    fun d(message: String) {
        Log.d(message, tag)
    }

    /**
     * Log an INFO message with this wrapper's tag.
     */
    fun i(message: String) {
        Log.i(message, tag)
    }

    /**
     * Log a WARN message with this wrapper's tag.
     */
    fun w(message: String, throwable: Throwable? = null) {
        Log.w(message, tag, throwable)
    }

    /**
     * Log an ERROR message with this wrapper's tag.
     */
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(message, tag, throwable)
    }

    /**
     * Log a FATAL message with this wrapper's tag.
     * This will crash the app after logging.
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
 * Extension function to log with automatic class tag.
 *
 * Usage:
 * ```
 * class MyClass {
 *     fun doWork() {
 *         loggerD("Starting work")
 *         loggerE("Work failed", exception)
 *     }
 * }
 * ```
 */
inline fun <reified T> T.loggerV(message: String) {
    Log.v(message, T::class.simpleName ?: "App")
}

inline fun <reified T> T.loggerD(message: String) {
    Log.d(message, T::class.simpleName ?: "App")
}

inline fun <reified T> T.loggerI(message: String) {
    Log.i(message, T::class.simpleName ?: "App")
}

inline fun <reified T> T.loggerW(message: String, throwable: Throwable? = null) {
    Log.w(message, T::class.simpleName ?: "App", throwable)
}

inline fun <reified T> T.loggerE(message: String, throwable: Throwable? = null) {
    Log.e(message, T::class.simpleName ?: "App", throwable)
}

inline fun <reified T> T.loggerFatal(message: String, throwable: Throwable? = null) {
    Log.fatal(message, T::class.simpleName ?: "App", throwable)
}