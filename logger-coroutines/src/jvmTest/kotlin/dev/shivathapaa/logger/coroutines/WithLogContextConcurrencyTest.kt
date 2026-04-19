package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// JVM-only: ThreadContextElement guarantees context isolation across concurrent coroutines
// even when they yield and are interleaved on the same or different threads. This is a
// JVM/Android feature single-threaded platforms share one context slot and cannot
// isolate concurrent coroutines this way.
class WithLogContextConcurrencyTest {

    @Test
    fun withLogContextConcurrentCoroutinesHaveIsolatedContexts() = runTest {
        val results = (0 until 3).map { i ->
            async {
                withLogContext(LogContext(mapOf("coroutineId" to "$i"))) {
                    yield()
                    LogContextHolder.current().values["coroutineId"]
                }
            }
        }.awaitAll()

        assertEquals(listOf("0", "1", "2"), results)
    }

    @Test
    fun withLogContextConcurrentCoroutinesDoNotLeakContextToSiblings() = runTest {
        val results = (0 until 3).map { i ->
            async {
                withLogContext(LogContext(mapOf("id" to "$i", "only-$i" to "yes"))) {
                    yield()
                    val sibling = (i + 1) % 3
                    LogContextHolder.current().values["only-$sibling"]
                }
            }
        }.awaitAll()

        assertTrue(results.all { it == null })
    }

    @Test
    fun withLogContextContextRestoredAfterConcurrentCoroutineCompletes() = runTest {
        val deferred = async {
            withLogContext(LogContext(mapOf("bg" to "job"))) {
                yield()
                LogContextHolder.current().values["bg"]
            }
        }

        withLogContext(LogContext(mapOf("fg" to "main"))) {
            yield()
            assertEquals("main", LogContextHolder.current().values["fg"])
            assertNull(LogContextHolder.current().values["bg"])
        }

        assertEquals("job", deferred.await())
        assertNull(LogContextHolder.current().values["bg"])
        assertNull(LogContextHolder.current().values["fg"])
    }
}