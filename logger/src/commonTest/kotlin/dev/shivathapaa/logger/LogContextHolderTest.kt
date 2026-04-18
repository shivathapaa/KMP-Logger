package dev.shivathapaa.logger

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LogContextHolderTest {

    @Test
    fun currentReturnsEmptyContextByDefault() {
        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun withContextInstallsContext() {
        val ctx = LogContext(mapOf("requestId" to "req-1"))

        LogContextHolder.withContext(ctx) {
            assertEquals("req-1", LogContextHolder.current().values["requestId"])
        }
    }

    @Test
    fun withContextRestoresPreviousContextAfterBlock() {
        val ctx = LogContext(mapOf("requestId" to "req-1"))

        LogContextHolder.withContext(ctx) { }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun withContextRestoresContextOnException() {
        val ctx = LogContext(mapOf("requestId" to "req-1"))

        assertFailsWith<RuntimeException> {
            LogContextHolder.withContext(ctx) {
                throw RuntimeException("test error")
            }
        }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun nestedContextsMerge() {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        LogContextHolder.withContext(outer) {
            LogContextHolder.withContext(inner) {
                val current = LogContextHolder.current()
                assertEquals("t-1", current.values["traceId"])
                assertEquals("s-1", current.values["spanId"])
            }
        }
    }

    @Test
    fun nestedContextInnerOverridesOuterOnKeyCollision() {
        val outer = LogContext(mapOf("env" to "prod"))
        val inner = LogContext(mapOf("env" to "test"))

        LogContextHolder.withContext(outer) {
            LogContextHolder.withContext(inner) {
                assertEquals("test", LogContextHolder.current().values["env"])
            }
            assertEquals("prod", LogContextHolder.current().values["env"])
        }
    }

    @Test
    fun withContextPropagatesReturnValue() {
        val ctx = LogContext(mapOf("k" to "v"))

        val result = LogContextHolder.withContext(ctx) { 42 }

        assertEquals(42, result)
    }

    @Test
    fun suspendWithContextInstallsContext() = runTest {
        val ctx = LogContext(mapOf("requestId" to "req-suspend"))

        LogContextHolder.withSuspendingContext(ctx) {
            yield()
            assertEquals("req-suspend", LogContextHolder.current().values["requestId"])
        }
    }

    @Test
    fun suspendWithContextRestoresPreviousContextAfterBlock() = runTest {
        val ctx = LogContext(mapOf("requestId" to "req-suspend"))

        LogContextHolder.withSuspendingContext(ctx) { yield() }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun suspendWithContextRestoresContextOnException() = runTest {
        val ctx = LogContext(mapOf("requestId" to "req-suspend"))

        assertFailsWith<RuntimeException> {
            LogContextHolder.withSuspendingContext(ctx) {
                yield()
                throw RuntimeException("test error")
            }
        }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun suspendWithContextPropagatesReturnValue() = runTest {
        val ctx = LogContext(mapOf("k" to "v"))

        val result = LogContextHolder.withSuspendingContext(ctx) {
            yield()
            42
        }

        assertEquals(42, result)
    }

    @Test
    fun suspendWithContextNestedMerge() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        LogContextHolder.withSuspendingContext(outer) {
            LogContextHolder.withSuspendingContext(inner) {
                yield()
                val current = LogContextHolder.current()
                assertEquals("t-1", current.values["traceId"])
                assertEquals("s-1", current.values["spanId"])
            }
        }
    }

    @Test
    fun outerContextRestoredAfterNestedBlock() {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        LogContextHolder.withContext(outer) {
            LogContextHolder.withContext(inner) { }
            val current = LogContextHolder.current()
            assertEquals("t-1", current.values["traceId"])
            assertEquals(null, current.values["spanId"])
        }
    }
}