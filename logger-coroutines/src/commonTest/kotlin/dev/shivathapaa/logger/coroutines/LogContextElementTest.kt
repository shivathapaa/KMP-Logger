package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Top-level helper: captures currentCoroutineContext() without being shadowed
// by coroutineContext from an enclosing runTest lambda.
private suspend fun currentCoroutineContext(): CoroutineContext = currentCoroutineContext()

class LogContextElementTest {

    // Construction
    @Test
    fun contextPropertyReflectsConstructorArg() {
        val ctx = LogContext(mapOf("requestId" to "req-1"))
        val element = LogContextElement(ctx)
        assertEquals(ctx.values, element.context.values)
    }

    @Test
    fun contextPropertyWithEmptyLogContext() {
        val element = LogContextElement(LogContext())
        assertTrue(element.context.values.isEmpty())
    }

    @Test
    fun contextPropertyRetainsMultipleValues() {
        val values = mapOf("traceId" to "t-1", "spanId" to "s-1", "userId" to "u-99")
        val element = LogContextElement(LogContext(values))
        assertEquals(values, element.context.values)
    }

    // Key 

    @Test
    fun keyIsLogContextElementCompanionKey() {
        val element = LogContextElement(LogContext(mapOf("k" to "v")))
        assertEquals(LogContextElement.Key, element.key)
    }

    @Test
    fun companionKeyAndClassRefResolveToSameKey() {
        assertEquals(LogContextElement.Key, LogContextElement)
    }

    // Coroutine-context lookup
    @Test
    fun elementRetrievableFromCoroutineContextByCompanionKey() = runTest {
        val ctx = LogContext(mapOf("userId" to "u-1"))
        withLogContext(ctx) {
            yield()
            val element = currentCoroutineContext()[LogContextElement.Key]
            assertNotNull(element)
            assertEquals("u-1", element.context.values["userId"])
        }
    }

    @Test
    fun elementRetrievableFromCoroutineContextByClassRef() = runTest {
        val ctx = LogContext(mapOf("service" to "api"))
        withLogContext(ctx) {
            yield()
            val element = currentCoroutineContext()[LogContextElement]
            assertNotNull(element)
            assertEquals("api", element.context.values["service"])
        }
    }

    @Test
    fun elementNotPresentInCoroutineContextOutsideWithLogContext() = runTest {
        val element = currentCoroutineContext()[LogContextElement]
        assertNull(element)
    }

    // Element context matches LogContextHolder
    @Test
    fun elementContextMatchesLogContextHolder() = runTest {
        val ctx = LogContext(mapOf("env" to "prod"))
        withLogContext(ctx) {
            yield()
            val element = currentCoroutineContext()[LogContextElement]
            assertNotNull(element)
            assertEquals(LogContextHolder.current().values, element.context.values)
        }
    }

    @Test
    fun nestedElementContextContainsMergedValues() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        withLogContext(outer) {
            withLogContext(inner) {
                yield()
                val element = currentCoroutineContext()[LogContextElement]
                assertNotNull(element)
                assertEquals("t-1", element.context.values["traceId"])
                assertEquals("s-1", element.context.values["spanId"])
            }
        }
    }

    @Test
    fun elementContextInOuterScopeDoesNotContainInnerKeys() = runTest {
        val outer = LogContext(mapOf("traceId" to "t-1"))
        val inner = LogContext(mapOf("spanId" to "s-1"))

        withLogContext(outer) {
            withLogContext(inner) { yield() }
            yield()
            val element = currentCoroutineContext()[LogContextElement]
            assertNotNull(element)
            assertEquals("t-1", element.context.values["traceId"])
            assertNull(element.context.values["spanId"])
        }
    }

    // Two distinct instances
    @Test
    fun twoElementInstancesWithDifferentContextsAreDistinct() {
        val a = LogContextElement(LogContext(mapOf("k" to "a")))
        val b = LogContextElement(LogContext(mapOf("k" to "b")))
        assertEquals("a", a.context.values["k"])
        assertEquals("b", b.context.values["k"])
    }
}