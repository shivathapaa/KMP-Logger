package dev.shivathapaa.logger

import dev.shivathapaa.logger.core.LogContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogContextTest {

    @Test
    fun emptyContextHasNoValues() {
        assertTrue(LogContext().values.isEmpty())
    }

    @Test
    fun mergeCominesBothMaps() {
        val a = LogContext(mapOf("requestId" to "req-1"))
        val b = LogContext(mapOf("userId" to "u-5"))

        val merged = a.merge(b)

        assertEquals("req-1", merged.values["requestId"])
        assertEquals("u-5", merged.values["userId"])
    }

    @Test
    fun mergeInnerOverridesOuterOnKeyCollision() {
        val outer = LogContext(mapOf("env" to "prod", "version" to "1"))
        val inner = LogContext(mapOf("env" to "staging"))

        val merged = outer.merge(inner)

        assertEquals("staging", merged.values["env"])
        assertEquals("1", merged.values["version"])
    }

    @Test
    fun mergeWithEmptyContextReturnsSameValues() {
        val ctx = LogContext(mapOf("key" to "value"))
        val merged = ctx.merge(LogContext())

        assertEquals("value", merged.values["key"])
        assertEquals(1, merged.values.size)
    }
}