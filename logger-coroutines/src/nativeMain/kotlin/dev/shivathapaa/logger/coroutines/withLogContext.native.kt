package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext

/**
 * Kotlin/Native implementation, shared by iOS, macOS, Linux, and MinGW.
 *
 * Carries the context in the coroutine context only - never in [LogContextHolder] - so a
 * coroutine can never observe another coroutine's context. See [withCoroutineOnlyLogContext].
 */
actual suspend fun <T> withLogContext(ctx: LogContext, block: suspend () -> T): T =
    withCoroutineOnlyLogContext(ctx, block)
