package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.LogSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The counterpart to `NativeLogContextIsolationTest` in `logger-coroutines`, which is
 * `@Ignore`d because ambient context cannot be propagated correctly on Kotlin/Native.
 *
 * A **bound** context is carried by the logger object itself, so there is no thread-local
 * to lose and no resumption hook required. This test runs the exact scenario that leaks
 * with ambient context - 8 concurrent coroutines on the multi-threaded `Dispatchers.Default`,
 * each suspending before it logs - and asserts every event carries its own context.
 *
 * Results are written into a pre-sized array indexed by owner id rather than collected in a
 * list: concurrent appends from several native threads would be a data race and could mask
 * the very leak this test exists to detect.
 */
class BoundLoggerNativeIsolationTest {

    @Test
    fun boundContextNeverLeaksAcrossCoroutinesOnMultiThreadedDispatcher() = runBlocking {
        val workers = 8
        val observed = arrayOfNulls<String>(workers)

        val sink = object : LogSink {
            override fun emit(event: LogEvent) {
                val owner = (event.context.values["owner"] as? String)?.toIntOrNull() ?: return
                observed[owner] = event.message
            }
        }

        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.DEBUG)
                .addSink(sink)
                .build()
        )

        (0 until workers).map { i ->
            async(Dispatchers.Default) {
                val log = LoggerFactory.get("Worker")
                    .withContext(LogContext(mapOf("owner" to "$i")))

                delay(2) // suspend, very likely resuming on a different pool thread
                yield()
                log.info { "from-$i" }
            }
        }.awaitAll()

        for (i in 0 until workers) {
            assertEquals(
                "from-$i",
                observed[i],
                "coroutine $i's bound context did not survive - bound context is leaking or lost"
            )
        }
    }
}
