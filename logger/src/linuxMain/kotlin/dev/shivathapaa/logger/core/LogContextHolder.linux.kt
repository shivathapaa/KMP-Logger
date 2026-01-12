package dev.shivathapaa.logger.core

actual object LogContextHolder {
    private val stack = mutableListOf<LogContext>()

    actual fun current(): LogContext =
        stack.lastOrNull() ?: LogContext()

    actual fun withContext(ctx: LogContext, block: () -> Unit) {
        val merged = current().merge(ctx)
        stack.add(merged)
        try {
            block()
        } finally {
            stack.removeLast()
        }
    }
}