package dev.shivathapaa.logger.formatters

object LogFormatters {
    fun default(showEmoji: Boolean): LogEventFormatter = DefaultLogFormatter(showEmoji)

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

    fun compact(showEmoji: Boolean): LogEventFormatter = CompactLogFormatter(showEmoji)

    fun json(showEmoji: Boolean): LogEventFormatter = JsonLogFormatter(showEmoji)
}
