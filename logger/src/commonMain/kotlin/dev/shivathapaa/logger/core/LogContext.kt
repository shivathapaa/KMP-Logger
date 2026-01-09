package dev.shivathapaa.logger.core

data class LogContext(
    val values: Map<String, Any?> = emptyMap()
) {
    fun merge(other: LogContext): LogContext =
        LogContext(values + other.values)
}