package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.InternalLoggerApi
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.withContext

@OptIn(InternalLoggerApi::class)
actual suspend fun <T> withLogContext(ctx: LogContext, block: suspend () -> T): T {
    val merged = LogContextHolder.current().merge(ctx)
    val previous = LogContextHolder.current()
    LogContextHolder.setContext(merged)
    return try {
        withContext(LogContextElement(merged)) { block() }
    } finally {
        LogContextHolder.setContext(previous)
    }
}