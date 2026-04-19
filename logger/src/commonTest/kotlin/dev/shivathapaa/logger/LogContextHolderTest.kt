package dev.shivathapaa.logger

import dev.shivathapaa.logger.core.InternalLoggerApi
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogContextHolderTest {

    @OptIn(InternalLoggerApi::class)
    @AfterTest
    fun cleanup() {
        LogContextHolder.setContext(LogContext())
    }

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

    // setContext 

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextWithNonEmptyContextSetsCurrentContext() {
        LogContextHolder.setContext(LogContext(mapOf("key" to "value")))
        assertEquals("value", LogContextHolder.current().values["key"])
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextWithEmptyContextClearsCurrentContext() {
        LogContextHolder.setContext(LogContext(mapOf("key" to "value")))
        LogContextHolder.setContext(LogContext())
        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextOverridesPreviousContextWithoutMerging() {
        LogContextHolder.setContext(LogContext(mapOf("a" to "1", "b" to "2")))
        LogContextHolder.setContext(LogContext(mapOf("a" to "override")))

        val current = LogContextHolder.current()
        assertEquals("override", current.values["a"])
        assertNull(current.values["b"]) // "b" not merged  completely replaced
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextRetainsExactValues() {
        val ctx = LogContext(mapOf("x" to "1", "y" to "2", "z" to "3"))
        LogContextHolder.setContext(ctx)
        assertEquals(ctx.values, LogContextHolder.current().values)
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextSequentialCallsLastOneWins() {
        LogContextHolder.setContext(LogContext(mapOf("k" to "first")))
        LogContextHolder.setContext(LogContext(mapOf("k" to "second")))
        LogContextHolder.setContext(LogContext(mapOf("k" to "third")))
        assertEquals("third", LogContextHolder.current().values["k"])
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextMultipleEmptyCallsReturnEmptyContext() {
        LogContextHolder.setContext(LogContext())
        LogContextHolder.setContext(LogContext())
        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextInsideWithContextIsOverriddenByWithContextRestore() {
        val outer = LogContext(mapOf("a" to "1"))

        LogContextHolder.withContext(outer) {
            // setContext inside withContext changes the thread-local mid-block
            LogContextHolder.setContext(LogContext(mapOf("b" to "hijacked")))
            assertEquals("hijacked", LogContextHolder.current().values["b"])
        }

        // withContext finally restores to the snapshot captured before the block,
        // so the setContext side-effect does not escape the withContext scope
        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @OptIn(InternalLoggerApi::class)
    @Test
    fun setContextWithMultipleKeysThenClearLeavesNothingBehind() {
        LogContextHolder.setContext(LogContext(mapOf("p" to "1", "q" to "2", "r" to "3")))
        LogContextHolder.setContext(LogContext())

        val current = LogContextHolder.current()
        assertNull(current.values["p"])
        assertNull(current.values["q"])
        assertNull(current.values["r"])
        assertTrue(current.values.isEmpty())
    }
}