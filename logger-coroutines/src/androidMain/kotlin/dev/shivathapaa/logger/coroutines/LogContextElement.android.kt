package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.InternalLoggerApi
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

actual class LogContextElement actual constructor(actual val context: LogContext) :
    ThreadContextElement<LogContext>,
    AbstractCoroutineContextElement(Key) {

    actual companion object Key : CoroutineContext.Key<LogContextElement>

    @OptIn(InternalLoggerApi::class)
    override fun updateThreadContext(context: CoroutineContext): LogContext {
        val previous = LogContextHolder.current()
        LogContextHolder.setContext(this.context)
        return previous
    }

    @OptIn(InternalLoggerApi::class)
    override fun restoreThreadContext(context: CoroutineContext, oldState: LogContext) {
        LogContextHolder.setContext(oldState)
    }
}