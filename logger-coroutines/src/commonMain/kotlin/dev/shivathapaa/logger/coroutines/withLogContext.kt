package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext

/**
 * Executes [block] with [ctx] merged into the current log context, propagating it
 * correctly across suspension points on all platforms.
 *
 * On **JVM and Android** the merged context is stored as a
 * [LogContextElement] (a `ThreadContextElement`) in the coroutine context.
 * The coroutines runtime installs and restores it via
 * [LogContextHolder][dev.shivathapaa.logger.core.LogContextHolder] on every thread
 * the coroutine resumes on, so it works correctly with `Dispatchers.IO`,
 * `Dispatchers.Default`, and any other multi-threaded dispatcher.
 *
 * On **JS, WasmJS, iOS, macOS, Linux, and MinGW** the context is set directly
 * on [LogContextHolder][dev.shivathapaa.logger.core.LogContextHolder] for the
 * duration of the block (safe because those platforms are single-threaded).
 *
 * The previous context is always restored after [block] completes, even if
 * [block] throws.
 *
 * Nested calls merge their contexts  -  inner values override outer values for
 * the same key.
 *
 * Example:
 * ```kotlin
 * withLogContext(LogContext(mapOf("requestId" to "req-123"))) {
 *     delay(100) // safe  -  context survives the suspension
 *     logger.info { "Handling request" }
 * }
 * ```
 *
 * @param ctx The context to merge into the current context for the duration of [block].
 * @param block The suspending block to execute within the merged context.
 * @return The value returned by [block].
 */
expect suspend fun <T> withLogContext(ctx: LogContext, block: suspend () -> T): T