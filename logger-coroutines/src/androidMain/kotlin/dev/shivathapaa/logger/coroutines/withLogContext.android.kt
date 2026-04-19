package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.withContext

actual suspend fun <T> withLogContext(ctx: LogContext, block: suspend () -> T): T {
    val merged = LogContextHolder.current().merge(ctx)
    return withContext(LogContextElement(merged)) { block() }
}