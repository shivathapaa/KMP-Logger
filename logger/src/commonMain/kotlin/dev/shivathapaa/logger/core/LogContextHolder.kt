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
     * **JVM/Android note:** This implementation uses a `ThreadLocal` to hold
     * the context. If the coroutine suspends and resumes on a different thread
     * (e.g. with `Dispatchers.IO`), the context will not propagate to the
     * resumed thread. Use with single-threaded dispatchers (e.g.
     * `Dispatchers.Main`) or ensure the coroutine does not migrate threads
     * across suspension points.
     *
     * @param ctx The context to merge into the current context for the
     *   duration of [block].
     * @param block The suspending block of code to execute within the merged context.
     * @return The value returned by [block].
     */
    suspend fun <T> withSuspendingContext(ctx: LogContext, block: suspend () -> T): T
}