package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogEventFormatter
import dev.shivathapaa.logger.formatters.LogFormatters

/**
 * A [LogSink] that sends log events to a remote destination via a provided [send] function.
 *
 * This can be used to integrate with custom HTTP backends, message queues, or any other
 * remote logging service.
 *
 * ### Example: Firebase Crashlytics Sink
 * ```kotlin
 * class FirebaseLogSink(
 *     private val minLevel: LogLevel = LogLevel.WARN,
 *     private val formatter: LogEventFormatter = LogFormatters.json(false)
 * ) : LogSink {
 *     override fun emit(event: LogEvent) {
 *         if (event.level.priority < minLevel.priority) return
 *
 *         val message = formatter.format(event)
 *         // FirebaseCrashlytics.getInstance().log(message)
 *
 *         if (event.level >= LogLevel.ERROR) {
 *             // FirebaseCrashlytics.getInstance().recordException(
 *             //     event.throwable ?: RuntimeException(message)
 *             // )
 *         }
 *     }
 * }
 * ```
 *
 * ### Example: Facebook App Events Sink
 * ```kotlin
 * class FacebookLogSink : LogSink {
 *     override fun emit(event: LogEvent) {
 *         // Map LogEvent to Facebook App Event parameters
 *         val params = Bundle().apply {
 *             putString("level", event.level.name)
 *             putString("logger", event.loggerName)
 *             event.message?.let { putString("message", it.take(100)) }
 *             
 *             // Add custom attributes
 *             event.attributes.forEach { (k, v) -> 
 *                 putString("attr_$k", v?.toString()?.take(100)) 
 *             }
 *         }
 *         
 *         val eventName = "log_${event.level.name.lowercase()}"
 *         // appEventsLogger.logEvent(eventName, params)
 *     }
 * }
 * ```
 *
 * @property logFormatter The formatter used to convert log events into strings.
 * @property send The function used to transmit the formatted log string.
 */
class RemoteLogSink(
    private val logFormatter: LogEventFormatter = LogFormatters.json(false),
    private val send: (String) -> Unit
) : LogSink {
    override fun emit(event: LogEvent) {
        val output = logFormatter.format(event)
        send(output)
    }
}