package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.api.LoggerFactory.get
import dev.shivathapaa.logger.api.LoggerFactory.install
import dev.shivathapaa.logger.core.DefaultLogger
import dev.shivathapaa.logger.core.LogPipeline
import dev.shivathapaa.logger.core.LogPolicy
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.DefaultLogSink
import kotlin.concurrent.Volatile

/**
 * The main entry point for configuring the logging pipeline and obtaining [Logger] instances.
 *
 * Call [install] once during application initialization to configure sinks, level filtering,
 * and per-tag overrides. If [install] is never called, a default configuration is used
 * automatically ([LogLevel.VERBOSE], [DefaultLogSink]).
 *
 * Loggers read the installed configuration on every call, so a logger obtained before [install]
 * (for example in a companion object) follows it too. Logger instances are cached by tag, so
 * repeated calls to [get] with the same tag typically return the same object.
 */
object LoggerFactory {
    @Volatile
    private var pipeline: LogPipeline = createPipeline(
        LoggerConfig.Builder()
            .minLevel(LogLevel.VERBOSE)
            .addSink(DefaultLogSink())
            .build()
    )

    @Volatile
    private var cache: Map<String, Logger> = emptyMap()

    private val currentPipeline: () -> LogPipeline = { pipeline }

    /**
     * Installs a new configuration, replacing any previously installed one.
     *
     * Takes effect immediately for every logger, including loggers obtained before this call.
     *
     * @param config The configuration to apply.
     */
    fun install(config: LoggerConfig) {
        pipeline = createPipeline(config)
    }

    /**
     * Returns a [Logger] identified by [tag].
     *
     * Instances are cached; calling [get] multiple times with the same tag returns the same
     * [Logger] object. In concurrent scenarios, two threads may briefly create separate
     * instances for the same tag - both are equivalent and the overhead is a single extra
     * allocation.
     *
     * @param tag The tag (name) for the logger, typically a class or module name.
     * @return A [Logger] bound to [tag].
     */
    fun get(tag: String): Logger {
        cache[tag]?.let { return it }

        val logger = Logger(delegate = DefaultLogger(tag, currentPipeline))
        cache = cache + (tag to logger)
        return logger
    }

    private fun createPipeline(config: LoggerConfig): LogPipeline =
        LogPipeline(
            policy = LogPolicy(
                minLevel = config.minLevel,
                overrides = config.overrides
            ),
            sinks = config.sinks
        )
}