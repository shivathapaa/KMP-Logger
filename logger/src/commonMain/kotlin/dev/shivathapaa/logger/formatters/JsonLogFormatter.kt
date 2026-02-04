package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

/**
 * A formatter that represents each [LogEvent] as a JSON string.
 * This is particularly useful for machine-readable logging or cloud logging services.
 *
 * @property showEmoji Whether to prepend the log level emoji to the JSON string.
 */
internal class JsonLogFormatter(private val showEmoji: Boolean) : LogEventFormatter {
    override fun format(event: LogEvent): String = buildString {
        val emoji = if (showEmoji) "${event.level.emoji} " else ""
        append(emoji)
        append('{')
        appendField("level", event.level.name)
        appendField("logger", event.loggerName)

        event.timestamp?.let {
            appendField("timestamp", it)
        }

        event.message?.let {
            appendField("message", it)
        }

        if (event.attributes.isNotEmpty()) {
            appendField("attributes", mapToJson(event.attributes), raw = true)
        }

        if (event.context.values.isNotEmpty()) {
            appendField("context", mapToJson(event.context.values), raw = true)
        }

        event.throwable?.let {
            appendField("error", it.stackTraceToString())
        }

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

    private fun mapToJson(map: Map<String, Any?>): String =
        buildString {
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
            is Number, is Boolean -> append(value)
            else -> append('"').append(value.toString().escapeJson()).append('"')
        }
    }
}

private fun String.escapeJson(): String =
    buildString(length) {
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