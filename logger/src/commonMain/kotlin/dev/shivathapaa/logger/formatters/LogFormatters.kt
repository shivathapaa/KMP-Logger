package dev.shivathapaa.logger.formatters

object LogFormatters {
    fun default(): LogEventFormatter = DefaultLogFormatter

    fun pretty(
        includeTimestamp: Boolean = false,
        includeThread: Boolean = false,
        prettyPrint: Boolean = true
    ): LogEventFormatter =
        PrettyLogFormatter(
            includeTimestamp,
            includeThread,
            prettyPrint
        )

    fun compact(): LogEventFormatter = CompactLogFormatter

    fun json(): LogEventFormatter = JsonLogFormatter
}
