package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogEvent

class TestSink : LogSink {
    val events = mutableListOf<LogEvent>()
    override fun emit(event: LogEvent) {
        events += event
    }

    fun clear() = events.clear()

    fun hasLevel(level: LogLevel) = events.any { it.level == level }

    fun messagesWithLevel(level: LogLevel) =
        events.filter { it.level == level }.map { it.message }

    fun lastEvent() = events.lastOrNull()
}
