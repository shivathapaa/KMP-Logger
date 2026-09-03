# Module logger

Core Kotlin Multiplatform logger with zero third-party runtime dependencies. Two front-door APIs,
`Log` for quick tag-based calls and `Logger` for structured logging, funnel through one pipeline so
configuration applies uniformly. Ships on Android, iOS, macOS, JVM, JS (Node & Browser), Wasm/JS,
Linux, and MinGW.

`LoggerFactory.install(LoggerConfig)` builds the pipeline (policy + sinks); `LoggerFactory.get(tag)`
returns cached loggers. Without `install`, an auto-default applies: `minLevel = VERBOSE` +
`DefaultLogSink`. Suppressed levels cost nothing beyond the level check, since the policy filter runs
before the message lambda.

# Package dev.shivathapaa.logger.api

Public surface. `Log` is the simple tag-based facade (`Log.i`, `Log.withTag`, `Log.withClassTag`,
`T.loggerI` extensions). `Logger` is the structured API with a message lambda, the `AttrBuilder` DSL,
and `withContext(...)` binding. `LoggerFactory` resolves and caches loggers by tag. `LogLevel` is the
severity ladder (`VERBOSE`..`FATAL`, `OFF`).

# Package dev.shivathapaa.logger.core

Pipeline internals and the immutable event model. `DefaultLogger` assembles the read-only `LogEvent`
(timestamp, level, tag, message, throwable, attributes, resolved context, thread name). `LogPipeline`
filters via `LogPolicy` and fans out to every sink; `FATAL` flushes then throws. `LoggerConfig`
configures `minLevel` and per-tag overrides. `LogContext` and `LogContextHolder` carry contextual
key-value data merged into every event.

# Package dev.shivathapaa.logger.formatters

`LogEventFormatter` (a `fun interface`) and the `LogFormatters` factory: `default`, `pretty`,
`compact`, `json`. `json` stays valid JSON even with `showEmoji`. Formatters treat `LogEvent` as a
read-only snapshot and must not mutate it.

# Package dev.shivathapaa.logger.sink

`LogSink` is the output SPI (`emit` + optional `flush`). Bundled sinks: `DefaultLogSink` (routes to
the platform-native logger), `ConsoleSink` (`println`), `RemoteLogSink` (delegates to a sender), and
`TestSink` (capture for tests). Sinks compose: wrap another sink to redact, sample, or route.
