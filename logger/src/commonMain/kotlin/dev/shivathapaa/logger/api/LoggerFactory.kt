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
 * Logger instances are cached by tag, so repeated calls to [get] with the same tag typically
 * return the same object. The cache is invalidated whenever [install] is called.
 */
object LoggerFactory {
    @Volatile
    private var pipeline: LogPipeline? = null

    @Volatile
    private var cache: Map<String, Logger> = emptyMap()

    init {
        installDefault()
    }

    /**
     * Installs a new configuration, replacing any previously installed one.
     *
     * All cached logger instances are invalidated so subsequent calls to [get] pick up
     * the new pipeline.
     *
     * @param config The configuration to apply.
     */
    fun install(config: LoggerConfig) {
        pipeline = LogPipeline(
            policy = LogPolicy(
                minLevel = config.minLevel,
                overrides = config.overrides
            ),
            sinks = config.sinks
        )
        cache = emptyMap()
    }

    /**
     * Returns a [Logger] identified by [tag].
     *
     * Instances are cached; calling [get] multiple times with the same tag returns the same
     * [Logger] object until [install] is called again. In concurrent scenarios, two threads
     * may briefly create separate instances for the same tag - both are equivalent and the
     * overhead is a single extra allocation.
     *
     * @param tag The tag (name) for the logger, typically a class or module name.
     * @return A [Logger] bound to [tag].
     */
    fun get(tag: String): Logger {
        cache[tag]?.let { return it }

        val pipe = pipeline ?: run {
            installDefault()
            pipeline!!
        }

        val logger = Logger(delegate = DefaultLogger(tag, pipe))
        cache = cache + (tag to logger)
        return logger
    }

    private fun installDefault() {
        install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.VERBOSE)
                .addSink(DefaultLogSink())
                .build()
        )
    }
}