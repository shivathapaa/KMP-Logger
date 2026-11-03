package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.PlatformLogger
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

/**
 * A [LogSink] that writes to each platform's native log output:
 * - **Android** - `android.util.Log` (visible in Logcat)
 * - **iOS / macOS** - `NSLog` (visible in the Xcode console)
 * - **JS / Wasm** - the browser or Node.js `console`
 * - **JVM / Linux / Windows** - standard output
 *
 * This is the sink installed automatically when
 * [LoggerFactory.install][dev.shivathapaa.logger.api.LoggerFactory.install] is never called, and
 * the recommended one during development: every level appears where developers already look.
 *
 * The output format is controlled by [logFormatter]. Defaults to
 * [LogFormatters.pretty] without emoji.
 *
 * A throwable is rendered once, by the platform channel itself (on JS as the error object), so
 * [logFormatter] receives the event without it. As a result [LogFormatters.json] output from this
 * sink has no `error` field; use [ConsoleSink] or [RemoteLogSink] for pure JSON lines.
 *
 * Example:
 * ```kotlin
 * LoggerFactory.install(
 *     LoggerConfig.Builder()
 *         .addSink(DefaultLogSink())
 *         // or with a custom formatter
 *         .addSink(DefaultLogSink(LogFormatters.compact(showEmoji = true)))
 *         .build()
 * )
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
        val throwable = event.throwable
        // The platform channel renders the throwable, so leaving it out of the formatted text
        // prints the stack trace once instead of twice.
        val output = logFormatter.format(if (throwable == null) event else event.copy(throwable = null))

        when (event.level) {
            LogLevel.VERBOSE -> logger.v(output, tag, throwable)
            LogLevel.DEBUG -> logger.d(output, tag, throwable)
            LogLevel.INFO -> logger.i(output, tag, throwable)
            LogLevel.WARN -> logger.w(output, tag, throwable)
            LogLevel.ERROR -> logger.e(output, tag, throwable)
            LogLevel.FATAL -> logger.wtf(output, tag, throwable)
            LogLevel.OFF -> Unit
        }
    }
}