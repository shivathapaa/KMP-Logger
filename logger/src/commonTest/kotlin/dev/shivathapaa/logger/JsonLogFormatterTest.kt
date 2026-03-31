package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.formatters.LogFormatters
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonLogFormatterTest {

    private fun event(
        level: LogLevel = LogLevel.INFO,
        message: String? = "Test message",
        attrs: Map<String, Any?> = emptyMap(),
        context: Map<String, Any?> = emptyMap(),
        throwable: Throwable? = null
    ) = LogEvent(
        level = level,
        loggerName = "TestLogger",
        message = message,
        throwable = throwable,
        attributes = attrs,
        context = LogContext(context),
        thread = "main",
        timestamp = 1711785600000L
    )

    private val formatter = LogFormatters.json(showEmoji = false)
    private val formatterWithEmoji = LogFormatters.json(showEmoji = true)

    @Test
    fun outputStartsAndEndsWithBraces() {
        val result = formatter.format(event())
        assertTrue(result.startsWith("{"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun containsLevelField() {
        val result = formatter.format(event(level = LogLevel.WARN))
        assertTrue(result.contains("\"level\":\"WARN\""))
    }

    @Test
    fun containsLoggerField() {
        val result = formatter.format(event())
        assertTrue(result.contains("\"logger\":\"TestLogger\""))
    }

    @Test
    fun containsTimestampField() {
        val result = formatter.format(event())
        assertTrue(result.contains("\"timestamp\":1711785600000"))
    }

    @Test
    fun containsMessageField() {
        val result = formatter.format(event(message = "Hello world"))
        assertTrue(result.contains("\"message\":\"Hello world\""))
    }

    @Test
    fun noEmojiFieldWhenShowEmojiIsFalse() {
        val result = formatter.format(event())
        assertFalse(result.contains("levelEmoji"))
    }

    @Test
    fun emojiFieldInsideObjectWhenShowEmojiIsTrue() {
        val result = formatterWithEmoji.format(event(level = LogLevel.INFO))
        // emoji field must be inside the JSON object, not before it
        assertTrue(result.startsWith("{"), "Output must start with '{', not with emoji")
        assertTrue(result.contains("\"levelEmoji\""))
        assertTrue(result.contains(LogLevel.INFO.emoji))
    }

    @Test
    fun attributesIncludedWhenPresent() {
        val result = formatter.format(event(attrs = mapOf("userId" to 42, "action" to "login")))
        assertTrue(result.contains("\"attributes\""))
        assertTrue(result.contains("\"userId\":42"))
        assertTrue(result.contains("\"action\":\"login\""))
    }

    @Test
    fun attributesOmittedWhenEmpty() {
        val result = formatter.format(event(attrs = emptyMap()))
        assertFalse(result.contains("\"attributes\""))
    }

    @Test
    fun contextIncludedWhenPresent() {
        val result = formatter.format(event(context = mapOf("requestId" to "req-1")))
        assertTrue(result.contains("\"context\""))
        assertTrue(result.contains("\"requestId\":\"req-1\""))
    }

    @Test
    fun contextOmittedWhenEmpty() {
        val result = formatter.format(event())
        assertFalse(result.contains("\"context\""))
    }

    @Test
    fun errorFieldIncludedWhenThrowablePresent() {
        val result = formatter.format(event(throwable = RuntimeException("boom")))
        assertTrue(result.contains("\"error\""))
        assertTrue(result.contains("boom"))
    }

    @Test
    fun errorFieldOmittedWhenNoThrowable() {
        val result = formatter.format(event())
        assertFalse(result.contains("\"error\""))
    }

    @Test
    fun specialCharactersInMessageAreEscaped() {
        val result = formatter.format(event(message = "say \"hello\""))
        assertTrue(result.contains("\\\"hello\\\""))
    }

    @Test
    fun booleanAttributeNotQuoted() {
        val result = formatter.format(event(attrs = mapOf("flag" to true)))
        assertTrue(result.contains("\"flag\":true"))
    }

    @Test
    fun numberAttributeNotQuoted() {
        val result = formatter.format(event(attrs = mapOf("count" to 99)))
        assertTrue(result.contains("\"count\":99"))
    }

    @Test
    fun nullAttributeRenderedAsNull() {
        val result = formatter.format(event(attrs = mapOf("key" to null)))
        assertTrue(result.contains("\"key\":null"))
    }
}