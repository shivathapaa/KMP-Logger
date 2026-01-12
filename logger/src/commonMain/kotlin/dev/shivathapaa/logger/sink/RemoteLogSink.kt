package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

class RemoteLogSink(
    private val logFormatter: LogEventFormatter = LogFormatters.json(false),
    private val send: (String) -> Unit
) : LogSink {
    override fun emit(event: LogEvent) {
        val output = logFormatter.format(event)
        send(output)
    }
}