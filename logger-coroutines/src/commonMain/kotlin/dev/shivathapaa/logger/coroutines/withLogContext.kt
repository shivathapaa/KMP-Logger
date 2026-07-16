package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext

/**
 * Executes [block] with [ctx] merged into the active log context.
 *
 * ### The portable guarantee (every platform)
 * The merged context is carried in the [kotlin.coroutines.CoroutineContext] as a
 * [LogContextElement]. It therefore survives suspension and thread hops, nests correctly
 * (inner values override outer ones), and **no coroutine can ever observe another
 * coroutine's context**. Read it with [currentLogContext], or attach it to a logger with
 * [withActiveLogContext]:
 *
 * ```kotlin
 * withLogContext(LogContext(mapOf("requestId" to "req-123"))) {
 *     val log = LoggerFactory.get("Api").withActiveLogContext()
 *     delay(100)
 *     log.info { "Handling request" }   // carries requestId on every platform
 * }
 * ```
 *
 * ### The JVM/Android bonus
 * There, [LogContextElement] is a real `kotlinx.coroutines.ThreadContextElement`, so the
 * dispatcher also mirrors the context into
 * [LogContextHolder][dev.shivathapaa.logger.core.LogContextHolder] on every thread the
 * coroutine resumes on. Plain **unbound** loggers therefore see it too, with no suspend call:
 *
 * ```kotlin
 * withLogContext(LogContext(mapOf("requestId" to "req-123"))) {
 *     logger.info { "Handling request" }   // carries requestId - JVM/Android only
 * }
 * ```
 *
 * That mirroring is impossible elsewhere: `ThreadContextElement` exists only on JVM, so
 * nothing can keep thread state in sync with coroutine resumption. Writing there anyway is
 * what used to hand one coroutine another's context, so on other targets this function
 * deliberately does not touch the holder.
 *
 * **Portable rule:** if the code must behave identically on every target, bind the context
 * to the logger - via [withActiveLogContext] or
 * [Logger.withContext][dev.shivathapaa.logger.api.Logger.withContext] - rather than relying
 * on ambient visibility.
 *
 * @param ctx The context to merge into the active context for the duration of [block].
 * @param block The suspending block to execute within the merged context.
 * @return The value returned by [block].
 */
expect suspend fun <T> withLogContext(ctx: LogContext, block: suspend () -> T): T