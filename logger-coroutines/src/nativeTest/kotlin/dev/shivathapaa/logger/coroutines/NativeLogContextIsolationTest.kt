package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Kotlin/Native has no `kotlinx.coroutines.ThreadContextElement`, so a log context cannot be
 * reinstalled into thread state when a coroutine resumes on another thread.
 *
 * [withLogContext] therefore keeps the context in the coroutine context and never writes it
 * to [LogContextHolder]. These tests pin the two guarantees that follow, both of which used
 * to be violated:
 *
 *  1. a coroutine can never observe another coroutine's context, and
 *  2. the context is still readable - correctly - via [currentLogContext].
 */
class NativeLogContextIsolationTest {

    @Test
    fun concurrentCoroutinesNeverObserveAnotherCoroutinesContext() = runBlocking {
        val observed = (0 until 8).map { i ->
            async(Dispatchers.Default) {
                withLogContext(LogContext(mapOf("owner" to "$i"))) {
                    delay(2)
                    LogContextHolder.current().values["owner"]
                }
            }
        }.awaitAll()

        // The ambient holder is never written on Native, so nothing may appear here. What
        // must never happen is seeing a *sibling's* value.
        observed.forEachIndexed { i, value ->
            assertTrue(
                value == null || value == "$i",
                "coroutine $i observed context owned by $value - contexts are leaking across threads"
            )
        }
    }

    @Test
    fun activeContextIsReadableAndCorrectPerCoroutine() = runBlocking {
        val observed = (0 until 8).map { i ->
            async(Dispatchers.Default) {
                withLogContext(LogContext(mapOf("owner" to "$i"))) {
                    delay(2)
                    currentLogContext().values["owner"]
                }
            }
        }.awaitAll()

        assertEquals((0 until 8).map { "$it" }, observed)
    }

    @Test
    fun nestedContextsMergeAcrossSuspension() = runBlocking {
        withLogContext(LogContext(mapOf("traceId" to "t-1"))) {
            delay(1)
            withLogContext(LogContext(mapOf("spanId" to "s-1"))) {
                delay(1)
                val ctx = currentLogContext().values
                assertEquals("t-1", ctx["traceId"])
                assertEquals("s-1", ctx["spanId"])
            }
            assertEquals(null, currentLogContext().values["spanId"])
            assertEquals("t-1", currentLogContext().values["traceId"])
        }
    }

    @Test
    fun innerContextOverridesOuterOnCollision() = runBlocking {
        withLogContext(LogContext(mapOf("stage" to "outer"))) {
            withLogContext(LogContext(mapOf("stage" to "inner"))) {
                delay(1)
                assertEquals("inner", currentLogContext().values["stage"])
            }
            assertEquals("outer", currentLogContext().values["stage"])
        }
    }
}
