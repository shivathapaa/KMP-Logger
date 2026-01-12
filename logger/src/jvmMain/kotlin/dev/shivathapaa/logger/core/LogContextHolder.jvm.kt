package dev.shivathapaa.logger.core

actual object LogContextHolder {
    private val holder = ThreadLocal<LogContext>()

    actual fun current(): LogContext =
        holder.get() ?: LogContext()

    actual fun withContext(ctx: LogContext, block: () -> Unit) {
        val previous = current()
        val merged = previous.merge(ctx)

        holder.set(merged)
        try {
            block()
        } finally {
            holder.set(previous)
        }
    }
}