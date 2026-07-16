package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

/**
 * A [LogSink] that writes log events to standard output using [println].
 *
 * Suitable for command-line tools, desktop apps, and server-side environments
 * where platform-native logging (e.g. Logcat, NSLog) is not available or desired.
 *
 * The output format is controlled by [logFormatter]. Defaults to
 * [LogFormatters.default] without emoji.
 *
 * Example:
 * ```kotlin
 * LoggerFactory.install(
 *     LoggerConfig.Builder()
 *         .addSink(ConsoleSink())
 *         // or with a custom formatter
 *         .addSink(ConsoleSink(LogFormatters.json(showEmoji = false)))
 *         .build()
 * )
 * ```
 *
 * @property logFormatter The formatter used to convert log events into strings.
 */
class ConsoleSink(
    private val logFormatter: LogEventFormatter = LogFormatters.default(false)
) : LogSink {
    override fun emit(event: LogEvent) {
        println(logFormatter.format(event))
    }
}