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

    @Deprecated(
        message = "Unsafe: the context is held in thread state across suspension, so a " +
                "coroutine that resumes on another thread - or interleaves with a sibling - " +
                "can observe the wrong context. Bind the context to the logger with " +
                "Logger.withContext(ctx), or use withLogContext from logger-coroutines.",
        level = DeprecationLevel.WARNING
    )
    actual suspend fun <T> withSuspendingContext(ctx: LogContext, block: suspend () -> T): T {
        val previous = current()
        holder.set(previous.merge(ctx))
        try {
            return block()
        } finally {
            holder.set(previous)
        }
    }

    @InternalLoggerApi
    actual fun setContext(ctx: LogContext) {
        if (ctx.values.isEmpty()) holder.remove() else holder.set(ctx)
    }
}