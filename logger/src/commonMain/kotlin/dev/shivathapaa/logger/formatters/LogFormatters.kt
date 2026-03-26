package dev.shivathapaa.logger.formatters

/**
 * Factory object for obtaining standard [LogEventFormatter] implementations.
 *
 * Each factory method returns a pre-configured formatter suitable for common
 * output scenarios. Custom formatters can be created by implementing
 * [LogEventFormatter] directly or using its `fun interface` syntax:
 *
 * ```kotlin
 * val myFormatter = LogEventFormatter { event ->
 *     "[${event.level}] ${event.loggerName}: ${event.message}"
 * }
 * ```
 */
object LogFormatters {

    /**
     * Returns a formatter that produces a concise, human-readable single-line
     * output containing the log level, logger name, message, and stack trace
     * (if a throwable is present).
     *
     * @param showEmoji Whether to prefix the line with the level's emoji indicator.
     */
    fun default(showEmoji: Boolean): LogEventFormatter = DefaultLogFormatter(showEmoji)

    /**
     * Returns a formatter that produces a detailed, human-readable output with
     * optional timestamp, thread name, and pretty-printed attributes and context.
     *
     * @param showEmoji Whether to prefix the line with the level's emoji indicator.
     * @param includeTimestamp Whether to include the event timestamp.
     * @param includeThread Whether to include the thread name.
     * @param prettyPrint Whether to format attributes and context as indented
     *   multi-line blocks (`true`) or as a compact inline string (`false`).
     */
    fun pretty(
        showEmoji: Boolean,
        includeTimestamp: Boolean = false,
        includeThread: Boolean = false,
        prettyPrint: Boolean = true,
    ): LogEventFormatter = PrettyLogFormatter(
        includeTimestamp = includeTimestamp,
        includeThread = includeThread,
        prettyPrint = prettyPrint,
        showEmoji = showEmoji
    )

    /**
     * Returns a formatter that produces a compact single-line output including
     * the log level, logger name, message, and inline attributes.
     *
     * @param showEmoji Whether to prefix the line with the level's emoji indicator.
     */
    fun compact(showEmoji: Boolean): LogEventFormatter = CompactLogFormatter(showEmoji)

    /**
     * Returns a formatter that serializes each log event as a single-line JSON
     * object, suitable for ingestion by log aggregation platforms such as
     * Elasticsearch, Datadog, Splunk, and Loki.
     *
     * The output is always valid, standards-compliant JSON. When [showEmoji]
     * is `true`, the level's emoji is included as a dedicated `"levelEmoji"`
     * field inside the JSON object rather than outside it, preserving validity.
     *
     * @param showEmoji Whether to include a `"levelEmoji"` field in the JSON output.
     */
    fun json(showEmoji: Boolean): LogEventFormatter = JsonLogFormatter(showEmoji)
}