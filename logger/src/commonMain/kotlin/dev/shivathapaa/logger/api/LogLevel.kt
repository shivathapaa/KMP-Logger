package dev.shivathapaa.logger.api

/**
 * Defines the severity levels for log events.
 *
 * @property priority The numerical priority of the level, used for filtering.
 * @property emoji A visual indicator associated with the level.
 */
enum class LogLevel(val priority: Int, val emoji: String) {
    /** Detailed information, typically only useful for development. */
    VERBOSE(0, "💜"),
    /** Debugging information. */
    DEBUG(1, "💚"),
    /** General operational information. */
    INFO(2, "💙"),
    /** Potential issues or important but non-critical occurrences. */
    WARN(3, "💛"),
    /** Errors and failures that should be investigated. */
    ERROR(4, "❤️"),
    /** Severe errors that will lead to application termination. */
    FATAL(5, "💔"),
    /** Special level used to turn off all logging. */
    OFF(6, "❌")
}
