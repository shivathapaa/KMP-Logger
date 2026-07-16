package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineContext.Element] that carries a [LogContext] through a coroutine's lifetime.
 *
 * ### JVM and Android
 * Also implements `ThreadContextElement<LogContext>`. The coroutine dispatcher automatically
 * calls [LogContextHolder.setContext][dev.shivathapaa.logger.core.LogContextHolder.setContext]
 * via `updateThreadContext` before each dispatch and restores the previous context via
 * `restoreThreadContext` after. This means the [LogContext] is correctly visible in
 * [LogContextHolder.current()][dev.shivathapaa.logger.core.LogContextHolder.current] on
 * every thread the coroutine runs on, including after suspension points that hop to a
 * different thread-pool thread (e.g. `Dispatchers.IO` or `Dispatchers.Default`).
 * Concurrent coroutines on JVM are also fully isolated from each other.
 *
 * ### Other platforms (JS, WasmJS, iOS, macOS, Linux, MinGW)
 * `kotlinx.coroutines.ThreadContextElement` does not exist outside JVM, so here the element
 * is a plain carrier: it holds the [LogContext] in the coroutine context and nothing
 * mirrors it into [LogContextHolder]. The context still survives suspension and thread hops
 * - it travels with the coroutine - and coroutines remain isolated from each other. It is
 * simply not visible to a non-suspending `LogContextHolder.current()` read, because
 * resolving the coroutine context requires `suspend`.
 *
 * Read it with [currentLogContext], or attach it to a logger with [withActiveLogContext].
 *
 * ### Accessing the element
 * The active element is always retrievable from inside a [withLogContext] block:
 * ```kotlin
 * withLogContext(LogContext(mapOf("requestId" to "req-1"))) {
 *     val element = kotlinx.coroutines.currentCoroutineContext()[LogContextElement]
 *     println(element?.context)  // LogContext(values={requestId=req-1})
 * }
 * ```
 *
 * Prefer [withLogContext] over constructing this element directly. Direct construction
 * is useful when attaching a context to an entire [kotlinx.coroutines.CoroutineScope]:
 * ```kotlin
 * val scope = CoroutineScope(
 *     Dispatchers.IO + LogContextElement(LogContext(mapOf("service" to "api")))
 * )
 * ```
 */
expect class LogContextElement(context: LogContext) : CoroutineContext.Element {
    val context: LogContext
    override val key: CoroutineContext.Key<*>

    companion object Key : CoroutineContext.Key<LogContextElement>
}