# KMP Logger

A lightweight, structured logging library for Kotlin Multiplatform projects with support for
different platforms i.e.,
Android, iOS, js, wasmJs, jvm, macosX64, macosArm64, linuxX64, and mingwX64.

## Features

- **Structured Logging** - Attach key-value attributes to log events
- **Basic Logging** - Simple log levels and messages
- **Log Levels** - VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
- **Configurable** - Per-logger level overrides and multiple sinks
- **Context Support** - Thread-local and coroutine context propagation
- **Lazy Evaluation** - Log messages only evaluated when needed
- **Platform Native** - Uses Logcat on Android, NSLog on iOS
- **Testable** - Built-in TestSink for unit testing
- **Extensible** - Custom sinks for different outputs

## Installation

```kotlin
// build.gradle.kts (commonMain)
commonMain.dependencies {
    implementation("io.github.shivathapaa:logger:1.2.0")
}
```

For only Android app,

```kotlin
// build.gradle.kts (androidMain)
androidMain.dependencies {
    implementation("io.github.shivathapaa:logger-android:1.2.0")
}
```

For different platforms, check artifacts
at [Maven Central](https://central.sonatype.com/search?q=logger&namespace=io.github.shivathapaa)

Checkout [Advance Sinks](#advanced-topics).

- [Custom Sinks](#custom-sinks)
- [Remote Logging Sink](#remote-logging-sink)
- [Firebase Logging Sink](#firebase-logging-sink)
- [Facebook Logging Sink](#facebook-logging-sink)
- [Filtering Sensitive Data](#filtering-sensitive-data)

## Simple Log API

For quick, non-structured logging without the need for configuration, use the simple `Log` API:

### Basic Usage

```kotlin
// Direct logging with default tag
Log.v("Verbose message")
Log.d("Debug message")
Log.i("App started")
Log.w("Warning message")
Log.e("Error occurred")
Log.fatal("Critical failure")

// With exceptions
Log.e("Operation failed", throwable = exception)
Log.w("Recovered from error", throwable = exception)

// With custom tag
Log.i("User logged in", tag = "Auth")
Log.d("Request completed", tag = "Network")
```

### Set Default Tag

```kotlin
// Set once during app initialization
Log.setDefaultTag("MyApp")

// All subsequent logs use this tag
Log.i("This uses 'MyApp' tag")
```

### Class-Based Logger

Create a logger instance with automatic class name as tag:

```kotlin
class UserViewModel {
    private val log = Log.withClassTag()

    fun login() {
        log.d("Login attempt started")

        try {
            // Login logic
            log.i("Login successful")
        } catch (e: Exception) {
            log.e("Login failed", throwable = e)
        }
    }
}
```

### Module-Based Logger

Create a logger for a specific module or component:

```kotlin
object NetworkModule {
    private val log = Log.withTag("Network")

    fun fetchData() {
        log.d("Starting API request")
        log.i("Request completed successfully")
    }
}
```

### Extension Functions

Use extension functions for the cleanest syntax:

```kotlin
class MyViewModel {
    fun doWork() {
        loggerD("Starting work")  // Uses "MyViewModel" as tag
        loggerI("Work in progress")

        try {
            riskyOperation()
        } catch (e: Exception) {
            loggerE("Work failed", e)  // Automatically uses class name
        }
    }
}
```

Available extensions: `loggerV()`, `loggerD()`, `loggerI()`, `loggerW()`, `loggerE()`,
`loggerFatal()`

## Structured Logging Quick Start

### 1. Initialize the Logger

```kotlin
fun main() {
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(DefaultLogSink())  // Choose predefined sink or create custom
        .build()

    LoggerFactory.install(config)
}
```

### 2. Get a Logger Instance

```kotlin
val logger = LoggerFactory.get("MyApp")
```

### 3. Start Logging

```kotlin
logger.info { "Application started" }
logger.debug { "Debug information" }
logger.error { "Something went wrong" }
```

## Core Concepts

### Log Levels

Logs are filtered based on priority (lowest to highest):

| Level   | Priority | Usage                                       |
|---------|----------|---------------------------------------------|
| VERBOSE | 0        | Most detailed                               |
| DEBUG   | 1        | Debugging information                       |
| INFO    | 2        | General informational messages              |
| WARN    | 3        | Warning messages for potential issues       |
| ERROR   | 4        | Error messages for failures                 |
| FATAL   | 5        | Critical errors (flushes sinks and crashes) |
| OFF     | 6        | Disables all logging                        |

### Lazy Evaluation

Log messages use lambda syntax for lazy evaluation - the message is only computed if the log level
is enabled:

```kotlin
// Bad: Always computes expensive operation
logger.debug("Result: ${expensiveComputation()}")

// Good: Only computes if DEBUG is enabled
logger.debug { "Result: ${expensiveComputation()}" }
```

### Structured Logging with Attributes

Attach key-value metadata to logs for machine-readable output:

```kotlin
logger.info(
    attrs = {
        attr("userId", 12345)
        attr("action", "login")
        attr("duration", 1500)
    }
) { "User logged in" }
```

Output:

```
[INFO] MyApp - User logged in | attrs={userId=12345, action=login, duration=1500}
```

### Exception Logging

Log exceptions with full stack traces:

```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    logger.error(
        throwable = e,
        attrs = {
            attr("operation", "riskyOperation")
            attr("retryCount", 3)
        }
    ) { "Operation failed after retries" }
}
```

## Configuration

### Basic Configuration

```kotlin
val config = LoggerConfig.Builder()
    .minLevel(LogLevel.INFO)          // Set minimum log level
    .addSink(DefaultLogSink())       // Add output sink
    .build()

LoggerFactory.install(config)
```

### Per-Logger Level Overrides

Control log levels for specific loggers:

```kotlin
val config = LoggerConfig.Builder()
    .minLevel(LogLevel.INFO)                    // Default for all loggers
    .override("NetworkModule", LogLevel.DEBUG)  // Debug for network logs
    .override("ThirdPartySDK", LogLevel.ERROR)  // Only errors from SDK
    .addSink(DefaultLogSink())
    .build()

LoggerFactory.install(config)

// This logger will use DEBUG level
val networkLogger = LoggerFactory.get("NetworkModule")

// This logger will use ERROR level
val sdkLogger = LoggerFactory.get("ThirdPartySDK")

// This logger will use INFO (default)
val appLogger = LoggerFactory.get("MyApp")
```

### Multiple Sinks

Send logs to multiple destinations:

```kotlin
val config = LoggerConfig.Builder()
    .minLevel(LogLevel.DEBUG)
    .addSink(DefaultLogSink())      // Console output
    .addSink(FileSink("app.log"))    // File output (custom)
    .addSink(RemoteSink(apiUrl))     // Remote logging (custom)
    .build()
```

## Log Context

### Basic Context

Add common fields to all logs within a scope:

```kotlin
val context = LogContext(
    values = mapOf(
        "requestId" to "req-123",
        "userId" to 456
    )
)

LogContextHolder.withContext(context) {
    logger.info { "Processing request" }
    logger.debug { "Validating input" }
    logger.info { "Request completed" }
}
```

Output:

```
[INFO] MyApp - Processing request | ctx={requestId=req-123, userId=456}
[DEBUG] MyApp - Validating input | ctx={requestId=req-123, userId=456}
[INFO] MyApp - Request completed | ctx={requestId=req-123, userId=456}
```

### Nested Context

Contexts are automatically merged:

```kotlin
val traceContext = LogContext(mapOf("traceId" to "trace-123"))
val spanContext = LogContext(mapOf("spanId" to "span-456"))

LogContextHolder.withContext(traceContext) {
    logger.info { "Outer scope" }  // Has traceId

    LogContextHolder.withContext(spanContext) {
        logger.info { "Inner scope" }  // Has both traceId and spanId
    }

    logger.info { "Back to outer" }  // Has only traceId again
}
```

## Log Formatters

You can format log as per your use case. Common formatters are provided:

```kotlin 
DefaultLogFormatter
CompactLogFormatter
PrettyLogFormatter
JsonLogFormatter
```

## Available Sinks

### DefaultLogSink

Platform-specific native logging (recommended):

```kotlin
.addSink(DefaultLogSink())
```

- **Android**: Uses `android.util.Log` (Logcat)
- **iOS**: Uses `NSLog`

### ConsoleSink

Basic console output with formatting:

```kotlin
.addSink(ConsoleSink())
```

Output format:

```
[INFO] MyApp - Message | attrs={key=value} | ctx={context=data}
```

### RemoteSink

Basic sink for remote logging:

```kotlin
.addSink(RemoteSink())
```

### TestSink

Capture logs for unit testing:

```kotlin
val testSink = TestSink()

val config = LoggerConfig.Builder()
    .minLevel(LogLevel.DEBUG)
    .addSink(testSink)
    .build()

LoggerFactory.install(config)

// Run your code
logger.info { "Test message" }

// Verify
assertEquals(1, testSink.events.size)
assertEquals(LogLevel.INFO, testSink.events[0].level)
assertEquals("Test message", testSink.events[0].message)
```

## Simple vs Structured Logging

| Feature         | Simple Log API  | Structured Logger API         |
|-----------------|-----------------|-------------------------------|
| Setup Required  | ❌ No            | ✅ Yes (LoggerFactory.install) |
| Attributes      | ❌ No            | ✅ Yes                         |
| Context         | ❌ No            | ✅ Yes                         |
| Multiple Sinks  | ❌ No            | ✅ Yes                         |
| Level Overrides | ❌ No            | ✅ Yes                         |
| Lazy Evaluation | ❌ No            | ✅ Yes                         |
| Testing Support | ❌ Limited       | ✅ TestSink                    |
| Use Case        | Quick debugging | Production logging            |

**Use Simple Log API when:**

- Quick debugging during development
- Simple console output
- No need for structured data
- Prototyping

**Use Structured Logger API when:**

- Production applications
- Need structured attributes
- Want context propagation
- Multiple output destinations
- Unit testing logs

## Usage Examples

### HTTP Request Logging

```kotlin
logger.info(
    attrs = {
        attr("method", "POST")
        attr("path", "/api/users")
        attr("statusCode", 201)
        attr("duration", 234)
        attr("ip", "192.168.1.1")
    }
) { "HTTP Request" }
```

### Database Query Logging

```kotlin
logger.debug(
    attrs = {
        attr("query", "SELECT * FROM users WHERE id = ?")
        attr("params", listOf(123))
        attr("executionTime", 45)
        attr("rowsAffected", 1)
    }
) { "Query executed" }
```

### Business Event Logging

```kotlin
logger.info(
    attrs = {
        attr("event", "order_created")
        attr("orderId", "ORD-001")
        attr("userId", 789)
        attr("total", 99.99)
        attr("items", 3)
    }
) { "Order created successfully" }
```

### Async Logging

```kotlin
suspend fun backgroundTask() {
    val logger = LoggerFactory.get("BackgroundTask")

    logger.info { "Task started" }

    delay(1000)

    logger.debug { "Processing..." }

    delay(1000)

    logger.info { "Task completed" }
}
```

## Testing

### Unit Testing with TestSink

```kotlin
@Test
fun should_log_error_when_operation_fails() {
    // Setup
    val testSink = TestSink()
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(testSink)
        .build()

    LoggerFactory.install(config)
    val logger = LoggerFactory.get("MyClass")

    // Execute
    logger.error(
        attrs = { attr("operation", "save") }
    ) { "Failed to save data" }

    // Assert
    assertEquals(1, testSink.events.size)

    val event = testSink.events[0]
    assertEquals(LogLevel.ERROR, event.level)
    assertEquals("Failed to save data", event.message)
    assertEquals("MyClass", event.loggerName)
    assertEquals("save", event.attributes["operation"])
}
```

### Testing Context Propagation

```kotlin
@Test
fun should_propagate_context_to_nested_logs() {
    val testSink = TestSink()
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(testSink)
        .build()

    LoggerFactory.install(config)
    val logger = LoggerFactory.get("ContextTest")

    val context = LogContext(mapOf("requestId" to "req-123"))

    LogContextHolder.withContext(context) {
        logger.info { "Log 1" }
        logger.info { "Log 2" }
    }

    testSink.events.forEach { event ->
        assertEquals("req-123", event.context.values["requestId"])
    }
}
```

## Advanced Topics

### Custom Sinks

Create custom sinks for specialized logging:

```kotlin
class FileSink(private val filename: String) : LogSink {
    private val file = File(filename)

    override fun emit(event: LogEvent) {
        val line = "[${event.level}] ${event.loggerName}: ${event.message}\n"
        file.appendText(line)
    }

    override fun flush() {
        // Ensure all data is written
    }
}
```

### Firebase Logging Sink

```kotlin

actual class FirebaseLogSink(
    private val minLevel: LogLevel = LogLevel.WARN,
    private val formatter: LogEventFormatter = LogFormatters.json(false),
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) : LogSink {

    override fun emit(event: LogEvent) {
        if (event.level < minLevel) return

        attachContext(event)

        val message = formatter.format(event)

        when (event.level) {
            LogLevel.ERROR,
            LogLevel.FATAL -> {
                val throwable = event.throwable ?: LoggedException(message)
                crashlytics.recordException(throwable)
            }

            else -> {
                crashlytics.log(message)
            }
        }
    }

    private fun attachContext(event: LogEvent) {
        event.context.values.forEach { (key, value) ->
            crashlytics.setCustomKey(
                key.take(40),
                value?.toString()?.take(100) ?: "null"
            )
        }
    }

    private class LoggedException(message: String) : RuntimeException(message)
}
// Similar actual for other platforms if available 
```

### Facebook Logging Sink

```kotlin
actual class FacebookLogSink(
    private val minLevel: LogLevel = LogLevel.INFO,
    private val appEventsLogger: AppEventsLogger
) : LogSink {

    override fun emit(event: LogEvent) {
        if (event.level < minLevel) return

        val eventName = mapEventName(event)
        val params = buildParams(event)

        appEventsLogger.logEvent(eventName, params)
    }

    private fun mapEventName(event: LogEvent): String {
        return when (event.level) {
            LogLevel.INFO -> "app_log_info"
            LogLevel.WARN -> "app_log_warn"
            LogLevel.ERROR -> "app_log_error"
            LogLevel.FATAL -> "app_log_fatal"
            else -> "app_log"
        }
    }

    private fun buildParams(event: LogEvent): Bundle {
        return Bundle().apply {
            putString("logger", event.loggerName)
            putString("level", event.level.name)
            putString("thread", event.thread)

            event.message?.let {
                putString("message", it.take(100))
            }

            // Attributes
            event.attributes.forEach { (k, v) ->
                putString(
                    "attr_${k.safeKey()}",
                    v?.toString()?.take(100)
                )
            }

            // Context
            event.context.values.forEach { (k, v) ->
                putString(
                    "ctx_${k.safeKey()}",
                    v?.toString()?.take(100)
                )
            }

            // Error metadata (not stacktrace!)
            event.throwable?.let {
                putString("exception", it::class.simpleName)
                putString("exception_message", it.message?.take(100))
            }
        }
    }

    private fun String.safeKey(): String =
        lowercase()
            .replace("[^a-z0-9_]".toRegex(), "_")
            .take(40)
}
// Similar actual for other platforms if available 
```

### Filtering Sensitive Data

```kotlin
class SanitizingSink(private val delegate: LogSink) : LogSink {
    private val sensitiveKeys = setOf("password", "apiKey", "token")

    override fun emit(event: LogEvent) {
        val sanitizedAttrs = event.attributes.mapValues { (key, value) ->
            if (key in sensitiveKeys) "***REDACTED***" else value
        }

        val sanitizedEvent = event.copy(attributes = sanitizedAttrs)
        delegate.emit(sanitizedEvent)
    }
}
```

## Platform-Specific Implementation

### Android

```kotlin
// In your Application class or main activity
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LoggerConfig.Builder()
            .minLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO)
            .addSink(DefaultLogSink())
            .build()

        LoggerFactory.install(config)
    }
}
```

### iOS

```kotlin
// In your AppDelegate or main entry point
fun initializeApp() {
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(DefaultLogSink())
        .build()

    LoggerFactory.install(config)
}
```

### JVM/Desktop

```kotlin
fun main() {
    val config = LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(DefaultLogSink())
        .build()

    LoggerFactory.install(config)

    // Your application code
}
```

## Best Practices

### 1. Use Appropriate Log Levels

```kotlin
// Good
logger.debug { "Cache size: ${cache.size}" }
logger.info { "User logged in successfully" }
logger.warn { "API rate limit approaching" }
logger.error { "Failed to connect to database" }

// Bad
logger.error { "User clicked button" }  // Not an error!
logger.debug { "Critical system failure" }  // Use ERROR or FATAL
```

### 2. Use Lazy Evaluation

```kotlin
// Good - Lambda only evaluated if level is enabled
logger.debug { "User: ${user.toDetailedString()}" }

// Bad - Always evaluates expensive operation
logger.debug("User: ${user.toDetailedString()}")
```

### 3. Use Structured Attributes

```kotlin
// Good - Machine-readable, searchable
logger.info(
    attrs = {
        attr("userId", userId)
        attr("duration", duration)
    }
) { "Request completed" }

// Bad - Hard to parse
logger.info { "Request completed for user $userId in ${duration}ms" }
```

### 4. Use Context for Common Fields

```kotlin
// Good - Context applied to all logs
LogContextHolder.withContext(LogContext(mapOf("requestId" to requestId))) {
    logger.info { "Starting request" }
    processRequest()
    logger.info { "Request completed" }
}

// Bad - Repeating requestId everywhere
logger.info(attrs = { attr("requestId", requestId) }) { "Starting request" }
logger.info(attrs = { attr("requestId", requestId) }) { "Request completed" }
```

### 5. Don't Log Sensitive Information

```kotlin
// Good
logger.info(
    attrs = { attr("userId", userId) }
) { "User authenticated" }

// Bad - Logging sensitive data
logger.info { "User logged in with password: $password" }
```

## Performance Considerations

1. **Lazy Evaluation**: Always use lambda syntax for log messages to avoid unnecessary computation
2. **Log Level Filtering**: Set appropriate `minLevel` to reduce overhead
3. **Async Logging**: For high-throughput scenarios, consider wrapping your sink in an async wrapper
4. **Attribute Count**: While attributes are powerful, avoid adding dozens to every log

## Troubleshooting

### Logs Not Appearing

1. Check that `LoggerFactory.install()` was called
2. Verify the `minLevel` is not filtering your logs
3. Check per-logger overrides with `.override()`
4. Ensure at least one sink is configured

### Override Not Working

```kotlin
// Make sure the logger name EXACTLY matches the override key
val config = LoggerConfig.Builder()
    .override("MyLogger", LogLevel.ERROR)  // ← Must match exactly
    .build()

val logger = LoggerFactory.get("MyLogger")  // ← Must match exactly
```

### Context Not Propagating

Ensure you're using `LogContextHolder.withContext()` and not just creating a `LogContext` object:

```kotlin
// Correct
LogContextHolder.withContext(context) {
    logger.info { "Has context" }
}

// Wrong - context is not applied
val context = LogContext(...)
logger.info { "No context" }
```

## License

Apache License 2.0 License - See [LICENSE](LICENSE) file for details

## Contributing

You can contribute to this project in several ways:

- Have an idea for an improvement or a new feature? I'm open to suggestions! Feel free to suggest
  changes, request enhancements, or report
  issues [here](https://github.com/shivathapaa/KMP-Logger/issues/new/choose).
- Share the project with your network to help others discover it.
- Want to contribute directly? You're welcome to open a pull request! Be sure to review
  the [CONTRIBUTING.md](https://github.com/shivathapaa/KMP-Logger/blob/main/CONTRIBUTING.md) guide
  before getting started.
- Show your support by giving this repository a Star⭐. It means a lot! 😊

## Sample Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/e9013bac-2e26-43bf-960c-cdd4eff2f1e7" alt="" width="40%">&nbsp;
  <img src="https://github.com/user-attachments/assets/c37203c3-2360-4575-bce4-aa19945e6ea0" alt="" width="40%">&nbsp;
</p>

---

Thank you for star! 😉