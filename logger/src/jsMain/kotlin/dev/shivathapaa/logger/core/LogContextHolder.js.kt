package dev.shivathapaa.logger.core

actual object LogContextHolder {
    private var currentContext: LogContext = LogContext()

    actual fun current(): LogContext = currentContext

    actual fun withContext(ctx: LogContext, block: () -> Unit) {
        val previous = current()
        val merged = previous.merge(ctx)

        currentContext = merged
        try {
            block()
        } finally {
            currentContext = previous
        }
    }
}