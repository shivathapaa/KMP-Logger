package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogFormatters
import dev.shivathapaa.logger.sink.DefaultLogSink
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * On the JVM, DefaultLogSink writes to standard output, so its real output can be captured and
 * asserted on here. Android host tests cannot do the same: `android.util.Log` is a stub there.
 */
class DefaultLogSinkJvmTest {

    private fun event(level: LogLevel, message: String, throwable: Throwable? = null) = LogEvent(
        level = level,
        loggerName = "SinkTest",
        message = message,
        throwable = throwable,
        attributes = emptyMap(),
        context = LogContext(),
        thread = "main",
        timestamp = 0L
    )

    private fun captureStandardOutput(block: () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer, true, "UTF-8"))
        try {
            block()
        } finally {
            System.setOut(original)
        }
        return buffer.toString("UTF-8")
    }

    @Test
    fun throwableStackTraceIsPrintedOnce() {
        val marker = "unique-failure-marker"
        val output = captureStandardOutput {
            DefaultLogSink(LogFormatters.pretty(showEmoji = false))
                .emit(event(LogLevel.ERROR, "request failed", IllegalStateException(marker)))
        }

        assertEquals(1, output.split(marker).size - 1, output)
    }

    @Test
    fun verboseAndDebugReachStandardOutput() {
        val output = captureStandardOutput {
            val sink = DefaultLogSink()
            sink.emit(event(LogLevel.VERBOSE, "verbose line"))
            sink.emit(event(LogLevel.DEBUG, "debug line"))
        }

        assertContains(output, "verbose line")
        assertContains(output, "debug line")
    }
}
