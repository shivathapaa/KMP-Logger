package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.PlatformLogger
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

class DefaultLogSink(
    private val logFormatter: LogEventFormatter = LogFormatters.pretty(false)
) : LogSink {
    private val logger = PlatformLogger()

    override fun emit(event: LogEvent) {
        val tag = event.loggerName
        val output = logFormatter.format(event)

        when (event.level) {
            LogLevel.VERBOSE -> logger.v(output, tag)
            LogLevel.DEBUG -> logger.d(output, tag)
            LogLevel.INFO -> logger.i(output, tag)
            LogLevel.WARN -> logger.w(output, tag, event.throwable)
            LogLevel.ERROR -> logger.e(output, tag, event.throwable)
            LogLevel.FATAL -> logger.wtf(output, tag, event.throwable)
            LogLevel.OFF -> {}
        }
    }

    private fun formatMap(map: Map<String, Any?>): String {
        return map.entries.joinToString(", ") { (k, v) -> "$k=$v" }
    }
}