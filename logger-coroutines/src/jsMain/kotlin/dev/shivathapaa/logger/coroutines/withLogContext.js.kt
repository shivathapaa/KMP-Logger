package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext

/**
 * JS implementation.
 *
 * The runtime is single-threaded, but concurrent coroutines still interleave across
 * suspension points, so a shared ambient slot would hand one coroutine another's context.
 * The context is therefore carried in the coroutine context only. See
 * [withCoroutineOnlyLogContext].
 */
actual suspend fun <T> withLogContext(ctx: LogContext, block: suspend () -> T): T =
    withCoroutineOnlyLogContext(ctx, block)
