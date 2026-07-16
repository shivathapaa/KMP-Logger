package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.LogSink
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guarantees that must hold on EVERY target - jvm, android, native, js, wasmJs.
 *
 * Uses `runTest`, which exists everywhere (unlike `runBlocking`), so JS and Wasm are covered
 * too. Interleaving is forced with `yield()`: on a single-threaded runtime that is the exact
 * shape that used to hand one coroutine another coroutine's context.
 */
class PortableContextIsolationTest {

    /**
     * The universal guarantee, and the regression test for the ambient leak.
     *
     * The ambient holder must never expose a *foreign* coroutine's context. What it may
     * legitimately hold differs per platform - on JVM/Android a real `ThreadContextElement`
     * keeps it in sync with this coroutine, elsewhere nothing writes it - so both "my own
     * value" and "nothing" are correct. Seeing a sibling's value is never correct.
     *
     * This is deliberately asserted against [LogContextHolder] rather than
     * [currentLogContext]: the latter reads the coroutine context and is isolated by
     * construction, so it would pass even against the buggy implementation.
     */
    @Test
    fun ambientHolderNeverExposesAForeignCoroutinesContext() = runTest {
        val observed = (0 until 8).map { i ->
            async {
                withLogContext(LogContext(mapOf("owner" to "$i"))) {
                    yield()
                    val first = LogContextHolder.current().values["owner"]
                    yield()
                    val second = LogContextHolder.current().values["owner"]
                    listOf(first, second)
                }
            }
        }.awaitAll()

        observed.forEachIndexed { i, values ->
            values.forEach { value ->
                assertTrue(
                    value == null || value == "$i",
                    "coroutine $i observed ambient context owned by $value"
                )
            }
        }
    }

    @Test
    fun interleavedCoroutinesEachSeeOnlyTheirOwnActiveContext() = runTest {
        val observed = (0 until 8).map { i ->
            async {
                withLogContext(LogContext(mapOf("owner" to "$i"))) {
                    yield()
                    val first = currentLogContext().values["owner"]
                    yield()
                    val second = currentLogContext().values["owner"]
                    // Must be stable across suspension as well as correct.
                    listOf(first, second)
                }
            }
        }.awaitAll()

        observed.forEachIndexed { i, values ->
            assertEquals(listOf("$i", "$i"), values, "coroutine $i observed a foreign context")
        }
    }

    @Test
    fun interleavedCoroutinesEmitEventsCarryingOnlyTheirOwnBoundContext() = runTest {
        val events = installCapturingSink()

        (0 until 8).map { i ->
            async {
                withLogContext(LogContext(mapOf("owner" to "$i"))) {
                    val log = LoggerFactory.get("Worker").withActiveLogContext()
                    yield()
                    log.info { "from-$i" }
                }
            }
        }.awaitAll()

        assertEquals(8, events.size)
        events.forEach { event ->
            val owner = event.context.values["owner"]
            assertEquals(
                "from-$owner",
                event.message,
                "event carried context owned by $owner - contexts are crossing coroutines"
            )
        }
    }

    /**
     * A bound logger needs no coroutine machinery at all: the context is a field, so
     * interleaving cannot disturb it and no ambient scope is involved.
     */
    @Test
    fun boundLoggersEmitTheirOwnContextUnderInterleaving() = runTest {
        val events = installCapturingSink()

        (0 until 8).map { i ->
            async {
                val log = LoggerFactory.get("Worker").withContext("owner" to "$i")
                yield()
                log.info { "from-$i" }
                yield()
                log.withContext("step" to "second").info { "second-$i" }
            }
        }.awaitAll()

        assertEquals(16, events.size)
        events.forEach { event ->
            val owner = event.context.values["owner"]
            assertTrue(
                event.message == "from-$owner" || event.message == "second-$owner",
                "event '${event.message}' carried context owned by $owner"
            )
        }
        // The chained binding must not bleed onto the first message.
        assertEquals(8, events.count { it.context.values["step"] == "second" })
    }

    /** Installs a sink that captures events, and returns the list it appends to. */
    private fun installCapturingSink(): MutableList<LogEvent> {
        val events = mutableListOf<LogEvent>()
        val sink = object : LogSink {
            override fun emit(event: LogEvent) {
                events += event
            }
        }
        LoggerFactory.install(
            LoggerConfig.Builder().minLevel(LogLevel.DEBUG).addSink(sink).build()
        )
        return events
    }
}
