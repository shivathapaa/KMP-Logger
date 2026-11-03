package dev.shivathapaa.logger.core

import kotlin.time.Clock

/**
 * Returns the current time as epoch milliseconds.
 *
 * The value is suitable for use as a [LogEvent] timestamp.
 */
internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()