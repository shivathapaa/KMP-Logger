package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.TestSink
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LoggerFactoryTest {

    private val sink = TestSink()

    @BeforeTest
    fun setup() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.VERBOSE)
                .addSink(sink)
                .build()
        )
        sink.clear()
    }

    @Test
    fun getReturnsLogger() {
        assertNotNull(LoggerFactory.get("MyService"))
    }

    @Test
    fun getReturnsCachedInstanceForSameTag() {
        val a = LoggerFactory.get("CachedTag")
        val b = LoggerFactory.get("CachedTag")
        assertSame(a, b)
    }

    @Test
    fun getReturnsDifferentInstancesForDifferentTags() {
        val a = LoggerFactory.get("TagA")
        val b = LoggerFactory.get("TagB")
        assertTrue(a !== b)
    }

    @Test
    fun installInvalidatesCache() {
        val before = LoggerFactory.get("CacheTest")

        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.INFO)
                .addSink(sink)
                .build()
        )

        val after = LoggerFactory.get("CacheTest")
        assertTrue(before !== after)
    }

    @Test
    fun installedSinkReceivesEvents() {
        LoggerFactory.get("SinkTest").info { "hello from factory" }
        assertEquals(1, sink.events.size)
        assertEquals("hello from factory", sink.lastEvent()?.message)
    }

    @Test
    fun installedMinLevelFiltersEvents() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.WARN)
                .addSink(sink)
                .build()
        )
        sink.clear()

        LoggerFactory.get("FilterTest").debug { "should be filtered" }
        assertTrue(sink.events.isEmpty())

        LoggerFactory.get("FilterTest").warn { "should pass" }
        assertEquals(1, sink.events.size)
    }

    @Test
    fun perTagOverrideApplied() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.INFO)
                .override("VerboseTag", LogLevel.VERBOSE)
                .addSink(sink)
                .build()
        )
        sink.clear()

        LoggerFactory.get("VerboseTag").verbose { "verbose for this tag" }
        assertEquals(1, sink.events.size)

        LoggerFactory.get("OtherTag").verbose { "verbose for other tag" }
        assertEquals(1, sink.events.size) // still 1, other tag was filtered
    }
}