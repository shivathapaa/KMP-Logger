package dev.shivathapaa.logger.core

/**
 * A context container that holds a map of values to be included in log events.
 *
 * @property values The map of context values.
 */
data class LogContext(
    val values: Map<String, Any?> = emptyMap()
) {
    /**
     * Merges this context with another context.
     * Values in the [other] context will override values in this context if keys collide.
     *
     * @param other The context to merge with.
     * @return A new [LogContext] containing the merged values.
     */
    fun merge(other: LogContext): LogContext =
        LogContext(values + other.values)
}