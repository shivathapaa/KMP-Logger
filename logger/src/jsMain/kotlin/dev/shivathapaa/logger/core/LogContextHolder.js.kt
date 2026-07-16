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

    @Deprecated(
        message = "Unsafe: the context is held in thread state across suspension, so a " +
                "coroutine that resumes on another thread - or interleaves with a sibling - " +
                "can observe the wrong context. Bind the context to the logger with " +
                "Logger.withContext(ctx), or use withLogContext from logger-coroutines.",
        level = DeprecationLevel.WARNING
    )
    actual suspend fun <T> withSuspendingContext(ctx: LogContext, block: suspend () -> T): T {
        val previous = current
        current = previous.merge(ctx)
        try {
            return block()
        } finally {
            current = previous
        }
    }

    @InternalLoggerApi
    actual fun setContext(ctx: LogContext) {
        current = ctx
    }
}