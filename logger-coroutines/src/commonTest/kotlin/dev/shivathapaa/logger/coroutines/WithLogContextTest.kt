package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Top-level helper: captures currentCoroutineContext without being shadowed
// by coroutineContext from an enclosing runTest lambda.
private suspend fun currentCoroutineContext(): CoroutineContext = currentCoroutineContext()

class WithLogContextTest {

    @Test
    fun withLogContextInstallsContext() = runTest {
        val ctx = LogContext(mapOf("requestId" to "req-1"))

        withLogContext(ctx) {
            yield()
            assertEquals("req-1", LogContextHolder.current().values["requestId"])
        }
    }

    @Test
    fun withLogContextRestoresPreviousContextAfterBlock() = runTest {
        val ctx = LogContext(mapOf("requestId" to "req-1"))

        withLogContext(ctx) { yield() }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun withLogContextRestoresContextOnException() = runTest {
        val ctx = LogContext(mapOf("requestId" to "req-1"))

        assertFailsWith<RuntimeException> {
            withLogContext(ctx) {
                yield()
                throw RuntimeException("test error")
            }
        }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun withLogContextPropagatesReturnValue() = runTest {
        val ctx = LogContext(mapOf("k" to "v"))

        val result = withLogContext(ctx) {
            yield()
            42
        }

        assertEquals(42, result)
    }

    @Test
    fun nestedWithLogContextMergesContexts() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        withLogContext(outer) {
            withLogContext(inner) {
                yield()
                val current = LogContextHolder.current()
                assertEquals("t-1", current.values["traceId"])
                assertEquals("s-1", current.values["spanId"])
            }
        }
    }

    @Test
    fun nestedWithLogContextInnerOverridesOuterOnKeyCollision() = runTest {
        val outer = LogContext(mapOf("env" to "prod"))
        val inner = LogContext(mapOf("env" to "test"))

        withLogContext(outer) {
            withLogContext(inner) {
                yield()
                assertEquals("test", LogContextHolder.current().values["env"])
            }
            yield()
            assertEquals("prod", LogContextHolder.current().values["env"])
        }
    }

    @Test
    fun outerContextRestoredAfterNestedBlock() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        withLogContext(outer) {
            withLogContext(inner) { yield() }
            yield()
            val current = LogContextHolder.current()
            assertEquals("t-1", current.values["traceId"])
            assertEquals(null, current.values["spanId"])
        }
    }

    @Test
    fun logContextElementCanBeReadFromCoroutineContext() = runTest {
        val ctx = LogContext(mapOf("userId" to "u-1"))

        withLogContext(ctx) {
            yield()
            val element = currentCoroutineContext()[LogContextElement]
            assertEquals("u-1", element?.context?.values?.get("userId"))
        }
    }

    // Empty input 

    @Test
    fun withLogContextEmptyCtxDoesNotAddValues() = runTest {
        withLogContext(LogContext()) {
            yield()
            assertTrue(LogContextHolder.current().values.isEmpty())
        }
    }

    @Test
    fun withLogContextEmptyCtxPreservesExistingOuterValues() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        withLogContext(outer) {
            withLogContext(LogContext()) {
                yield()
                assertEquals("t-1", LogContextHolder.current().values["traceId"])
                assertEquals(1, LogContextHolder.current().values.size)
            }
        }
    }

    // Multiple keys in one call 

    @Test
    fun withLogContextMultipleKeysAllVisibleInsideBlock() = runTest {
        val ctx = LogContext(mapOf("traceId" to "t-1", "spanId" to "s-2", "userId" to "u-3"))

        withLogContext(ctx) {
            yield()
            val current = LogContextHolder.current()
            assertEquals("t-1", current.values["traceId"])
            assertEquals("s-2", current.values["spanId"])
            assertEquals("u-3", current.values["userId"])
        }
    }

    @Test
    fun withLogContextMultipleKeysAllRemovedAfterBlock() = runTest {
        val ctx = LogContext(mapOf("a" to "1", "b" to "2", "c" to "3"))

        withLogContext(ctx) { yield() }

        val current = LogContextHolder.current()
        assertNull(current.values["a"])
        assertNull(current.values["b"])
        assertNull(current.values["c"])
    }

    // Deep nesting 

    @Test
    fun withLogContextThreeLevelsDeepMergesAllContexts() = runTest {
        val level1 = LogContext(mapOf("level" to "1", "l1" to "a"))
        val level2 = LogContext(mapOf("level" to "2", "l2" to "b"))
        val level3 = LogContext(mapOf("level" to "3", "l3" to "c"))

        withLogContext(level1) {
            withLogContext(level2) {
                withLogContext(level3) {
                    yield()
                    val current = LogContextHolder.current()
                    assertEquals("3", current.values["level"]) // innermost wins
                    assertEquals("a", current.values["l1"])
                    assertEquals("b", current.values["l2"])
                    assertEquals("c", current.values["l3"])
                }
                yield()
                val current = LogContextHolder.current()
                assertEquals("2", current.values["level"])
                assertEquals("a", current.values["l1"])
                assertEquals("b", current.values["l2"])
                assertNull(current.values["l3"])
            }
            yield()
            val current = LogContextHolder.current()
            assertEquals("1", current.values["level"])
            assertEquals("a", current.values["l1"])
            assertNull(current.values["l2"])
            assertNull(current.values["l3"])
        }
    }

    @Test
    fun withLogContextThreeLevelsRestoredToEmptyAfterAll() = runTest {
        withLogContext(LogContext(mapOf("a" to "1"))) {
            withLogContext(LogContext(mapOf("b" to "2"))) {
                withLogContext(LogContext(mapOf("c" to "3"))) { yield() }
            }
        }
        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun withLogContextExceptionInDeepNestRestoresAllLevels() = runTest {
        assertFailsWith<RuntimeException> {
            withLogContext(LogContext(mapOf("a" to "1"))) {
                withLogContext(LogContext(mapOf("b" to "2"))) {
                    withLogContext(LogContext(mapOf("c" to "3"))) {
                        yield()
                        throw RuntimeException("deep failure")
                    }
                }
            }
        }
        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    // Coroutine-context element carries merged values 

    @Test
    fun coroutineContextElementHasAllMergedValues() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1", "shared" to "outer"))
        val inner = LogContext(mapOf("spanId" to "s-1", "shared" to "inner"))

        withLogContext(outer) {
            withLogContext(inner) {
                yield()
                val element = currentCoroutineContext()[LogContextElement]
                assertNotNull(element)
                assertEquals("t-1", element.context.values["traceId"])
                assertEquals("s-1", element.context.values["spanId"])
                assertEquals("inner", element.context.values["shared"])
            }
        }
    }

    // Return value edge cases 

    @Test
    fun withLogContextReturnsNullableValue() = runTest {
        val result: String? = withLogContext(LogContext(mapOf("k" to "v"))) {
            yield()
            null
        }
        assertNull(result)
    }

    @Test
    fun withLogContextReturnsUnitImplicitly() = runTest {
        var executed = false
        withLogContext(LogContext(mapOf("k" to "v"))) {
            yield()
            executed = true
        }
        assertTrue(executed)
    }
}