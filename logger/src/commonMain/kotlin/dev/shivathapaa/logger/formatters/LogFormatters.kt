package dev.shivathapaa.logger.formatters

/**
 * Factory for creating standard [LogEventFormatter] implementations.
 */
object LogFormatters {
    /**
     * Creates a default formatter.
     *
     * @param showEmoji Whether to show log level emojis.
     */
    fun default(showEmoji: Boolean): LogEventFormatter = DefaultLogFormatter(showEmoji)

    /**
     * Creates a pretty formatter with various options for including metadata.
     *
     * @param showEmoji Whether to show log level emojis.
     * @param includeTimestamp Whether to include the event timestamp.
     * @param includeThread Whether to include the thread name.
     * @param prettyPrint Whether to use multi-line pretty printing for attributes and context.
     */
    fun pretty(
        showEmoji: Boolean,
        includeTimestamp: Boolean = false,
        includeThread: Boolean = false,
        prettyPrint: Boolean = true,
    ): LogEventFormatter =
        PrettyLogFormatter(
            includeTimestamp,
            includeThread,
            prettyPrint,
            showEmoji
        )

    /**
     * Creates a compact formatter, useful for minimal output.
     *
     * @param showEmoji Whether to show log level emojis.
     */
    fun compact(showEmoji: Boolean): LogEventFormatter = CompactLogFormatter(showEmoji)

    /**
     * Creates a JSON formatter that outputs each log event as a JSON string.
     *
     * @param showEmoji Whether to include emojis in the JSON output.
     */
    fun json(showEmoji: Boolean): LogEventFormatter = JsonLogFormatter(showEmoji)
}
