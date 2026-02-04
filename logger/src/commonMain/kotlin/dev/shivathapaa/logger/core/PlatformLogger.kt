package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.KMP_LOGGER_DEFAULT_TAG

/**
 * A platform-specific logger implementation.
 * This class is expected to be implemented for each supported platform (e.g., Android, iOS, JVM).
 *
 * @property tag The default tag used for logging if not provided in individual log calls.
 */
internal expect class PlatformLogger(tag: String = KMP_LOGGER_DEFAULT_TAG) {
    /** Logs a verbose message. */
    fun v(message: String, tag: String)
    /** Logs a debug message. */
    fun d(message: String, tag: String)
    /** Logs an info message. */
    fun i(message: String, tag: String)
    /** Logs a warning message. */
    fun w(message: String, tag: String, throwable: Throwable? = null)
    /** Logs an error message. */
    fun e(message: String, tag: String, throwable: Throwable? = null)
    /** Logs a fatal error message. */
    fun wtf(message: String, tag: String, throwable: Throwable? = null)
}