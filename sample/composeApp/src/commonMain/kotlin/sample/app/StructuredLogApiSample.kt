package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import dev.shivathapaa.logger.sink.ConsoleSink
import dev.shivathapaa.logger.sink.DefaultLogSink
import dev.shivathapaa.logger.sink.TestSink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StructuredLogApiSample(
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        initLogger()
    }

    Column(
        modifier = modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Structured Log API Demo", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Check console for log outputs",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        Text("Basic Features", style = MaterialTheme.typography.titleMedium)
        DemoButton("1. All Log Levels", ::allLogLevels)
        DemoButton("2. Lazy Evaluation", ::lazyEvaluation)
        DemoButton("3. Log with Attributes", ::logWithAttributes)
        DemoButton("4. Log with Exceptions", ::logWithExceptions)

        Spacer(Modifier.height(8.dp))

        Text("Advanced Features", style = MaterialTheme.typography.titleMedium)
        DemoButton("5. Log Context", ::logContext)
        DemoButton("6. Nested Context", ::nestedContext)
        DemoButton("7. Async Logging", ::asyncLogging)
        DemoButton("8. Structured Logging", ::structuredLogging)

        Spacer(Modifier.height(8.dp))

        Text("Configuration & Testing", style = MaterialTheme.typography.titleMedium)
        DemoButton("9. Logger Overrides", ::loggerOverrides)
        DemoButton("10. Multiple Sinks", ::multipleSinks)
        DemoButton("11. Test Sink Verification", ::testSinkVerification)
        DemoButton("12. Min Level Filtering", ::minLevelFiltering)

        Spacer(Modifier.height(8.dp))

        Text("Edge Cases & Special Scenarios", style = MaterialTheme.typography.titleMedium)
        DemoButton("13. Empty Messages", ::emptyMessages)
        DemoButton("14. Null Attributes", ::nullAttributes)
        DemoButton("15. Complex Objects", ::complexObjects)
        DemoButton("16. OFF Level", ::offLevel)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { fatalDemo() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("FATAL LOG (Will Crash)")
        }
    }
}


@Composable
fun DemoButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Text(text)
    }
}

fun initLogger() {
    println("*\n${"=".repeat(70)}")
    println("*INITIALIZING LOGGER")
    println("*=".repeat(70))

    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(DefaultLogSink())
        .build()

    LoggerFactory.install(config)
    println("** Logger initialized with DEBUG level and Console sink")
    println("** Override: OverrideDemo -> ERROR")
    println("** Override: FilteredDemo -> WARN")
}

fun allLogLevels() {
    printHeader("1. ALL LOG LEVELS")
    val logger = LoggerFactory.get("LevelsDemo")

    logger.verbose { "VERBOSE: Most detailed logging (priority: 0)" }
    logger.debug { "DEBUG: Debugging information (priority: 1)" }
    logger.info { "INFO: General information (priority: 2)" }
    logger.warn { "WARN: Warning messages (priority: 3)" }
    logger.error { "ERROR: Error messages (priority: 4)" }
    // FATAL skipped - will crash app
    println("** All log levels demonstrated (except FATAL)")
}

fun lazyEvaluation() {
    printHeader("2. LAZY EVALUATION")
    val logger = LoggerFactory.get("LazyDemo")

    var computationCount = 0

    fun expensiveOperation(): String {
        computationCount++
        println("*  → [COMPUTING EXPENSIVE OPERATION #$computationCount]")

        // Only delaying hack
        var sum = 0L
        repeat(5_000_000) {
            sum += it
        }

        return "Result: $sum"
    }

    println("*Testing lazy evaluation:")

    // Executes only if DEBUG is enabled
    logger.debug { "Debug log: ${expensiveOperation()}" }

    // Skipped if VERBOSE is disabled
    logger.verbose { "Verbose log: ${expensiveOperation()}" }

    println("** Computation executed $computationCount time(s)")
    println("** Lambda only evaluated when log level is enabled")
}

fun logWithAttributes() {
    printHeader("3. LOG WITH ATTRIBUTES")
    val logger = LoggerFactory.get("AttributesDemo")

    // Simple attributes
    logger.info(
        attrs = {
            attr("userId", 12345)
            attr("action", "login")
        }
    ) { "User logged in" }

    // Multiple attributes of different types
    logger.debug(
        attrs = {
            attr("requestId", "req-abc-123")
            attr("duration", 1500)
            attr("success", true)
            attr("statusCode", 200)
        }
    ) { "API request completed" }

    // Attributes only (minimal message)
    logger.info(
        attrs = {
            attr("event", "user_signup")
            attr("email", "user@example.com")
            attr("plan", "premium")
        }
    ) { "Event logged" }

    println("** Attributes are key-value pairs attached to log events")
}

fun logWithExceptions() {
    printHeader("4. LOG WITH EXCEPTIONS")
    val logger = LoggerFactory.get("ExceptionDemo")

    // Simple exception
    try {
        throw IllegalArgumentException("Invalid input parameter")
    } catch (e: Exception) {
        logger.error(throwable = e) { "Caught exception" }
    }

    // Exception with attributes
    try {
        val result = 10 / 0
    } catch (e: ArithmeticException) {
        logger.error(
            throwable = e,
            attrs = {
                attr("operation", "division")
                attr("numerator", 10)
                attr("denominator", 0)
            }
        ) { "Division by zero" }
    }

    // Warning with exception (non-critical)
    try {
        throw IllegalStateException("Unexpected state recovered")
    } catch (e: Exception) {
        logger.warn(throwable = e) { "Recovered from error" }
    }

    println("** Exceptions include full stack trace")
}

fun logContext() {
    printHeader("5. LOG CONTEXT")
    val logger = LoggerFactory.get("ContextDemo")

    // Create context with common fields
    val requestContext = LogContext(
        values = mapOf(
            "requestId" to "req-xyz-789",
            "userId" to 456,
            "sessionId" to "sess-abc-123"
        )
    )

    println("*Logging without context:")
    logger.info { "Regular log message" }

    println("*\nLogging with context:")
    LogContextHolder.withContext(requestContext) {
        logger.info { "Processing request" }
        logger.debug { "Validating input" }
        logger.info { "Request completed" }
    }

    println("*\nContext is automatically removed after block:")
    logger.info { "Back to no context" }

    println("** Context adds metadata to all logs within scope")
}

fun nestedContext() {
    printHeader("6. NESTED CONTEXT")
    val logger = LoggerFactory.get("NestedDemo")

    val outerContext = LogContext(mapOf("traceId" to "trace-123"))
    val innerContext = LogContext(mapOf("spanId" to "span-456"))

    LogContextHolder.withContext(outerContext) {
        logger.info { "Outer context (traceId only)" }

        LogContextHolder.withContext(innerContext) {
            logger.info { "Inner context (traceId + spanId merged)" }
        }

        logger.info { "Back to outer context (traceId only)" }
    }

    println("** Nested contexts are merged")
}

fun asyncLogging() {
    printHeader("7. ASYNC LOGGING")

    CoroutineScope(Dispatchers.Default).launch {
        val logger = LoggerFactory.get("AsyncDemo")

        logger.info { "Starting async operation" }

        delay(50)
        logger.debug(attrs = { attr("step", 1) }) { "Step 1 completed" }

        delay(50)
        logger.debug(attrs = { attr("step", 2) }) { "Step 2 completed" }

        logger.info { "Async operation finished" }
    }

    println("** Logs can be called from coroutines")
}

fun structuredLogging() {
    printHeader("8. STRUCTURED LOGGING")
    val logger = LoggerFactory.get("StructuredDemo")

    // HTTP request logging
    logger.info(
        attrs = {
            attr("method", "POST")
            attr("path", "/api/users")
            attr("statusCode", 201)
            attr("duration", 234)
        }
    ) { "HTTP Request" }

    // Database query
    logger.debug(
        attrs = {
            attr("query", "SELECT * FROM users WHERE id = ?")
            attr("executionTime", 45)
            attr("rowsAffected", 1)
        }
    ) { "Database query" }

    // Business event
    logger.info(
        attrs = {
            attr("orderId", "ORD-001")
            attr("userId", 789)
            attr("total", 99.99)
            attr("items", 3)
        }
    ) { "Order created" }

    println("** Structured logging for machine-readable logs")
}

fun loggerOverrides() {
    printHeader("9. LOGGER OVERRIDES")

    // This logger has ERROR override
    val overrideLogger = LoggerFactory.get("OverrideDemo")

    println("*OverrideDemo logger (override: ERROR):")
    overrideLogger.debug { "DEBUG - Should NOT appear" }
    overrideLogger.info { "INFO - Should NOT appear" }
    overrideLogger.warn { "WARN - Should NOT appear" }
    overrideLogger.error { "ERROR - Should appear ✓" }

    println("*\nFilteredDemo logger (override: WARN):")
    val filteredLogger = LoggerFactory.get("FilteredDemo")
    filteredLogger.debug { "DEBUG - Should NOT appear" }
    filteredLogger.info { "INFO - Should NOT appear" }
    filteredLogger.warn { "WARN - Should appear ✓" }
    filteredLogger.error { "ERROR - Should appear ✓" }

    println("*\nRegular logger (minLevel: DEBUG):")
    val regularLogger = LoggerFactory.get("RegularDemo")
    regularLogger.debug { "DEBUG - Should appear ✓" }
    regularLogger.info { "INFO - Should appear ✓" }

    println("** Overrides allow per-logger level control")
}

fun multipleSinks() {
    printHeader("10. MULTIPLE SINKS")

    val testSink = TestSink()
    val consoleSink = ConsoleSink()
    val defaultLogSink = DefaultLogSink()

    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.INFO)
        .addSink(consoleSink)
        .addSink(testSink)
        .addSink(defaultLogSink)
        .build()

    LoggerFactory.install(config)

    val logger = LoggerFactory.get("MultiSinkDemo")
    logger.info { "This goes to console, default, and test sink" }
    logger.error { "This also goes to all three sinks" }

    println("*\nTestSink captured ${testSink.events.size} events")
    println("** Multiple sinks receive the same log events")

    // Restore original config
    initLogger()
}

fun testSinkVerification() {
    printHeader("11. TEST SINK VERIFICATION")

    val testSink = TestSink()
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(testSink)
        .build()

    LoggerFactory.install(config)

    val logger = LoggerFactory.get("TestVerification")

    // Generate test logs
    logger.info { "Test message 1" }
    logger.debug(attrs = { attr("key", "value") }) { "Test message 2" }
    logger.error(throwable = Exception("Test error")) { "Test message 3" }

    // Verify captured events
    println("*Captured ${testSink.events.size} events:")
    testSink.events.forEachIndexed { idx, event ->
        println("*  [$idx] ${event.level} - ${event.message}")
        println("*      Logger: ${event.loggerName}")
        println("*      Attributes: ${event.attributes}")
        println("*      Has throwable: ${event.throwable != null}")
    }

    println("** TestSink is perfect for unit testing")

    // Restore original config
    initLogger()
}

fun minLevelFiltering() {
    printHeader("12. MIN LEVEL FILTERING")

    println("*Testing different minLevel configurations:\n")

    // Test with INFO level
    val infoSink = TestSink()
    val infoConfig = LoggerConfig.Builder()
        .minLevel(LogLevel.INFO)
        .addSink(infoSink)
        .build()

    LoggerFactory.install(infoConfig)
    val infoLogger = LoggerFactory.get("MinLevelTest")

    infoLogger.verbose { "VERBOSE" }
    infoLogger.debug { "DEBUG" }
    infoLogger.info { "INFO" }
    infoLogger.warn { "WARN" }
    infoLogger.error { "ERROR" }

    println("*With minLevel=INFO, captured ${infoSink.events.size} events:")
    infoSink.events.forEach { println("*  - ${it.level}: ${it.message}") }

    // Test with ERROR level
    val errorSink = TestSink()
    val errorConfig = LoggerConfig.Builder()
        .minLevel(LogLevel.ERROR)
        .addSink(errorSink)
        .build()

    LoggerFactory.install(errorConfig)
    val errorLogger = LoggerFactory.get("MinLevelTest")

    errorLogger.verbose { "VERBOSE" }
    errorLogger.debug { "DEBUG" }
    errorLogger.info { "INFO" }
    errorLogger.warn { "WARN" }
    errorLogger.error { "ERROR" }

    println("*\nWith minLevel=ERROR, captured ${errorSink.events.size} events:")
    errorSink.events.forEach { println("*  - ${it.level}: ${it.message}") }

    println("** minLevel filters logs below threshold")

    // Restore original config
    initLogger()
}

fun emptyMessages() {
    printHeader("13. EMPTY MESSAGES")
    val logger = LoggerFactory.get("EmptyDemo")

    logger.info { "" }
    logger.info(attrs = { attr("key", "value") }) { "" }

    println("** Empty messages are allowed")
}

fun nullAttributes() {
    printHeader("14. NULL ATTRIBUTES")
    val logger = LoggerFactory.get("NullDemo")

    logger.info(
        attrs = {
            attr("field1", null)
            attr("field2", "value")
            attr("field3", null)
        }
    ) { "Message with null attributes" }

    println("** Null attribute values are supported")
}

fun complexObjects() {
    printHeader("15. COMPLEX OBJECTS")
    val logger = LoggerFactory.get("ComplexDemo")

    data class User(val id: Int, val name: String, val email: String)
    data class Order(val id: String, val user: User, val total: Double)

    val user = User(123, "John Doe", "john@example.com")
    val order = Order("ORD-001", user, 299.99)

    logger.info(
        attrs = {
            attr("user", user)
            attr("order", order)
            attr("list", listOf(1, 2, 3))
            attr("map", mapOf("a" to 1, "b" to 2))
        }
    ) { "Complex objects as attributes" }

    println("** Any objects can be attributes (toString() is used)")
}

fun offLevel() {
    printHeader("16. OFF LEVEL")

    val testSink = TestSink()
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.OFF)
        .addSink(testSink)
        .build()

    LoggerFactory.install(config)
    val logger = LoggerFactory.get("OffDemo")

    logger.verbose { "This won't be logged" }
    logger.debug { "This won't be logged" }
    logger.info { "This won't be logged" }
    logger.warn { "This won't be logged" }
    logger.error { "This won't be logged" }

    println("*With minLevel=OFF, captured ${testSink.events.size} events")
    println("** OFF level disables all logging")

    // Restore original config
    initLogger()
}

fun fatalDemo() {
    CoroutineScope(Dispatchers.Default).launch {
        printHeader("FATAL LOG - WILL CRASH APP")
        val logger = LoggerFactory.get("FatalDemo")

        println("*About to log FATAL - app will crash in 2 seconds...")
        delay(2000)

        logger.fatal(
            throwable = IllegalStateException("Critical system failure"),
            attrs = { attr("errorCode", "FATAL_001") }
        ) { "Unrecoverable error" }

        println("*This line will never execute")
    }
}

fun printHeader(title: String) {
    println("*\n${"=".repeat(70)}")
    println(title)
    println("*=".repeat(70))
}