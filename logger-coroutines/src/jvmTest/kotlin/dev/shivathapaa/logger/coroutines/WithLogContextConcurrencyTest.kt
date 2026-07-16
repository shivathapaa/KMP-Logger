package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// JVM-only: LogContextElement is a real ThreadContextElement here, so the context is
// reinstalled by the dispatcher on every thread the coroutine resumes on.
//
// The `runTest` cases below cover *interleaved isolation* on a single-threaded test
// dispatcher. The `runBlocking` cases cover *thread-hop survival* on real multi-threaded
// dispatchers (Dispatchers.IO / Dispatchers.Default) - which is the guarantee that does
// NOT hold on the other targets, where the element is a plain CoroutineContext.Element.
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

    @Test
    fun withLogContextSurvivesThreadHopsOnRealDispatchers() = runBlocking {
        val threads = mutableSetOf<String>()

        withLogContext(LogContext(mapOf("requestId" to "req-1"))) {
            threads += Thread.currentThread().name

            val onIo = withContext(Dispatchers.IO) {
                threads += Thread.currentThread().name
                delay(1)
                LogContextHolder.current().values["requestId"]
            }
            assertEquals("req-1", onIo)

            val onDefault = withContext(Dispatchers.Default) {
                threads += Thread.currentThread().name
                delay(1)
                LogContextHolder.current().values["requestId"]
            }
            assertEquals("req-1", onDefault)
        }

        // Prove the hop actually happened - otherwise the assertions above are vacuous.
        assertTrue(threads.size > 1, "expected to run on more than one thread, saw $threads")
        assertNull(LogContextHolder.current().values["requestId"])
    }

    @Test
    fun withLogContextIsolatesConcurrentCoroutinesAcrossRealThreads() = runBlocking {
        val results = (0 until 8).map { i ->
            async(Dispatchers.Default) {
                withLogContext(LogContext(mapOf("id" to "$i"))) {
                    yield()
                    delay(1)
                    LogContextHolder.current().values["id"]
                }
            }
        }.awaitAll()

        assertEquals((0 until 8).map { "$it" }, results)
    }
}