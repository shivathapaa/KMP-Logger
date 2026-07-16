package dev.shivathapaa.logger

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.core.LogPolicy
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogPolicyTest {

    private fun event(level: LogLevel, tag: String = "Test") = LogEvent(
        level = level,
        loggerName = tag,
        message = "msg",
        throwable = null,
        attributes = emptyMap(),
        context = LogContext(),
        thread = "main",
        timestamp = 0L
    )

    @Test
    fun allowsEventAtExactMinLevel() {
        val policy = LogPolicy(LogLevel.INFO, emptyMap())
        assertTrue(policy.allows(event(LogLevel.INFO)))
    }

    @Test
    fun allowsEventAboveMinLevel() {
        val policy = LogPolicy(LogLevel.INFO, emptyMap())
        assertTrue(policy.allows(event(LogLevel.ERROR)))
    }

    @Test
    fun blocksEventBelowMinLevel() {
        val policy = LogPolicy(LogLevel.INFO, emptyMap())
        assertFalse(policy.allows(event(LogLevel.DEBUG)))
    }

    @Test
    fun offDisablesAllEvents() {
        val policy = LogPolicy(LogLevel.OFF, emptyMap())
        LogLevel.entries.filter { it != LogLevel.OFF }.forEach { level ->
            assertFalse(policy.allows(event(level)), "Expected $level to be blocked when OFF")
        }
    }

    @Test
    fun overrideRaisesLevelForTag() {
        val policy = LogPolicy(LogLevel.VERBOSE, mapOf("NoisyModule" to LogLevel.ERROR))
        assertFalse(policy.allows(event(LogLevel.WARN, tag = "NoisyModule")))
        assertTrue(policy.allows(event(LogLevel.ERROR, tag = "NoisyModule")))
    }

    @Test
    fun overrideLowersLevelForTag() {
        val policy = LogPolicy(LogLevel.WARN, mapOf("DebugModule" to LogLevel.DEBUG))
        assertTrue(policy.allows(event(LogLevel.DEBUG, tag = "DebugModule")))
        assertFalse(policy.allows(event(LogLevel.VERBOSE, tag = "DebugModule")))
    }

    @Test
    fun overrideDoesNotAffectOtherTags() {
        val policy = LogPolicy(LogLevel.INFO, mapOf("SpecialTag" to LogLevel.ERROR))
        assertTrue(policy.allows(event(LogLevel.INFO, tag = "OtherTag")))
        assertFalse(policy.allows(event(LogLevel.INFO, tag = "SpecialTag")))
    }

    @Test
    fun overrideEscapesGlobalOff() {
        // minLevel(OFF) + override(tag, DEBUG) is the "silence everything except X" case:
        // the override must still win, otherwise the global OFF swallows it.
        val policy = LogPolicy(LogLevel.OFF, mapOf("Network" to LogLevel.DEBUG))

        assertTrue(policy.allows(event(LogLevel.DEBUG, tag = "Network")))
        assertTrue(policy.allows(event(LogLevel.ERROR, tag = "Network")))
        assertFalse(policy.allows(event(LogLevel.ERROR, tag = "Other")))
    }

    @Test
    fun overrideToOffSilencesOnlyThatTag() {
        val policy = LogPolicy(LogLevel.VERBOSE, mapOf("Noisy" to LogLevel.OFF))

        assertFalse(policy.allows(event(LogLevel.ERROR, tag = "Noisy")))
        assertTrue(policy.allows(event(LogLevel.VERBOSE, tag = "Other")))
    }

    @Test
    fun levelAndNameOverloadMatchesEventOverload() {
        val policy = LogPolicy(LogLevel.INFO, mapOf("Tagged" to LogLevel.ERROR))

        val event = event(LogLevel.WARN, tag = "Tagged")
        val byEvent = policy.allows(event)
        val byParts = policy.allows(LogLevel.WARN, "Tagged")

        assertTrue(byEvent == byParts)
    }
}