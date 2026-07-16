package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.core.LogContextHolder.withContext


/**
 * Holds and propagates the active [LogContext] for the current execution scope.
 *
 * This object is implemented per-platform to use the most appropriate
 * scoping mechanism (e.g. [ThreadLocal] on JVM/Android, a thread-local
 * stack on Apple targets).
 *
 * Contexts are scoped: the previous context is always restored after
 * [withContext] completes, even if the block throws. Nested calls merge
 * their contexts - inner values override outer values for the same key.
 *
 * Example:
 * ```kotlin
 * val requestContext = LogContext(mapOf("requestId" to "req-123"))
 *
 * val result = LogContextHolder.withContext(requestContext) {
 *     logger.info { "Handling request" }
 *     processRequest() // return value is propagated
 * }
 * ```
 */
expect object LogContextHolder {

    /**
     * Returns the currently active [LogContext] for the current thread or
     * execution scope. Returns an empty [LogContext] if none has been set.
     */
    fun current(): LogContext

    /**
     * Executes [block] with [ctx] merged into the current context, then
     * restores the previous context regardless of whether [block] throws.
     *
     * The merged context is formed by overlaying [ctx] on top of the current
     * context - keys present in [ctx] override existing keys, while keys
     * not present in [ctx] are preserved from the outer context.
     *
     * @param ctx The context to merge into the current context for the
     *   duration of [block].
     * @param block The block of code to execute within the merged context.
     * @return The value returned by [block].
     */
    fun <T> withContext(ctx: LogContext, block: () -> T): T

    /**
     * Suspending variant of [withContext] for use in coroutines. Executes [block] with [ctx] merged
     * into the current context, then restores the previous context regardless
     * of whether [block] throws or suspends.
     *
     * **Thread-safety warning (JVM/Android):** This implementation uses a `ThreadLocal` to hold
     * the context. If the coroutine suspends and resumes on a different thread
     * (e.g. with `Dispatchers.IO` or `Dispatchers.Default`), the restored context
     * will not be visible on the new thread. Concurrent coroutines that yield and
     * interleave on the same dispatcher can also observe each other's context.
     *
     * For correct context propagation across suspension points and thread hops,
     * prefer [withLogContext][dev.shivathapaa.logger.coroutines.withLogContext]
     * from the `logger-coroutines` module, which uses
     * [LogContextElement][dev.shivathapaa.logger.coroutines.LogContextElement]
     * (a `ThreadContextElement` on JVM/Android) to install and restore the context
     * automatically on every thread the coroutine resumes on.
     *
     * This function is safe to use only when:
     * - The coroutine always resumes on the same thread (e.g. `Dispatchers.Main`), **and**
     * - No other coroutines run concurrently on the same dispatcher.
     *
     * Neither condition can be enforced or detected by this function, and violating either
     * yields a *wrong* context rather than a missing one - so it is deprecated. Prefer a
     * mechanism that cannot be wrong:
     * - [Logger.withContext][dev.shivathapaa.logger.api.Logger.withContext] binds the context
     *   to the logger object; correct on every platform and dispatcher.
     * - [withLogContext][dev.shivathapaa.logger.coroutines.withLogContext] carries it in the
     *   coroutine context, and on JVM/Android additionally keeps this holder in sync.
     *
     * @param ctx The context to merge into the current context for the
     *   duration of [block].
     * @param block The suspending block of code to execute within the merged context.
     * @return The value returned by [block].
     */
    @Deprecated(
        message = "Unsafe: the context is held in thread state across suspension, so a " +
                "coroutine that resumes on another thread - or interleaves with a sibling - " +
                "can observe the wrong context. Bind the context to the logger with " +
                "Logger.withContext(ctx), or use withLogContext from logger-coroutines.",
        level = DeprecationLevel.WARNING
    )
    suspend fun <T> withSuspendingContext(ctx: LogContext, block: suspend () -> T): T

    /**
     * Directly sets the active [LogContext] for the current thread or execution scope,
     * replacing any previously installed context without merging.
     *
     * Passing an empty [LogContext] clears the active context entirely (equivalent
     * to removing it from the current scope).
     *
     * **This is an internal API** used exclusively by the `logger-coroutines` module, and
     * only on **JVM/Android**, where it is called by
     * [LogContextElement][dev.shivathapaa.logger.coroutines.LogContextElement]'s
     * `ThreadContextElement.updateThreadContext` and `restoreThreadContext` hooks, which the
     * coroutine dispatcher invokes automatically on each thread switch.
     *
     * On every other target `ThreadContextElement` does not exist, so nothing can keep this
     * holder in sync with coroutine resumption. `withLogContext` deliberately does **not**
     * write here on those targets: doing so handed one coroutine another coroutine's context.
     *
     * Do not call this from application code. Use [withContext] or
     * [withLogContext][dev.shivathapaa.logger.coroutines.withLogContext] instead.
     */
    @InternalLoggerApi
    fun setContext(ctx: LogContext)
}