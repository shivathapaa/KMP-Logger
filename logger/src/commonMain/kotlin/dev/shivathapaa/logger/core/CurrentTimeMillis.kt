package dev.shivathapaa.logger.core

/**
 * Returns the current time as epoch milliseconds.
 *
 * The value is suitable for use as a [LogEvent] timestamp.
 */
internal expect fun currentTimeMillis(): Long