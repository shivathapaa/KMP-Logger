package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.TestSink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestSinkTest {

    private val sink = TestSink()

    private fun event(level: LogLevel, message: String = "msg") = LogEvent(
        level = level,
        loggerName = "Test",
        message = message,
        throwable = null,
        attributes = emptyMap(),
        context = LogContext(),
        thread = "main",
        timestamp = 0L
    )

    @Test
    fun emitStoresEvent() {
        sink.emit(event(LogLevel.INFO))
        assertEquals(1, sink.events.size)
    }

    @Test
    fun clearRemovesAllEvents() {
        sink.emit(event(LogLevel.INFO))
        sink.emit(event(LogLevel.DEBUG))
        sink.clear()
        assertTrue(sink.events.isEmpty())
    }

    @Test
    fun hasLevelReturnsTrueWhenMatched() {
        sink.emit(event(LogLevel.ERROR))
        assertTrue(sink.hasLevel(LogLevel.ERROR))
    }

    @Test
    fun hasLevelReturnsFalseWhenNotMatched() {
        sink.emit(event(LogLevel.INFO))
        assertFalse(sink.hasLevel(LogLevel.ERROR))
    }

    @Test
    fun messagesWithLevelReturnsOnlyMatching() {
        sink.emit(event(LogLevel.INFO, "info message"))
        sink.emit(event(LogLevel.ERROR, "error message"))
        sink.emit(event(LogLevel.INFO, "another info"))

        val infoMessages = sink.messagesWithLevel(LogLevel.INFO)
        assertEquals(2, infoMessages.size)
        assertTrue(infoMessages.contains("info message"))
        assertTrue(infoMessages.contains("another info"))
    }

    @Test
    fun lastEventReturnsNullWhenEmpty() {
        assertNull(sink.lastEvent())
    }

    @Test
    fun lastEventReturnsMostRecent() {
        sink.emit(event(LogLevel.DEBUG, "first"))
        sink.emit(event(LogLevel.INFO, "second"))
        assertEquals("second", sink.lastEvent()?.message)
    }
}