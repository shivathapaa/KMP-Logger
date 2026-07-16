package dev.shivathapaa.logger.core

import kotlin.native.concurrent.ThreadLocal

/**
 * Kotlin/Native coroutines are **not** single-threaded - `Dispatchers.Default` is a
 * multi-threaded worker pool. Without [ThreadLocal] this object's state would be one
 * process-wide slot, so concurrent coroutines on different threads would read each
 * other's context - i.e. silently *wrong* context data.
 *
 * [ThreadLocal] gives each thread its own slot, matching the `ThreadLocal` used on
 * JVM/Android and `NSThread.threadDictionary` on Apple. A coroutine that resumes on a
 * different thread therefore sees an *empty* context rather than another coroutine's:
 * it fails closed. Reinstalling context across a thread hop needs a
 * `ThreadContextElement`, which kotlinx.coroutines only ships on JVM.
 */
@ThreadLocal
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