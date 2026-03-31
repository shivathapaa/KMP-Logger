package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogLevelTest {

    @Test
    fun levelsAreOrderedCorrectly() {
        assertTrue(LogLevel.VERBOSE < LogLevel.DEBUG)
        assertTrue(LogLevel.DEBUG < LogLevel.INFO)
        assertTrue(LogLevel.INFO < LogLevel.WARN)
        assertTrue(LogLevel.WARN < LogLevel.ERROR)
        assertTrue(LogLevel.ERROR < LogLevel.FATAL)
        assertTrue(LogLevel.FATAL < LogLevel.OFF)
    }

    @Test
    fun greaterThanOrEqualComparison() {
        assertTrue(LogLevel.INFO >= LogLevel.INFO)
        assertTrue(LogLevel.WARN >= LogLevel.INFO)
        assertFalse(LogLevel.DEBUG >= LogLevel.INFO)
    }

    @Test
    fun levelHasEmoji() {
        LogLevel.entries.forEach { level ->
            assertTrue(level.emoji.isNotEmpty(), "Expected emoji for $level")
        }
    }
}