package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.DefaultLogger
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import dev.shivathapaa.logger.core.LogPipeline
import dev.shivathapaa.logger.core.LogPolicy
import dev.shivathapaa.logger.sink.TestSink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultLoggerTest {

    private val sink = TestSink()

    private fun logger(minLevel: LogLevel = LogLevel.VERBOSE, tag: String = "Test"): DefaultLogger {
        val pipeline = LogPipeline(LogPolicy(minLevel, emptyMap()), listOf(sink))
        return DefaultLogger(tag, pipeline)
    }

    @Test
    fun logEmitsEventWithCorrectLevel() {
        logger().log(LogLevel.INFO) { "hello" }
        assertEquals(LogLevel.INFO, sink.lastEvent()?.level)
    }

    @Test
    fun logEmitsEventWithCorrectMessage() {
        logger().log(LogLevel.DEBUG) { "debug message" }
        assertEquals("debug message", sink.lastEvent()?.message)
    }

    @Test
    fun logEmitsEventWithCorrectLoggerName() {
        logger(tag = "MyService").log(LogLevel.INFO) { "msg" }
        assertEquals("MyService", sink.lastEvent()?.loggerName)
    }

    @Test
    fun logPopulatesTimestamp() {
        logger().log(LogLevel.INFO) { "msg" }
        assertTrue((sink.lastEvent()?.timestamp ?: 0L) > 0L)
    }

    @Test
    fun logPopulatesThreadName() {
        logger().log(LogLevel.INFO) { "msg" }
        val thread = sink.lastEvent()?.thread
        assertTrue(thread != null && thread.isNotEmpty())
    }

    @Test
    fun logAttachesThrowable() {
        val ex = RuntimeException("oops")
        logger().log(LogLevel.ERROR, throwable = ex) { "msg" }
        assertEquals(ex, sink.lastEvent()?.throwable)
    }

    @Test
    fun logAttachesAttributes() {
        logger().log(LogLevel.INFO, attrs = {
            attr("userId", 42)
            attr("action", "login")
        }) { "msg" }
        val attrs = sink.lastEvent()?.attributes
        assertEquals(42, attrs?.get("userId"))
        assertEquals("login", attrs?.get("action"))
    }

    @Test
    fun logCapturesActiveContext() {
        val ctx = LogContext(mapOf("requestId" to "req-1"))
        LogContextHolder.withContext(ctx) {
            logger().log(LogLevel.INFO) { "msg" }
        }
        assertEquals("req-1", sink.lastEvent()?.context?.values?.get("requestId"))
    }

    @Test
    fun logSkipsEventBelowMinLevel() {
        logger(minLevel = LogLevel.WARN).log(LogLevel.DEBUG) { "should not appear" }
        assertTrue(sink.events.isEmpty())
    }

    @Test
    fun messageLambdaNotEvaluatedWhenFiltered() {
        var evaluated = false
        logger(minLevel = LogLevel.WARN).log(LogLevel.DEBUG) {
            evaluated = true
            "expensive message"
        }
        assertFalse(evaluated, "Message lambda should not be evaluated for filtered level")
    }

    @Test
    fun messageLambdaEvaluatedWhenAllowed() {
        var evaluated = false
        logger(minLevel = LogLevel.DEBUG).log(LogLevel.DEBUG) {
            evaluated = true
            "message"
        }
        assertTrue(evaluated)
    }

    @Test
    fun offLevelNeverEmits() {
        logger().log(LogLevel.OFF) { "should not emit" }
        assertTrue(sink.events.isEmpty())
    }

    @Test
    fun fatalLevelThrows() {
        assertFailsWith<RuntimeException> {
            logger().log(LogLevel.FATAL) { "fatal" }
        }
    }
}