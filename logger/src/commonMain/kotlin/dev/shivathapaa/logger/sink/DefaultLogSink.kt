package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.PlatformLogger
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

/**
 * A [LogSink] that delegates to the platform's native logging mechanism.
 *
 * On each platform this maps to the most appropriate native output:
 * - **Android** - `android.util.Log` (visible in Logcat)
 * - **iOS / macOS** - `NSLog` (visible in Xcode console)
 * - **JVM** - `java.util.logging.Logger`
 * - **JS / Wasm** - `console.log` / `console.error` etc.
 * - **Linux / Windows** - `println` to standard output
 *
 * This is the recommended sink for most applications because it integrates
 * with the platform's existing log infrastructure (filtering, log levels,
 * crash reporting hooks, etc.).
 *
 * The output format is controlled by [logFormatter]. Defaults to
 * [LogFormatters.pretty] without emoji.
 *
 * Example:
 * ```kotlin
 * LoggerFactory.install {
 *     addSink(DefaultLogSink())
 *     // or with a custom formatter
 *     addSink(DefaultLogSink(LogFormatters.compact(showEmoji = true)))
 * }
 * ```
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
            LogLevel.OFF -> Unit
        }
    }
}