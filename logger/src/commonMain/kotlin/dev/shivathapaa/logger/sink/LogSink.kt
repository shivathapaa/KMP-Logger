package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent

interface LogSink {
    fun emit(event: LogEvent)
    fun flush() {}
}
