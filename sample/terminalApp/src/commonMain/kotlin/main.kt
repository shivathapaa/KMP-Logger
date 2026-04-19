import dev.shivathapaa.logger.api.Log
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.coroutines.LogContextElement
import dev.shivathapaa.logger.coroutines.withLogContext
import dev.shivathapaa.logger.sink.DefaultLogSink
import dev.shivathapaa.logger.sink.TestSink
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking {
    setupLogger()

    simpleApiSamples()
    structuredApiSamples()
    coroutineSamples()
}

// Setup
fun setupLogger() {
    printHeader("LOGGER SETUP")

    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(DefaultLogSink())
        .build()

    LoggerFactory.install(config)
    println("* Logger ready: minLevel=DEBUG, sink=DefaultLogSink")
}

// Simple API
fun simpleApiSamples() {
    printHeader("SIMPLE LOG API")

    Log.setDefaultTag("TerminalApp")
    Log.v("VERBOSE: trace detail")
    Log.d("DEBUG: development info")
    Log.i("INFO: app started")
    Log.w("WARN: approaching limit")
    Log.e("ERROR: something went wrong")

    Log.i("Custom tag", tag = "Network")
    Log.d("Another tag", tag = "Cache")

    val log = Log.withTag("Auth")
    log.i("User logged in")
    log.d("Token refreshed")

    println("* Simple Log API - done")
}

// Structured API
fun structuredApiSamples() {
    printHeader("STRUCTURED LOG API")

    val logger = LoggerFactory.get("StructuredDemo")

    logger.info(
        attrs = {
            attr("method", "POST")
            attr("path", "/api/orders")
            attr("statusCode", 201)
            attr("durationMs", 84)
        }
    ) { "HTTP request" }

    logger.debug(
        attrs = {
            attr("query", "SELECT * FROM users WHERE id = ?")
            attr("params", listOf(123))
            attr("executionTime", 12)
        }
    ) { "DB query" }

    val ctx = LogContext(mapOf("requestId" to "req-term-001", "userId" to 77))
    LogContextHolder.withContext(ctx) {
        logger.info { "Inside sync context: requestId and userId attached" }
        logger.debug { "All logs in this block carry the context" }
    }
    logger.info { "After withContext: context removed" }

    println("* Structured Log API - done")
}

// Coroutine API
suspend fun coroutineSamples() {
    basicWithLogContextSample()
    contextSurvivesSuspensionSample()
    nestedWithLogContextSample()
    withSuspendingContextSample()
    logContextElementOnScopeSample()
    concurrentIsolationSample()
    requestPipelineSample()
    testSinkSample()
}

suspend fun basicWithLogContextSample() {
    printHeader("COROUTINE 1: BASIC withLogContext")

    val logger = LoggerFactory.get("TermCoroutineBasic")
    val ctx = LogContext(mapOf("requestId" to "req-coroutine-001", "env" to "terminal"))

    logger.info { "Before withLogContext - no context" }

    withLogContext(ctx) {
        logger.info { "Inside withLogContext - requestId and env present" }
        delay(10)
        logger.debug { "After delay - context still present" }
    }

    logger.info { "After withLogContext - context removed" }
    println("* Basic withLogContext - done")
}

suspend fun contextSurvivesSuspensionSample() {
    printHeader("COROUTINE 2: CONTEXT SURVIVES SUSPENSION")

    val logger = LoggerFactory.get("TermSuspensionDemo")
    val ctx = LogContext(mapOf("operationId" to "op-suspend"))

    withLogContext(ctx) {
        logger.info { "Before delay - context present" }
        delay(30)
        logger.info { "After delay(30) - context still present" }
        delay(50)
        logger.info { "After delay(50) - context still present" }
    }

    println("* Context survives suspension - done")
}

suspend fun nestedWithLogContextSample() {
    printHeader("COROUTINE 3: NESTED withLogContext")

    val logger = LoggerFactory.get("TermNestedDemo")

    val outerCtx = LogContext(mapOf("traceId" to "trace-term", "service" to "terminal"))
    val innerCtx = LogContext(mapOf("spanId" to "span-db", "service" to "database"))

    withLogContext(outerCtx) {
        logger.info { "Outer: traceId=trace-term, service=terminal" }

        withLogContext(innerCtx) {
            // service overrides to "database", spanId added
            logger.info { "Inner: traceId=trace-term, spanId=span-db, service=database" }
            delay(10)
            logger.debug { "After delay in inner - merged context still present" }
        }

        logger.info { "Back to outer: traceId=trace-term, service=terminal" }
    }

    println("* Nested withLogContext - done")
}

suspend fun withSuspendingContextSample() {
    printHeader("COROUTINE 4: withSuspendingContext (CORE)")

    val logger = LoggerFactory.get("TermSuspendingCtx")
    val ctx = LogContext(mapOf("source" to "withSuspendingContext"))

    // Safe here: single-threaded native dispatcher, no thread migration
    LogContextHolder.withSuspendingContext(ctx) {
        logger.info { "Inside withSuspendingContext - source present" }
        delay(10)
        logger.info { "After delay - context present (safe on native, limited on JVM)" }
    }

    println("* withSuspendingContext - done")
    println("* Note: use withLogContext for guaranteed propagation on JVM/Android")
}

suspend fun logContextElementOnScopeSample() {
    printHeader("COROUTINE 5: LogContextElement ON SCOPE")

    val logger = LoggerFactory.get("TermScopeCtx")
    val scopeCtx = LogContext(mapOf("service" to "terminal-app", "version" to "1.4.0"))
    val scopeElement = LogContextElement(scopeCtx)

    // Attach context to coroutine context directly
    withContext(scopeElement) {
        logger.info { "Inherited from context element: service=terminal-app, version=1.4.0" }

        withLogContext(LogContext(mapOf("operation" to "read"))) {
            logger.info { "Merged: service=terminal-app, version=1.4.0, operation=read" }
        }
    }

    println("* LogContextElement on scope - done")
}

suspend fun concurrentIsolationSample() {
    printHeader("COROUTINE 6: CONCURRENT ISOLATION")

    val logger = LoggerFactory.get("TermConcurrencyDemo")

    // On native these run cooperatively but contexts are restored correctly
    coroutineScope {
        launch {
            withLogContext(LogContext(mapOf("worker" to "A", "taskId" to 1))) {
                repeat(3) { i ->
                    delay(15)
                    logger.info { "Worker A step $i - taskId=1" }
                }
            }
        }
    }

    // Sequential on native (no true parallelism), but context is correct either way
    withLogContext(LogContext(mapOf("worker" to "B", "taskId" to 2))) {
        repeat(3) { i ->
            delay(10)
            logger.info { "Worker B step $i - taskId=2" }
        }
    }

    println("* Concurrent isolation - done")
    println("* On native: cooperative, no thread migration. On JVM: true parallel with isolation.")
}

suspend fun requestPipelineSample() {
    printHeader("COROUTINE 7: REQUEST PIPELINE")

    val requestId = "req-term-${(1000..9999).random()}"
    val userId = (100..999).random()
    val logger = LoggerFactory.get("TermRequestPipeline")

    withLogContext(LogContext(mapOf("requestId" to requestId, "userId" to userId))) {
        logger.info { "Incoming request" }

        withLogContext(LogContext(mapOf("phase" to "auth"))) {
            logger.debug { "Verifying token" }
            delay(15)
            logger.info { "Token valid" }
        }

        withLogContext(LogContext(mapOf("phase" to "fetch"))) {
            logger.debug { "Querying data store" }
            delay(30)
            logger.debug(attrs = { attr("rows", 3) }) { "Query complete" }
        }

        withLogContext(LogContext(mapOf("phase" to "response"))) {
            logger.debug { "Serializing response" }
            delay(5)
            logger.info(attrs = {
                attr("statusCode", 200); attr(
                "durationMs",
                50
            )
            }) { "Request complete" }
        }
    }

    println("* Request pipeline - done")
}

suspend fun testSinkSample() {
    printHeader("COROUTINE 8: TestSink VERIFICATION")

    val testSink = TestSink()
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(testSink)
        .build()
    LoggerFactory.install(config)

    val logger = LoggerFactory.get("TermTestVerify")
    val ctx = LogContext(mapOf("testRun" to "terminal-coroutine", "env" to "native"))

    withLogContext(ctx) {
        delay(5)
        logger.info { "Log inside coroutine context" }
        delay(5)
        logger.debug(attrs = { attr("step", 1) }) { "Step completed" }
    }

    println("* Captured ${testSink.events.size} events:")
    testSink.events.forEachIndexed { idx, event ->
        val ctxValues = event.context.values
        println("*  [$idx] ${event.level} - ${event.message}")
        println("*      testRun: ${ctxValues["testRun"]}, env: ${ctxValues["env"]}")
        println("*      context intact: ${ctxValues["testRun"] == "terminal-coroutine"}")
    }

    val allCorrect = testSink.events.all { it.context.values["testRun"] == "terminal-coroutine" }
    println("** All events have correct context: $allCorrect")

    // Restore logger
    setupLogger()
}

// Utility
fun printHeader(title: String) {
    println("\n${"=".repeat(70)}")
    println(title)
    println("=".repeat(70))
}