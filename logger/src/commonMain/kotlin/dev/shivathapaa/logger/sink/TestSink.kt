package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogEvent

/**
 * A [LogSink] implementation designed for testing purposes.
 * It stores all emitted [LogEvent]s in an in-memory list for verification.
 */
class TestSink : LogSink {
    /**
     * The list of events captured by this sink.
     */
    val events = mutableListOf<LogEvent>()

    override fun emit(event: LogEvent) {
        events += event
    }

    /**
     * Clears all captured events.
     */
    fun clear() = events.clear()

    /**
     * Checks if any event with the specified [level] was captured.
     */
    fun hasLevel(level: LogLevel) = events.any { it.level == level }

    /**
     * Returns a list of messages for all captured events with the specified [level].
     */
    fun messagesWithLevel(level: LogLevel) =
        events.filter { it.level == level }.map { it.message }

    /**
     * Returns the most recently captured event, or null if no events have been captured.
     */
    fun lastEvent() = events.lastOrNull()
}