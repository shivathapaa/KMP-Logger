package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.TestSink
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BoundLoggerTest {

    private lateinit var sink: TestSink

    @BeforeTest
    fun setUp() {
        sink = TestSink()
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.DEBUG)
                .addSink(sink)
                .build()
        )
    }

    @Test
    fun boundContextAppearsOnEveryEvent() {
        val log = LoggerFactory.get("Api").withContext(LogContext(mapOf("service" to "api")))

        log.info { "one" }
        log.debug { "two" }

        assertEquals(2, sink.events.size)
        sink.events.forEach { assertEquals("api", it.context.values["service"]) }
    }

    @Test
    fun pairOverloadBindsContext() {
        LoggerFactory.get("Api").withContext("requestId" to "req-1", "userId" to 42)
            .info { "msg" }

        val ctx = sink.events.single().context.values
        assertEquals("req-1", ctx["requestId"])
        assertEquals(42, ctx["userId"])
    }

    @Test
    fun chainedBindingsMergeAndLaterValuesWin() {
        LoggerFactory.get("Api")
            .withContext(LogContext(mapOf("service" to "api", "stage" to "first")))
            .withContext(LogContext(mapOf("stage" to "second", "requestId" to "req-1")))
            .info { "msg" }

        val ctx = sink.events.single().context.values
        assertEquals("api", ctx["service"])
        assertEquals("second", ctx["stage"])
        assertEquals("req-1", ctx["requestId"])
    }

    @Test
    fun bindingDoesNotMutateOriginalLogger() {
        val base = LoggerFactory.get("Api")
        base.withContext(LogContext(mapOf("service" to "api")))

        base.info { "msg" }

        assertNull(sink.events.single().context.values["service"])
    }

    @Test
    fun boundContextMergesWithAmbientContext() {
        val log = LoggerFactory.get("Api").withContext(LogContext(mapOf("service" to "api")))

        LogContextHolder.withContext(LogContext(mapOf("requestId" to "req-1"))) {
            log.info { "msg" }
        }

        val ctx = sink.events.single().context.values
        assertEquals("api", ctx["service"])
        assertEquals("req-1", ctx["requestId"])
    }

    @Test
    fun boundContextWinsOverAmbientOnCollision() {
        // Ambient context is best-effort off JVM/Android and may even belong to another
        // coroutine, so it must never override a value bound deliberately to this logger.
        val log = LoggerFactory.get("Api").withContext(LogContext(mapOf("owner" to "bound")))

        LogContextHolder.withContext(LogContext(mapOf("owner" to "ambient"))) {
            log.info { "msg" }
        }

        assertEquals("bound", sink.events.single().context.values["owner"])
    }

    @Test
    fun bindingEmptyContextReturnsSameLogger() {
        val base = LoggerFactory.get("Api")
        assertSame(base, base.withContext(LogContext()))
    }

    @Test
    fun boundLoggerStillRespectsLevelFiltering() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.WARN)
                .addSink(sink)
                .build()
        )

        val log = LoggerFactory.get("Api").withContext(LogContext(mapOf("service" to "api")))
        log.debug { "suppressed" }
        log.warn { "kept" }

        assertEquals(1, sink.events.size)
        assertEquals("kept", sink.events.single().message)
    }

    @Test
    fun boundLoggerDoesNotEvaluateSuppressedMessages() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.WARN)
                .addSink(sink)
                .build()
        )

        var evaluated = false
        LoggerFactory.get("Api")
            .withContext(LogContext(mapOf("service" to "api")))
            .debug { evaluated = true; "suppressed" }

        assertTrue(!evaluated, "message lambda ran for a suppressed level")
    }

    @Test
    fun boundContextDoesNotLeakToOtherLoggers() {
        val bound = LoggerFactory.get("Api").withContext(LogContext(mapOf("service" to "api")))
        val other = LoggerFactory.get("Other")

        bound.info { "bound" }
        other.info { "other" }

        val otherEvent = sink.events.single { it.message == "other" }
        assertNull(otherEvent.context.values["service"])
    }
}
