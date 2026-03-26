package dev.shivathapaa.logger.core

actual object LogContextHolder {
    private val holder = ThreadLocal<LogContext>()

    actual fun current(): LogContext = holder.get() ?: LogContext()

    actual fun <T> withContext(ctx: LogContext, block: () -> T): T {
        val previous = current()
        holder.set(previous.merge(ctx))
        try {
            return block()
        } finally {
            holder.set(previous)
        }
    }
}