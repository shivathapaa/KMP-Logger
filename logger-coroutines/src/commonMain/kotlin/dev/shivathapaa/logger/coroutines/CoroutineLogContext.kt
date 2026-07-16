package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.api.Logger
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * The [LogContext] carried by the current coroutine, or an empty context if none is active.
 *
 * This reads the [kotlin.coroutines.CoroutineContext] rather than any thread-local state, so
 * it is correct on **every** platform and under any dispatcher - including multi-threaded
 * dispatchers on Kotlin/Native, where ambient propagation is not possible.
 */
suspend fun currentLogContext(): LogContext =
    coroutineContext[LogContextElement]?.context ?: LogContext()

/**
 * Returns a [Logger] bound to the coroutine's active [LogContext].
 *
 * This is the portable way to log inside a [withLogContext] block: the context is read from
 * the coroutine context and bound to the logger object, so every event it emits carries the
 * context on every platform.
 *
 * ```kotlin
 * withLogContext(LogContext(mapOf("requestId" to id))) {
 *     val log = LoggerFactory.get("Api").withActiveLogContext()
 *     log.info { "Starting" }              // carries requestId on every platform
 *
 *     withContext(Dispatchers.Default) {
 *         log.debug { "Working" }          // still carries it - it is bound to the object
 *     }
 * }
 * ```
 *
 * On JVM/Android the ambient context is also visible to plain, unbound loggers (see
 * [withLogContext]); binding additionally works everywhere else.
 */
suspend fun Logger.withActiveLogContext(): Logger = this.withContext(currentLogContext())

/**
 * Shared implementation of [withLogContext] for every target that has no
 * `kotlinx.coroutines.ThreadContextElement` - that is, everything except JVM and Android.
 *
 * The merged context is carried **only** in the [kotlin.coroutines.CoroutineContext], via
 * [LogContextElement]. It is deliberately **not** written into [LogContextHolder].
 *
 * Writing it there is what previously made these targets return *another coroutine's*
 * context: the holder is thread state, nothing reinstalls it when a coroutine resumes, and
 * pool threads are reused - so coroutine B would write its context into thread T's slot and
 * coroutine A would then read B's context after resuming on T. Carrying the context in the
 * coroutine context instead makes that class of bug impossible: nesting stays correct across
 * suspension and thread hops, and no coroutine can observe another's context.
 *
 * The trade-off is explicit: a plain non-suspending `logger.info { }` cannot see this
 * context, because reading the coroutine context requires `suspend`. Use
 * [withActiveLogContext] or [Logger.withContext] to attach it to a logger.
 */
internal suspend fun <T> withCoroutineOnlyLogContext(
    ctx: LogContext,
    block: suspend () -> T
): T {
    // Prefer the enclosing coroutine's context so nesting is correct across thread hops;
    // fall back to the ambient holder for entry from a synchronous LogContextHolder scope.
    val parent = coroutineContext[LogContextElement]?.context ?: LogContextHolder.current()
    return withContext(LogContextElement(parent.merge(ctx))) { block() }
}
