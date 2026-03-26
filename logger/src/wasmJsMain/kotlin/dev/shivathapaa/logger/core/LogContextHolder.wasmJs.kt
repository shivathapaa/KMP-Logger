package dev.shivathapaa.logger.core

actual object LogContextHolder {
    private var current: LogContext = LogContext()

    actual fun current(): LogContext = current

    actual fun <T> withContext(ctx: LogContext, block: () -> T): T {
        val previous = current
        current = previous.merge(ctx)
        try {
            return block()
        } finally {
            current = previous
        }
    }
}