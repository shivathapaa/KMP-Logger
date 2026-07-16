package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Kotlin/Native implementation, shared by iOS, macOS, Linux, and MinGW.
 *
 * `kotlinx.coroutines.ThreadContextElement` does not exist on Kotlin/Native, so this element
 * cannot reinstall the context when a coroutine resumes. It is a plain carrier: the context
 * travels with the coroutine, and is read via [currentLogContext] or bound to a logger with
 * [withActiveLogContext].
 */
actual class LogContextElement actual constructor(actual val context: LogContext) :
    AbstractCoroutineContextElement(Key) {

    actual companion object Key : CoroutineContext.Key<LogContextElement>
}
