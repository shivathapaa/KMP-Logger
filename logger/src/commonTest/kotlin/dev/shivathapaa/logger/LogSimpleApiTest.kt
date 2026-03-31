package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.Log
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.TestSink
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LogSimpleApiTest {

    private val sink = TestSink()

    @BeforeTest
    fun setup() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.VERBOSE)
                .addSink(sink)
                .build()
        )
        Log.setDefaultTag("App")
        sink.clear()
    }

    @Test
    fun verboseRoutesToPipeline() {
        Log.v("verbose message")
        assertEquals(1, sink.events.size)
        assertEquals(LogLevel.VERBOSE, sink.lastEvent()?.level)
    }

    @Test
    fun debugRoutesToPipeline() {
        Log.d("debug message")
        assertEquals(LogLevel.DEBUG, sink.lastEvent()?.level)
    }

    @Test
    fun infoRoutesToPipeline() {
        Log.i("info message")
        assertEquals(LogLevel.INFO, sink.lastEvent()?.level)
    }

    @Test
    fun warnRoutesToPipeline() {
        Log.w("warn message")
        assertEquals(LogLevel.WARN, sink.lastEvent()?.level)
    }

    @Test
    fun errorRoutesToPipeline() {
        Log.e("error message")
        assertEquals(LogLevel.ERROR, sink.lastEvent()?.level)
    }

    @Test
    fun customTagIsUsed() {
        Log.i("message", tag = "CustomTag")
        assertEquals("CustomTag", sink.lastEvent()?.loggerName)
    }

    @Test
    fun defaultTagIsUsed() {
        Log.setDefaultTag("MyApp")
        Log.i("message")
        assertEquals("MyApp", sink.lastEvent()?.loggerName)
    }

    @Test
    fun throwableIsAttached() {
        val ex = RuntimeException("oops")
        Log.e("error", throwable = ex)
        assertEquals(ex, sink.lastEvent()?.throwable)
    }

    @Test
    fun minLevelFromFactoryIsRespected() {
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.ERROR)
                .addSink(sink)
                .build()
        )
        sink.clear()

        Log.d("should be filtered")
        Log.i("should be filtered")
        Log.w("should be filtered")
        assertTrue(sink.events.isEmpty())

        Log.e("should pass")
        assertEquals(1, sink.events.size)
    }

    @Test
    fun fatalAlwaysThrows() {
        assertFailsWith<RuntimeException> {
            Log.fatal("unrecoverable")
        }
    }

    @Test
    fun fatalWithThrowableAttachesCause() {
        val cause = IllegalStateException("root")
        val ex = assertFailsWith<RuntimeException> {
            Log.fatal("unrecoverable", throwable = cause)
        }
        assertEquals(cause, ex.cause)
    }

    @Test
    fun withTagCreatesWrapperWithCorrectTag() {
        val log = Log.withTag("NetworkModule")
        log.i("request started")
        assertEquals("NetworkModule", sink.lastEvent()?.loggerName)
    }

    @Test
    fun withClassTagUsesSimpleName() {
        val log = Log.withClassTag<LogSimpleApiTest>()
        log.i("from wrapper")
        assertEquals("LogSimpleApiTest", sink.lastEvent()?.loggerName)
    }

    @Test
    fun logWrapperAllLevelsRoute() {
        val log = Log.withTag("WrapperTag")
        log.v("verbose"); assertEquals(LogLevel.VERBOSE, sink.lastEvent()?.level)
        log.d("debug");   assertEquals(LogLevel.DEBUG,   sink.lastEvent()?.level)
        log.i("info");    assertEquals(LogLevel.INFO,    sink.lastEvent()?.level)
        log.w("warn");    assertEquals(LogLevel.WARN,    sink.lastEvent()?.level)
        log.e("error");   assertEquals(LogLevel.ERROR,   sink.lastEvent()?.level)
    }
}