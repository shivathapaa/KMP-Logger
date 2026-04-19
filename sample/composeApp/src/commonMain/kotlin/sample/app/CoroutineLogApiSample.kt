package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LogContext
import dev.shivathapaa.logger.core.LogContextHolder
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.coroutines.LogContextElement
import dev.shivathapaa.logger.coroutines.withLogContext
import dev.shivathapaa.logger.sink.DefaultLogSink
import dev.shivathapaa.logger.sink.TestSink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Demonstrates the logger-coroutines module: safe LogContext propagation
 * across suspension points and thread hops using [withLogContext].
 */
@Composable
fun CoroutineLogApiSample(modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        initCoroutineLogger()
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Coroutine Log API Demo", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Check console for log outputs",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        Text("withLogContext", style = MaterialTheme.typography.titleMedium)
        DemoButton("1. Basic withLogContext", ::basicWithLogContext)
        DemoButton("2. Context Survives Suspension", ::contextSurvivesSuspension)
        DemoButton("3. Context Survives Thread Hop", ::contextSurvivesThreadHop)
        DemoButton("4. Nested withLogContext", ::nestedWithLogContext)

        Spacer(Modifier.height(8.dp))

        Text("Comparison & Scope", style = MaterialTheme.typography.titleMedium)
        DemoButton("5. withSuspendingContext (core)", ::withSuspendingContextDemo)
        DemoButton("6. LogContextElement on Scope", ::logContextElementOnScope)

        Spacer(Modifier.height(8.dp))

        Text("Concurrency & Real-World", style = MaterialTheme.typography.titleMedium)
        DemoButton("7. Concurrent Coroutine Isolation", ::concurrentCoroutineIsolation)
        DemoButton("8. Request Pipeline Simulation", ::requestPipelineSimulation)
        DemoButton("9. TestSink Verification", ::coroutineTestSinkVerification)

        Spacer(Modifier.height(16.dp))
    }
}

fun initCoroutineLogger() {
    printHeader("INITIALIZING LOGGER (Coroutine Demo)")

    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(DefaultLogSink())
        .build()

    LoggerFactory.install(config)
    println("** Logger initialized with DEBUG level and DefaultLogSink")
}

/**
 * Demonstrates basic withLogContext: context is attached for the block duration
 * and automatically removed when the block completes.
 */
fun basicWithLogContext() {
    printHeader("1. BASIC withLogContext")

    CoroutineScope(Dispatchers.Default).launch {
        val logger = LoggerFactory.get("CoroutineBasic")
        val ctx = LogContext(mapOf("requestId" to "req-001", "userId" to 42))

        println("* Before withLogContext - no context:")
        logger.info { "No context here" }

        withLogContext(ctx) {
            println("* Inside withLogContext - context attached:")
            logger.info { "Has requestId and userId in context" }
            logger.debug { "Context is automatically included in every log" }
        }

        println("* After withLogContext - context removed:")
        logger.info { "Context is gone again" }

        println("** withLogContext scopes context to the block")
    }
}

/**
 * Demonstrates that context survives suspension points (delay, yield, etc.).
 */
fun contextSurvivesSuspension() {
    printHeader("2. CONTEXT SURVIVES SUSPENSION")

    CoroutineScope(Dispatchers.Default).launch {
        val logger = LoggerFactory.get("SuspensionDemo")
        val ctx = LogContext(mapOf("operationId" to "op-suspend-test"))

        withLogContext(ctx) {
            logger.info { "Before suspension - operationId in context" }

            delay(50)  // suspension point
            logger.info { "After delay(50) - context still present" }

            delay(100) // another suspension point
            logger.info { "After delay(100) - context still present" }
        }

        println("** Context survives suspension on all platforms")
    }
}

/**
 * Demonstrates that context survives thread hops via withContext.
 * On JVM/Android this exercises the ThreadContextElement mechanism.
 * On single-threaded platforms (JS, iOS, etc.) there is no thread switch
 * but the test still passes correctly.
 */
fun contextSurvivesThreadHop() {
    printHeader("3. CONTEXT SURVIVES THREAD HOP")

    CoroutineScope(Dispatchers.Default).launch {
        val logger = LoggerFactory.get("ThreadHopDemo")
        val ctx = LogContext(mapOf("traceId" to "trace-thread-hop"))

        withLogContext(ctx) {
            logger.info { "Original coroutine context - traceId present" }

            withContext(Dispatchers.Default) {
                // On JVM/Android: different thread, ThreadContextElement restores context
                // On JS/iOS/etc.: same thread, context is still set
                logger.info { "After withContext(Default) - traceId still present" }
                delay(30)
                logger.debug { "After delay inside thread hop - traceId still present" }
            }

            logger.info { "Back to original context - traceId still present" }
        }

        println("** On JVM/Android: ThreadContextElement auto-installs on every thread resume")
        println("** On JS/iOS/native: single-threaded, context set directly on LogContextHolder")
    }
}

/**
 * Demonstrates that nested withLogContext calls merge their contexts.
 * Inner keys override outer keys on collision.
 */
fun nestedWithLogContext() {
    printHeader("4. NESTED withLogContext")

    CoroutineScope(Dispatchers.Default).launch {
        val logger = LoggerFactory.get("NestedCoroutineDemo")

        val outerCtx = LogContext(mapOf("traceId" to "trace-999", "service" to "api"))
        val innerCtx = LogContext(mapOf("spanId" to "span-456", "service" to "db"))

        withLogContext(outerCtx) {
            logger.info { "Outer: traceId=trace-999, service=api" }

            withLogContext(innerCtx) {
                // spanId added, service overrides to "db"
                logger.info { "Inner: traceId=trace-999, spanId=span-456, service=db" }

                delay(20)
                logger.debug { "After delay in inner - still merged context" }
            }

            logger.info { "Back to outer: traceId=trace-999, service=api" }
        }

        logger.info { "Outside all blocks - no context" }
        println("** Nested contexts merge; inner values override outer on key collision")
    }
}

/**
 * Demonstrates withSuspendingContext from the core module.
 * Safe on single-threaded dispatchers; limited on JVM/Android with thread hops.
 */
fun withSuspendingContextDemo() {
    printHeader("5. withSuspendingContext (CORE, LIMITED)")

    CoroutineScope(Dispatchers.Default).launch {
        val logger = LoggerFactory.get("SuspendingContextDemo")
        val ctx = LogContext(mapOf("source" to "withSuspendingContext"))

        // Safe: stays on the same dispatcher thread after a simple delay
        LogContextHolder.withSuspendingContext(ctx) {
            logger.info { "Inside withSuspendingContext - source present" }
            delay(30)
            // NOTE: On JVM/Android with multi-threaded dispatchers, context may be
            // missing here if the coroutine resumed on a different thread.
            // Use withLogContext from logger-coroutines for guaranteed propagation.
            logger.info { "After delay - context present (safe if no thread switch)" }
        }

        println("** withSuspendingContext: use withLogContext for guaranteed propagation on JVM/Android")
    }
}

/**
 * Demonstrates attaching a LogContextElement to an entire CoroutineScope,
 * so every coroutine launched in that scope inherits the context.
 */
fun logContextElementOnScope() {
    printHeader("6. LogContextElement ON SCOPE")

    val serviceCtx = LogContext(mapOf("service" to "payment-api", "version" to "v2"))
    val scope = CoroutineScope(Dispatchers.Default + LogContextElement(serviceCtx))

    scope.launch {
        val logger = LoggerFactory.get("ScopeCtxDemo")

        logger.info { "Coroutine 1: inherits service=payment-api, version=v2 from scope" }

        withLogContext(LogContext(mapOf("orderId" to "ORD-123"))) {
            // Merges scope context with local context
            logger.info { "With extra orderId: service=payment-api, version=v2, orderId=ORD-123" }
        }
    }

    scope.launch {
        delay(10)
        val logger = LoggerFactory.get("ScopeCtxDemo2")
        logger.info { "Coroutine 2: also inherits service=payment-api, version=v2 from scope" }
    }

    println("** LogContextElement on scope: all child coroutines inherit the context")
}

/**
 * Demonstrates that concurrent coroutines with different contexts do not
 * interfere with each other.
 */
fun concurrentCoroutineIsolation() {
    printHeader("7. CONCURRENT COROUTINE ISOLATION")

    val scope = CoroutineScope(Dispatchers.Default)

    scope.launch {
        val logger = LoggerFactory.get("ConcurrencyDemo")

        val job1 = launch {
            withLogContext(LogContext(mapOf("coroutine" to "A", "userId" to 1))) {
                repeat(3) { i ->
                    delay(20)
                    logger.info { "Coroutine A step $i - userId=1" }
                }
            }
        }

        val job2 = launch {
            withLogContext(LogContext(mapOf("coroutine" to "B", "userId" to 2))) {
                repeat(3) { i ->
                    delay(15)
                    logger.info { "Coroutine B step $i - userId=2" }
                }
            }
        }

        job1.join()
        job2.join()

        println("** Concurrent coroutines maintain isolated contexts")
        println("** Each userId log entry matches its coroutine (no cross-contamination)")
    }
}

/**
 * Simulates a real-world request pipeline where context flows through
 * multiple async operations: auth check, data fetch, response building.
 */
fun requestPipelineSimulation() {
    printHeader("8. REQUEST PIPELINE SIMULATION")

    CoroutineScope(Dispatchers.Default).launch {
        val requestId = "req-${(1000..9999).random()}"
        val userId = (100..999).random()

        val baseCtx = LogContext(mapOf("requestId" to requestId, "userId" to userId))

        withLogContext(baseCtx) {
            val logger = LoggerFactory.get("RequestPipeline")
            logger.info { "Incoming request" }

            // Auth phase
            withLogContext(LogContext(mapOf("phase" to "auth"))) {
                logger.debug { "Checking token" }
                delay(20)
                logger.info { "Token valid" }
            }

            // Data fetch phase
            withLogContext(LogContext(mapOf("phase" to "fetch"))) {
                logger.debug { "Querying database" }
                withContext(Dispatchers.Default) {
                    delay(40)
                    logger.debug(
                        attrs = { attr("rows", 5) }
                    ) { "Query complete" }
                }
            }

            // Response phase
            withLogContext(LogContext(mapOf("phase" to "response"))) {
                logger.debug { "Building response" }
                delay(10)
                logger.info(
                    attrs = { attr("statusCode", 200); attr("durationMs", 70) }
                ) { "Request complete" }
            }
        }

        println("** Request ID and user ID flowed through auth, fetch, and response phases")
    }
}

/**
 * Demonstrates using TestSink to assert that context values are present
 * in logs emitted from within withLogContext.
 */
fun coroutineTestSinkVerification() {
    printHeader("9. TEST SINK VERIFICATION")

    CoroutineScope(Dispatchers.Default).launch {
        val testSink = TestSink()
        val config = LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(testSink)
            .build()
        LoggerFactory.install(config)

        val logger = LoggerFactory.get("CoroutineTestVerify")
        val ctx = LogContext(mapOf("testRun" to "coroutine-ctx-test", "env" to "sample"))

        withLogContext(ctx) {
            delay(10)
            logger.info { "Log inside coroutine context" }
            delay(10)
            logger.debug(attrs = { attr("step", 1) }) { "Step completed" }
        }

        // Verify captured events
        println("* Captured ${testSink.events.size} events:")
        testSink.events.forEachIndexed { idx, event ->
            val ctxValues = event.context.values
            println("*  [$idx] ${event.level} - ${event.message}")
            println("*      testRun: ${ctxValues["testRun"]}")
            println("*      env: ${ctxValues["env"]}")
            println("*      context intact: ${ctxValues["testRun"] == "coroutine-ctx-test"}")
        }

        val allHaveContext = testSink.events.all {
            it.context.values["testRun"] == "coroutine-ctx-test"
        }
        println("** All ${testSink.events.size} events have correct context: $allHaveContext")

        // Restore logger
        initCoroutineLogger()
    }
}
