package dev.shivathapaa.logger.coroutines

import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * JVM/Android-only guarantee: `withLogContext` additionally mirrors the active context into
 * the ambient [LogContextHolder], so plain **unbound** loggers observe it without any
 * suspend call.
 *
 * This works here and only here because `LogContextElement` is a real
 * `kotlinx.coroutines.ThreadContextElement` on JVM/Android - the dispatcher reinstalls the
 * context on every thread the coroutine resumes on. `ThreadContextElement` does not exist on
 * Kotlin/Native or JS, so mirroring into thread state there would hand one coroutine another
 * coroutine's context (see `NativeLogContextIsolationTest`). On those targets the context is
 * carried in the coroutine context only, and is read via [currentLogContext] or bound with
 * [withActiveLogContext].
 *
 * These assertions deliberately live in `jvmTest` rather than `commonTest`: they encode a
 * platform capability, not the portable contract. Putting them in `commonTest` is what
 * previously made the leak look like passing behaviour.
 */
class AmbientLogContextJvmTest {

    @Test
    fun ambientHolderSeesContextInsideBlock() = runTest {
        withLogContext(LogContext(mapOf("requestId" to "req-1"))) {
            yield()
            assertEquals("req-1", LogContextHolder.current().values["requestId"])
        }
    }

    @Test
    fun ambientHolderIsRestoredAfterBlock() = runTest {
        withLogContext(LogContext(mapOf("requestId" to "req-1"))) { yield() }

        assertTrue(LogContextHolder.current().values.isEmpty())
    }

    @Test
    fun ambientHolderReflectsNestedMerge() = runTest {
        withLogContext(LogContext(mapOf("traceId" to "t-1"))) {
            withLogContext(LogContext(mapOf("spanId" to "s-1"))) {
                yield()
                val current = LogContextHolder.current().values
                assertEquals("t-1", current["traceId"])
                assertEquals("s-1", current["spanId"])
            }
            yield()
            assertEquals(null, LogContextHolder.current().values["spanId"])
        }
    }

    @Test
    fun ambientHolderMatchesActiveLogContext() = runTest {
        withLogContext(LogContext(mapOf("env" to "prod"))) {
            yield()
            assertEquals(currentLogContext().values, LogContextHolder.current().values)
        }
    }
}
