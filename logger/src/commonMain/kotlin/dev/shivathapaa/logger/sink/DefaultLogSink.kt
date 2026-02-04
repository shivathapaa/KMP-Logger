package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.PlatformLogger
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

/**
 * The default [LogSink] implementation that delegates to a [PlatformLogger].
 *
 * This sink automatically uses the appropriate platform-specific logging mechanism
 * (e.g., Logcat on Android, NSLog on iOS, standard output on JVM).
 *
 * @property logFormatter The formatter used to convert log events into strings.
 */
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