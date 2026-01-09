package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel

data class LogEvent(
    val level: LogLevel,
    val loggerName: String,
    val message: String?,
    val throwable: Throwable?,
    val attributes: Map<String, Any?>,
    val context: LogContext,
    val thread: String,
    val timestamp: Long? = null
)