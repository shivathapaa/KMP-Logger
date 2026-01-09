package dev.shivathapaa.logger.core

internal expect class PlatformLogger() {
    fun v(message: String, tag: String)
    fun d(message: String, tag: String)
    fun i(message: String, tag: String)
    fun w(message: String, tag: String, throwable: Throwable? = null)
    fun e(message: String, tag: String, throwable: Throwable? = null)
    fun wtf(message: String, tag: String, throwable: Throwable? = null)
}