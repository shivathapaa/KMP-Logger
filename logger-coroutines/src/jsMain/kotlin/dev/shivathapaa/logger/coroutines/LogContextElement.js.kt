package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

actual class LogContextElement actual constructor(actual val context: LogContext) :
    AbstractCoroutineContextElement(Key) {

    actual companion object Key : CoroutineContext.Key<LogContextElement>
}