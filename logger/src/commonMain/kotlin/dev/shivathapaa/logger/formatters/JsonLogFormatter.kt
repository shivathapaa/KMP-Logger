package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

/**
 * A [LogEventFormatter] that serializes each [LogEvent] as a single-line JSON object.
 *
 * Produces standards-compliant JSON suitable for ingestion by log aggregation
 * and observability platforms such as Elasticsearch, Datadog, Splunk, and Loki.
 *
 * Output fields:
 * - `level` - the log level name (e.g. `"INFO"`)
 * - `levelEmoji` - the level's emoji indicator, present only when [showEmoji] is `true`
 * - `logger` - the logger name / tag
 * - `timestamp` - epoch milliseconds since the Unix epoch
 * - `message` - the log message, present only when non-null
 * - `attributes` - key/value map of structured attributes, present only when non-empty
 * - `context` - key/value map of the active [dev.shivathapaa.logger.core.LogContext], present only when non-empty
 * - `error` - full stack trace string, present only when a throwable is attached
 *
 * Example output (`showEmoji = false`):
 * ```json
 * {"level":"ERROR","logger":"AuthService","message":"Login failed","attributes":{"userId":42}}
 * ```
 *
 * Example output (`showEmoji = true`):
 * ```json
 * {"level":"ERROR","levelEmoji":"❤️","logger":"AuthService","message":"Login failed"}
 * ```
 *
 * @property showEmoji Whether to include a `levelEmoji` field in the JSON output.
 */
internal class JsonLogFormatter(private val showEmoji: Boolean) : LogEventFormatter {
    override fun format(event: LogEvent): String = buildString {
        append('{')
        appendField("level", event.level.name)

        if (showEmoji) {
            appendField("levelEmoji", event.level.emoji)
        }

        appendField("logger", event.loggerName)

        appendField("timestamp", event.timestamp, raw = true)

        event.message?.let { appendField("message", it) }

        if (event.attributes.isNotEmpty()) {
            appendField("attributes", mapToJson(event.attributes), raw = true)
        }

        if (event.context.values.isNotEmpty()) {
            appendField("context", mapToJson(event.context.values), raw = true)
        }

        event.throwable?.let { appendField("error", it.stackTraceToString()) }

        append('}')
    }

    private fun StringBuilder.appendField(
        key: String,
        value: Any,
        raw: Boolean = false
    ) {
        if (last() != '{') append(',')
        append('"').append(key).append("\":")
        if (raw) {
            append(value)
        } else {
            append('"').append(value.toString().escapeJson()).append('"')
        }
    }

    private fun mapToJson(map: Map<String, Any?>): String = buildString {
        append('{')
        map.entries.forEachIndexed { index, (k, v) ->
            if (index > 0) append(',')
            append('"').append(k.escapeJson()).append("\":")
            appendValue(v)
        }
        append('}')
    }

    private fun StringBuilder.appendValue(value: Any?) {
        when (value) {
            null -> append("null")
            is Number,
            is Boolean -> append(value)

            else -> append('"').append(value.toString().escapeJson()).append('"')
        }
    }
}

private fun String.escapeJson(): String = buildString(length) {
    for (c in this@escapeJson) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(c)
        }
    }
}