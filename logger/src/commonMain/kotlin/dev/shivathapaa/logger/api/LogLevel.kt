package dev.shivathapaa.logger.api

/**
 * Defines the severity levels for log events, ordered from least to most severe.
 *
 * @property emoji A visual indicator associated with the level, used by formatters
 *   that have emoji output enabled.
 */
enum class LogLevel(val emoji: String) {
    /** Detailed information, typically only useful during development. */
    VERBOSE("💜"),
    /** Debugging information. */
    DEBUG("💚"),
    /** General operational information. */
    INFO("💙"),
    /** Potential issues or important but non-critical occurrences. */
    WARN("💛"),
    /** Errors and failures that should be investigated. */
    ERROR("❤️"),
    /** Severe errors that will lead to application termination. */
    FATAL("💔"),
    /** Special level used to turn off all logging. */
    OFF("❌")
}