package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.DefaultLogSink
import kotlin.test.Test

/**
 * Native DefaultLogSink channels (NSLog on Apple, standard output on Linux and MinGW) must treat
 * the message as data. Passing it as a printf-style format string would read or write arbitrary
 * memory on directives such as `%n`, so this test passes by not crashing.
 */
class DefaultLogSinkNativeTest {

    @Test
    fun printfDirectivesInMessagesAreNotInterpreted() {
        val sink = DefaultLogSink()
        val hostile = "100% done %s %n %@ %d %x"

        LogLevel.entries.filter { it != LogLevel.OFF }.forEach { level ->
            sink.emit(
                LogEvent(
                    level = level,
                    loggerName = "Printf %s",
                    message = hostile,
                    throwable = IllegalStateException(hostile),
                    attributes = emptyMap(),
                    context = LogContext(),
                    thread = "main",
                    timestamp = 0L
                )
            )
        }
    }
}
