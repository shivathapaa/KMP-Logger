# KMP Logger

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.shivathapaa/logger"><img src="https://img.shields.io/maven-central/v/io.github.shivathapaa/logger?label=Maven%20Central&color=blue" alt="Maven Central"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1+-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"/></a>
  <a href="https://github.com/shivathapaa/KMP-Logger/actions/workflows/ci.yml"><img src="https://github.com/shivathapaa/KMP-Logger/actions/workflows/ci.yml/badge.svg" alt="CI"/></a>
</p>

A lightweight, structured logging library for Kotlin Multiplatform. Supports Android, iOS, macOS,
watchOS, tvOS, JVM, JS (Node.js & Browser), Wasm/JS, Linux, and MinGW.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Simple Log API](#simple-log-api)
- [Structured Logging Quick Start](#structured-logging-quick-start)
- [Core Concepts](#core-concepts)
- [Configuration](#configuration)
- [Log Context](#log-context)
- [Coroutine Support](#coroutine-support)
- [Log Formatters](#log-formatters)
- [Available Sinks](#available-sinks)
- [Simple vs Structured Logging](#simple-vs-structured-logging)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [Advanced Topics](#advanced-topics)
- [Platform-Specific Setup](#platform-specific-setup)
- [Best Practices](#best-practices)
- [Performance Considerations](#performance-considerations)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Two APIs** - Simple `Log.*` for quick debugging; structured `Logger` for production use
- **Log Levels** - `VERBOSE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`, `OFF`
- **Structured Logging** - Attach typed key-value attributes to any log event
- **Context Propagation** - Thread-local context on JVM/Android, `NSThread` dictionary on Apple,
  scoped save/restore everywhere else
- **Coroutine-Aware Context** - Optional `logger-coroutines` module with `withLogContext` that
  propagates `LogContext` safely across suspension points and thread hops on all platforms
- **Lazy Evaluation** - Message lambda is never evaluated when the event would be filtered out
- **Real Timestamps** - Epoch milliseconds captured on all platforms
- **Thread Names** - Captured natively on Android, JVM, and Apple; `"main"` elsewhere
- **Platform-Native Output** - Logcat on Android, `NSLog` on Apple, `console` on JS/Wasm,
  `stdout` on JVM/Linux/MinGW
- **Configurable Pipeline** - Per-tag level overrides, multiple sinks, custom formatters
- **Built-in Formatters** - Default (human-readable), JSON (log aggregators), Compact, and Pretty
- **Testable** - Built-in `TestSink` for asserting log output in unit tests
- **Extensible** - Implement `LogSink` to send logs anywhere (remote, file, analytics)

## Installation

### Core library

```kotlin
// build.gradle.kts (commonMain)
commonMain.dependencies {
    implementation("io.github.shivathapaa:logger:1.4.0")
}
```

### Coroutine support (optional)

Add `logger-coroutines` if you use coroutines and need `LogContext` to survive suspension points
and thread switches (e.g. `Dispatchers.IO` or `Dispatchers.Default` on JVM/Android):

```kotlin
// build.gradle.kts (commonMain)
commonMain.dependencies {
    implementation("io.github.shivathapaa:logger:1.4.0")
    implementation("io.github.shivathapaa:logger-coroutines:1.4.0")
}
```

For Android-only apps, platform-specific artifacts are also available:

```kotlin
// build.gradle.kts (androidMain)
androidMain.dependencies {
    implementation("io.github.shivathapaa:logger-android:1.4.0")
}
```

For the full list of platform-specific artifacts, see
[Maven Central](https://central.sonatype.com/search?q=logger&namespace=io.github.shivathapaa).

## Simple Log API

For quick logging without any setup, use the `Log` API. No configuration required - it uses
`VERBOSE` as the default minimum level and writes to the platform-native output.

### Basic Usage

```kotlin
Log.v("Verbose message")
Log.d("Debug message")
Log.i("App started")
Log.w("Warning message")
Log.e("Error occurred")
Log.fatal("Critical failure") // always throws after logging

// With exceptions
Log.e("Operation failed", throwable = exception)
Log.w("Recovered from error", throwable = exception)

// With a custom tag
Log.i("User logged in", tag = "Auth")
Log.d("Request completed", tag = "Network")
```

### Set Default Tag

```kotlin
// Set once during app initialization
Log.setDefaultTag("MyApp")

// All subsequent calls use this tag
Log.i("This uses 'MyApp' tag")
```

### Class-Based Logger

```kotlin
class UserViewModel {
    private val log = Log.withClassTag<UserViewModel>()

    fun login() {
        log.d { "Login attempt started" }

        try {
            // login logic
            log.i { "Login successful" }
        } catch (e: Exception) {
            log.e(throwable = e) { "Login failed" }
        }
    }
}
```

### Module-Based Logger

```kotlin
object NetworkModule {
    private val log = Log.withTag("Network")

    fun fetchData() {
        log.d { "Starting API request" }
        log.i { "Request completed successfully" }
    }
}
```

### Extension Functions

```kotlin
class MyViewModel {
    fun doWork() {
        loggerD { "Starting work" }   // uses "MyViewModel" as tag
        loggerI { "Work in progress" }

        try {
            riskyOperation()
        } catch (e: Exception) {
            loggerE(e) { "Work failed" }
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
        .addSink(DefaultLogSink())
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

Levels are ordered from least to most severe. Setting `minLevel` passes that level and everything
above it:

| Level   | Emoji | Usage                                           |
|---------|-------|-------------------------------------------------|
| VERBOSE | 💜    | Most detailed, development only                 |
| DEBUG   | 💚    | Debugging information                           |
| INFO    | 💙    | General informational messages                  |
| WARN    | 💛    | Potential issues, non-critical                  |
| ERROR   | ❤️    | Errors and failures that need investigation     |
| FATAL   | 💔    | Unrecoverable errors - flushes sinks and throws |
| OFF     | ❌     | Disables all logging                            |

### Lazy Evaluation

Log messages use lambda syntax - the message is only computed if the log level is enabled:

```kotlin
// Bad: always computes the expensive operation
logger.debug("Result: ${expensiveComputation()}")

// Good: only computes if DEBUG is enabled
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
    .minLevel(LogLevel.INFO)
    .addSink(DefaultLogSink())
    .build()

LoggerFactory.install(config)
```

### Per-Logger Level Overrides

```kotlin
val config = LoggerConfig.Builder()
    .minLevel(LogLevel.INFO)                    // default for all loggers
    .override("NetworkModule", LogLevel.DEBUG)  // verbose network logs
    .override("ThirdPartySDK", LogLevel.ERROR)  // silence noisy SDK
    .addSink(DefaultLogSink())
    .build()

LoggerFactory.install(config)

val networkLogger = LoggerFactory.get("NetworkModule") // uses DEBUG
val sdkLogger = LoggerFactory.get("ThirdPartySDK")     // uses ERROR
val appLogger = LoggerFactory.get("MyApp")             // uses INFO (default)
```

### Multiple Sinks

```kotlin
val config = LoggerConfig.Builder()
    .minLevel(LogLevel.DEBUG)
    .addSink(DefaultLogSink())                            // platform-native output
    .addSink(FileSink("app.log"))                         // file output (custom)
    .addSink(RemoteLogSink { payload -> send(payload) })  // remote logging
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

Contexts are automatically merged. Inner keys override outer keys on collision:

```kotlin
val traceContext = LogContext(mapOf("traceId" to "trace-123"))
val spanContext = LogContext(mapOf("spanId" to "span-456"))

LogContextHolder.withContext(traceContext) {
    logger.info { "Outer scope" }  // has traceId

    LogContextHolder.withContext(spanContext) {
        logger.info { "Inner scope" }  // has traceId + spanId
    }

    logger.info { "Back to outer" }  // has traceId only
}
```

### withContext Returns a Value

`withContext` propagates the block's return value, so you can use it inline:

```kotlin
val result = LogContextHolder.withContext(context) {
    processRequest() // return value is propagated
}
```

## Coroutine Support

KMP Logger ships with an optional `logger-coroutines` module that provides safe `LogContext`
propagation across suspension points on all platforms.

### The Problem

`LogContextHolder.withContext` is synchronous. On JVM/Android, context is stored in a
`ThreadLocal`. When a coroutine suspends and resumes on a different thread (e.g. with
`Dispatchers.IO`), the `ThreadLocal` on the new thread is empty - the context is lost.

### Solution: `withLogContext`

The `logger-coroutines` module provides `withLogContext`, which solves this correctly on every
platform:

```kotlin
import dev.shivathapaa.logger.coroutines.withLogContext

suspend fun handleRequest(requestId: String) {
    val ctx = LogContext(mapOf("requestId" to requestId))

    withLogContext(ctx) {
        logger.info { "Starting request" }

        withContext(Dispatchers.IO) {       // thread hop - context still present
            delay(100)                      // suspension - context still present
            logger.debug { "Fetching data" }
        }

        logger.info { "Request complete" }
    }
}
```

### How It Works

| Platform                    | Mechanism                                                                                                                                                                 |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| JVM / Android               | `LogContextElement` implements `ThreadContextElement`. The coroutines dispatcher calls `updateThreadContext`/`restoreThreadContext` on every thread switch automatically. |
| iOS / macOS / Apple         | Single-threaded. Context is set directly on `LogContextHolder` for the duration of the block and restored in `finally`.                                                   |
| JS / WasmJS / Linux / MinGW | Single-threaded. Same approach as Apple.                                                                                                                                  |

### Nesting and Merging

`withLogContext` calls nest and merge just like `withContext`. Inner values override outer values
for the same key:

```kotlin
withLogContext(LogContext(mapOf("traceId" to "trace-123"))) {
    logger.info { "Outer" }  // traceId=trace-123

    withLogContext(LogContext(mapOf("spanId" to "span-456"))) {
        logger.info { "Inner" }  // traceId=trace-123, spanId=span-456
    }

    logger.info { "Back to outer" }  // traceId=trace-123
}
```

### Attaching Context to a CoroutineScope

To attach a fixed context to an entire scope, add `LogContextElement` directly to the scope's
coroutine context:

```kotlin
import dev.shivathapaa.logger.coroutines.LogContextElement

val scope = CoroutineScope(
    Dispatchers.IO + LogContextElement(LogContext(mapOf("service" to "payment-api")))
)

scope.launch {
    logger.info { "All coroutines in this scope carry service=payment-api" }
}
```

### Accessing the Active Element

Inside a `withLogContext` block you can read the current `LogContextElement` from the coroutine
context:

```kotlin
withLogContext(LogContext(mapOf("requestId" to "req-1"))) {
    val element = currentCoroutineContext()[LogContextElement]
    println(element?.context)  // LogContext(values={requestId=req-1})
}
```

### `withSuspendingContext` (core, limited)

The core `logger` module also exposes `LogContextHolder.withSuspendingContext`. It accepts a
suspending block but does **not** solve the thread-migration problem on JVM/Android - context is
stored in a `ThreadLocal` and will be lost if the coroutine resumes on a different thread.

Use `withSuspendingContext` only when:

- The coroutine is pinned to a single thread (e.g. `Dispatchers.Main`), **or**
- No thread-migration occurs in the block.

For all other cases, use `withLogContext` from `logger-coroutines`.

```kotlin
// Safe on single-threaded dispatchers (Main, iOS, JS, etc.)
LogContextHolder.withSuspendingContext(ctx) {
    delay(100)
    logger.info { "Context is present" }
}
```

### Which API to Use

| Scenario                                              | API                                          |
|-------------------------------------------------------|----------------------------------------------|
| Non-coroutine code                                    | `LogContextHolder.withContext`               |
| Coroutine, single-threaded dispatcher only (Main, JS) | `LogContextHolder.withSuspendingContext`     |
| Coroutine, any dispatcher / thread hops (IO, Default) | `withLogContext` from `logger-coroutines`    |
| Fixed context on a whole scope                        | `LogContextElement` from `logger-coroutines` |

## Log Formatters

Formatters convert a `LogEvent` into a string. Pass one to a sink. All built-in formatters
are obtained via `LogFormatters`:

### Default

Concise single-line: level, tag, message, stack trace if present.

```kotlin
ConsoleSink(LogFormatters.default(showEmoji = true))
// 💙 [INFO] PaymentService: Payment accepted
```

### Compact

Single-line with inline attributes.

```kotlin
ConsoleSink(LogFormatters.compact(showEmoji = false))
```

### Pretty

Multi-line human-readable output with timestamps and thread names.

```kotlin
ConsoleSink(
    LogFormatters.pretty(
        showEmoji = true,
        includeTimestamp = true,
        includeThread = true,
        prettyPrint = true
    )
)
```

### JSON

Single-line JSON, safe for log aggregation platforms (Elasticsearch, Datadog, Splunk, Loki).

```kotlin
RemoteLogSink(
    logFormatter = LogFormatters.json(showEmoji = false)
) { payload -> myApi.send(payload) }
```

When `showEmoji = true`, the emoji is added as a `"levelEmoji"` field **inside** the JSON object -
not prepended before it - so the output is always valid JSON:

```json
{
  "levelEmoji": "💙",
  "level": "INFO",
  "logger": "PaymentService",
  "timestamp": 1711785600000,
  "message": "Payment accepted",
  "thread": "main"
}
```

### Custom Formatter

```kotlin
val myFormatter = LogEventFormatter { event ->
    "[${event.level}] ${event.loggerName}: ${event.message}"
}
ConsoleSink(myFormatter)
```

## Available Sinks

### DefaultLogSink

Platform-native logging (recommended for most use cases):

```kotlin
.addSink(DefaultLogSink())
```

- **Android** - `android.util.Log` (Logcat)
- **Apple** - `NSLog`
- **JVM / Linux / MinGW** - `stdout`
- **JS / Wasm** - `console`

### ConsoleSink

Console output with a configurable formatter:

```kotlin
.addSink(ConsoleSink())
```

### RemoteLogSink

Formats the event and forwards it to any destination via a `send` lambda:

```kotlin
.addSink(
    RemoteLogSink(
        logFormatter = LogFormatters.json(showEmoji = false)
    ) { payload ->
        myHttpClient.post("/logs", payload)
    }
)
```

### TestSink

Captures events for assertion in unit tests:

```kotlin
val testSink = TestSink()

LoggerFactory.install(
    LoggerConfig.Builder()
        .minLevel(LogLevel.DEBUG)
        .addSink(testSink)
        .build()
)

logger.info { "Test message" }

assertEquals(1, testSink.events.size)
assertEquals(LogLevel.INFO, testSink.events[0].level)
assertEquals("Test message", testSink.events[0].message)
```

## Simple vs Structured Logging

Both APIs share the same pipeline. `LoggerFactory.install()` configuration - sinks, level
filtering, and per-tag overrides - applies to `Log.*` calls as well as `Logger` calls.

| Feature                        | Simple `Log` API                     | Structured `Logger` API  |
|--------------------------------|--------------------------------------|--------------------------|
| Setup required                 | No (auto-initialized)                | No (auto-initialized)    |
| Respects `LoggerFactory` sinks | ✅ Yes                                | ✅ Yes                    |
| Respects level overrides       | ✅ Yes                                | ✅ Yes                    |
| Testable via `TestSink`        | ✅ Yes                                | ✅ Yes                    |
| Lazy message evaluation        | ✅ Yes (via `withTag`/`withClassTag`) | ✅ Yes                    |
| Direct string message          | ✅ Yes (`Log.i("msg")`)               | ❌ No (lambda required)   |
| Structured attributes          | ❌ No                                 | ✅ Yes                    |
| Set log context                | ❌ No                                 | ✅ Via `LogContextHolder` |
| Carries active context         | ✅ Yes (thread-local)                 | ✅ Yes (thread-local)     |

**Use Simple `Log` API when:**

- Quick debugging during development
- Prototyping or simple scripts
- No structured data needed

**Use Structured `Logger` API when:**

- Production applications
- Lazy evaluation matters (hot paths, expensive message construction)
- Need structured key-value attributes
- Want to scope context to a block
- Unit testing specific log output

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
) { "HTTP request" }
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

### Coroutine Request Pipeline

```kotlin
suspend fun processOrder(orderId: String, userId: Int) {
    val ctx = LogContext(mapOf("orderId" to orderId, "userId" to userId))

    withLogContext(ctx) {
        logger.info { "Processing order" }

        val payment = withContext(Dispatchers.IO) {
            logger.debug { "Charging payment" }  // context present on IO thread
            chargePayment(orderId)
        }

        logger.info(attrs = { attr("paymentId", payment.id) }) { "Order complete" }
    }
}
```

## Testing

### Unit Testing with TestSink

```kotlin
@Test
fun logErrorWhenOperationFails() {
    val testSink = TestSink()
    LoggerFactory.install(
        LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(testSink)
            .build()
    )
    val logger = LoggerFactory.get("MyClass")

    logger.error(attrs = { attr("operation", "save") }) { "Failed to save data" }

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
fun propagatesContextToNestedLogs() {
    val testSink = TestSink()
    LoggerFactory.install(
        LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(testSink)
            .build()
    )
    val logger = LoggerFactory.get("ContextTest")

    LogContextHolder.withContext(LogContext(mapOf("requestId" to "req-123"))) {
        logger.info { "Log 1" }
        logger.info { "Log 2" }
    }

    testSink.events.forEach { event ->
        assertEquals("req-123", event.context.values["requestId"])
    }
}
```

### Testing Coroutine Context Propagation

```kotlin
@Test
fun coroutineContextSurvivesSuspension() = runTest {
        val testSink = TestSink()
        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.DEBUG)
                .addSink(testSink)
                .build()
        )
        val logger = LoggerFactory.get("CoroutineTest")

        withLogContext(LogContext(mapOf("requestId" to "req-123"))) {
            delay(10)
            logger.info { "After delay" }
        }

        assertEquals("req-123", testSink.events[0].context.values["requestId"])
    }
```

## Advanced Topics

### Custom Sinks

```kotlin
class FileSink(private val filename: String) : LogSink {
    private val file = File(filename)

    override fun emit(event: LogEvent) {
        val line = "[${event.level}] ${event.loggerName}: ${event.message}\n"
        file.appendText(line)
    }

    override fun flush() {
        // ensure all data is written
    }
}
```

### Firebase Logging Sink

```kotlin
class FirebaseLogSink(
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
            else -> crashlytics.log(message)
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
// Provide a no-op implementation for non-Android targets
```

### Facebook Logging Sink

```kotlin
class FacebookLogSink(
    private val minLevel: LogLevel = LogLevel.INFO,
    private val appEventsLogger: AppEventsLogger
) : LogSink {

    override fun emit(event: LogEvent) {
        if (event.level < minLevel) return

        val eventName = when (event.level) {
            LogLevel.INFO -> "app_log_info"
            LogLevel.WARN -> "app_log_warn"
            LogLevel.ERROR -> "app_log_error"
            LogLevel.FATAL -> "app_log_fatal"
            else -> "app_log"
        }

        val params = Bundle().apply {
            putString("logger", event.loggerName)
            putString("level", event.level.name)
            putString("thread", event.thread)
            putString("message", event.message.take(100))

            event.attributes.forEach { (k, v) ->
                putString("attr_${k.safeKey()}", v?.toString()?.take(100))
            }
            event.context.values.forEach { (k, v) ->
                putString("ctx_${k.safeKey()}", v?.toString()?.take(100))
            }
            event.throwable?.let {
                putString("exception", it::class.simpleName)
                putString("exception_message", it.message?.take(100))
            }
        }

        appEventsLogger.logEvent(eventName, params)
    }

    private fun String.safeKey(): String =
        lowercase().replace("[^a-z0-9_]".toRegex(), "_").take(40)
}
// Provide a no-op implementation for non-Android targets
```

### Filtering Sensitive Data

```kotlin
class SanitizingSink(private val delegate: LogSink) : LogSink {
    private val sensitiveKeys = setOf("password", "apiKey", "token")

    override fun emit(event: LogEvent) {
        val sanitized = event.attributes.mapValues { (key, value) ->
            if (key in sensitiveKeys) "***REDACTED***" else value
        }
        delegate.emit(event.copy(attributes = sanitized))
    }
}
```

## Platform-Specific Setup

### Android

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        LoggerFactory.install(
            LoggerConfig.Builder()
                .minLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO)
                .addSink(DefaultLogSink())
                .build()
        )
    }
}
```

### iOS / Apple

```kotlin
fun initializeApp() {
    LoggerFactory.install(
        LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(DefaultLogSink())
            .build()
    )
}
```

### JVM / Desktop

```kotlin
fun main() {
    LoggerFactory.install(
        LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(DefaultLogSink())
            .build()
    )

    // your application code
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
logger.error { "User clicked button" }       // not an error
logger.debug { "Critical system failure" }   // use ERROR or FATAL
```

### 2. Always Use Lazy Evaluation

```kotlin
// Good - lambda only evaluated if level is enabled
logger.debug { "User: ${user.toDetailedString()}" }

// Bad - always evaluates regardless of level
logger.debug("User: ${user.toDetailedString()}")
```

### 3. Use Structured Attributes

```kotlin
// Good - machine-readable, searchable
logger.info(
    attrs = {
        attr("userId", userId)
        attr("duration", duration)
    }
) { "Request completed" }

// Bad - hard to parse programmatically
logger.info { "Request completed for user $userId in ${duration}ms" }
```

### 4. Use Context for Common Fields

```kotlin
// Good - set once for the whole scope
LogContextHolder.withContext(LogContext(mapOf("requestId" to requestId))) {
    logger.info { "Starting request" }
    processRequest()
    logger.info { "Request completed" }
}

// Bad - repeating the same field everywhere
logger.info(attrs = { attr("requestId", requestId) }) { "Starting request" }
logger.info(attrs = { attr("requestId", requestId) }) { "Request completed" }
```

### 5. Use `withLogContext` in Coroutines

```kotlin
// Good - safe across thread hops and suspension
withLogContext(LogContext(mapOf("requestId" to requestId))) {
    withContext(Dispatchers.IO) { fetchData() }
}

// Risky on JVM/Android - context lost after thread switch
LogContextHolder.withContext(LogContext(mapOf("requestId" to requestId))) {
    withContext(Dispatchers.IO) { fetchData() } // context may be missing here
}
```

### 6. Never Log Sensitive Information

```kotlin
// Good
logger.info(attrs = { attr("userId", userId) }) { "User authenticated" }

// Bad
logger.info { "User logged in with password: $password" }
```

## Performance Considerations

1. **Lazy evaluation** - Always use lambda syntax to avoid unnecessary string construction
2. **Level filtering** - Set an appropriate `minLevel` to reduce overhead in production
3. **Async sinks** - For high-throughput scenarios, wrap your sink in an async dispatcher
4. **Attribute count** - Attributes are powerful but avoid attaching dozens to every event

## Troubleshooting

### Logs Not Appearing

1. If you never called `LoggerFactory.install()`, the default minimum level is `VERBOSE` - all logs
   should appear by default
2. If you did call `LoggerFactory.install()`, verify `minLevel` is not filtering your logs
3. Check per-logger overrides with `.override()` - they take precedence over `minLevel`
4. Ensure at least one sink is configured

### Override Not Working

```kotlin
// The logger name must EXACTLY match the override key
val config = LoggerConfig.Builder()
    .override("MyLogger", LogLevel.ERROR)  // ← exact string
    .build()

val logger = LoggerFactory.get("MyLogger")  // ← must match
```

### Context Not Propagating

You must call `LogContextHolder.withContext()` - simply creating a `LogContext` object does nothing:

```kotlin
// Correct
LogContextHolder.withContext(context) {
    logger.info { "Has context" }
}

// Wrong - context object exists but is never applied
val context = LogContext(mapOf("key" to "value"))
logger.info { "No context" }
```

### Context Lost After Suspension (JVM/Android)

If your context disappears after `delay()` or a thread switch on JVM/Android, you are using the
synchronous `LogContextHolder.withContext` inside a coroutine. Switch to `withLogContext` from the
`logger-coroutines` module:

```kotlin
// Before (context lost after thread switch on JVM/Android)
LogContextHolder.withContext(ctx) {
    withContext(Dispatchers.IO) { ... }
}

// After (context always present)
withLogContext(ctx) {
    withContext(Dispatchers.IO) { ... }
}
```

## Contributing

- Found a bug or have a feature
  idea? [Open an issue](https://github.com/shivathapaa/KMP-Logger/issues/new/choose)
- Want to contribute code?
  Review [CONTRIBUTING.md](https://github.com/shivathapaa/KMP-Logger/blob/main/CONTRIBUTING.md)
  before opening a pull request
- Help others discover the library by sharing it or giving the repo a ⭐

## License

Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Sample Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/e9013bac-2e26-43bf-960c-cdd4eff2f1e7" alt="Android Logcat output showing KMP Logger entries" width="40%">&nbsp;
  <img src="https://github.com/user-attachments/assets/c37203c3-2360-4575-bce4-aa19945e6ea0" alt="iOS NSLog output showing KMP Logger entries" width="40%">&nbsp;
</p>

---

Thanks for the ⭐ - it means a lot!