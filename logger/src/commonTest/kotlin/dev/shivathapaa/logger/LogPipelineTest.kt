package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.LogPipeline
import dev.shivathapaa.logger.core.LogPolicy
import dev.shivathapaa.logger.sink.TestSink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogPipelineTest {

    private val sink = TestSink()

    private fun pipeline(minLevel: LogLevel = LogLevel.VERBOSE) =
        LogPipeline(LogPolicy(minLevel, emptyMap()), listOf(sink))

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
    fun processEmitsEventToSink() {
        pipeline().process(event(LogLevel.INFO))
        assertEquals(1, sink.events.size)
    }

    @Test
    fun processFiltersEventBelowMinLevel() {
        pipeline(LogLevel.WARN).process(event(LogLevel.DEBUG))
        assertTrue(sink.events.isEmpty())
    }

    @Test
    fun processAllowsEventAtExactMinLevel() {
        pipeline(LogLevel.WARN).process(event(LogLevel.WARN))
        assertEquals(1, sink.events.size)
    }

    @Test
    fun fatalThrowsRuntimeException() {
        assertFailsWith<RuntimeException> {
            pipeline().process(event(LogLevel.FATAL, "Unrecoverable"))
        }
    }

    @Test
    fun fatalEmitsToSinkBeforeThrowing() {
        assertFailsWith<RuntimeException> {
            pipeline().process(event(LogLevel.FATAL))
        }
        assertEquals(1, sink.events.size)
    }

    @Test
    fun fatalFlushesAndThrowsWithMessage() {
        val ex = assertFailsWith<RuntimeException> {
            pipeline().process(event(LogLevel.FATAL, "boom"))
        }
        assertTrue(ex.message!!.contains("boom"))
    }

    @Test
    fun fatalWithCauseWrapsCause() {
        val cause = IllegalStateException("root cause")
        val fatalEvent = LogEvent(
            level = LogLevel.FATAL,
            loggerName = "Test",
            message = "fatal",
            throwable = cause,
            attributes = emptyMap(),
            context = LogContext(),
            thread = "main",
            timestamp = 0L
        )
        val ex = assertFailsWith<RuntimeException> {
            pipeline().process(fatalEvent)
        }
        assertEquals(cause, ex.cause)
    }

    @Test
    fun wouldProcessReturnsTrueWhenAllowed() {
        assertTrue(pipeline(LogLevel.INFO).wouldProcess(LogLevel.INFO, "Test"))
        assertTrue(pipeline(LogLevel.INFO).wouldProcess(LogLevel.ERROR, "Test"))
    }

    @Test
    fun wouldProcessReturnsFalseWhenFiltered() {
        assertFalse(pipeline(LogLevel.WARN).wouldProcess(LogLevel.DEBUG, "Test"))
    }
}