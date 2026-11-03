package dev.shivathapaa.logger

import dev.shivathapaa.logger.core.currentThreadName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CurrentThreadNameAppleTest {

    @Test
    fun mainThreadIsReportedAsMain() {
        assertEquals("main", currentThreadName())
    }

    @Test
    fun workerThreadIsNotReportedAsMain() = runBlocking {
        val name = withContext(Dispatchers.Default) { currentThreadName() }
        assertNotEquals("main", name)
    }
}
