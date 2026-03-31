package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogFormatters
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultLogFormatterTest {

    private fun event(
        level: LogLevel = LogLevel.INFO,
        tag: String = "TestLogger",
        message: String? = "Test message",
        throwable: Throwable? = null
    ) = LogEvent(
        level = level,
        loggerName = tag,
        message = message,
        throwable = throwable,
        attributes = emptyMap(),
        context = LogContext(),
        thread = "main",
        timestamp = 0L
    )

    private val formatter = LogFormatters.default(showEmoji = false)
    private val formatterWithEmoji = LogFormatters.default(showEmoji = true)

    @Test
    fun containsLevelName() {
        val result = formatter.format(event(level = LogLevel.WARN))
        assertTrue(result.contains("WARN"))
    }

    @Test
    fun containsLoggerName() {
        val result = formatter.format(event(tag = "MyService"))
        assertTrue(result.contains("MyService"))
    }

    @Test
    fun containsMessage() {
        val result = formatter.format(event(message = "Hello world"))
        assertTrue(result.contains("Hello world"))
    }

    @Test
    fun emojiIncludedWhenEnabled() {
        val result = formatterWithEmoji.format(event(level = LogLevel.INFO))
        assertTrue(result.contains(LogLevel.INFO.emoji))
    }

    @Test
    fun emojiNotIncludedWhenDisabled() {
        val result = formatter.format(event(level = LogLevel.INFO))
        assertFalse(result.contains(LogLevel.INFO.emoji))
    }

    @Test
    fun stackTraceIncludedWhenThrowablePresent() {
        val ex = RuntimeException("something broke")
        val result = formatter.format(event(throwable = ex))
        assertTrue(result.contains("something broke"))
    }

    @Test
    fun noStackTraceWhenNoThrowable() {
        val result = formatter.format(event())
        assertFalse(result.contains("Exception"))
    }
}