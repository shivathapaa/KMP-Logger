package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.KMP_LOGGER_DEFAULT_TAG

internal expect class PlatformLogger(tag: String = KMP_LOGGER_DEFAULT_TAG) {
    fun v(message: String, tag: String)
    fun d(message: String, tag: String)
    fun i(message: String, tag: String)
    fun w(message: String, tag: String, throwable: Throwable? = null)
    fun e(message: String, tag: String, throwable: Throwable? = null)
    fun wtf(message: String, tag: String, throwable: Throwable? = null)
}