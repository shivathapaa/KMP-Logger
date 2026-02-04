package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.DefaultLogger
import dev.shivathapaa.logger.core.LogPipeline
import dev.shivathapaa.logger.core.LogPolicy
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.DefaultLogSink
import kotlin.concurrent.Volatile

/**
 * The main entry point for obtaining [Logger] instances.
 *
 * It must be configured with a [LoggerConfig] before use, typically during application initialization.
 * If not explicitly installed, it initializes with a default configuration (INFO level, DefaultLogSink).
 */
object LoggerFactory {
    @Volatile
    private var pipeline: LogPipeline? = null

    init {
        installDefault()
    }

    /**
     * Installs the given configuration. This sets up the internal pipeline and sinks.
     *
     * @param config The configuration to use.
     */
    fun install(config: LoggerConfig) {
        pipeline = LogPipeline(
            policy = LogPolicy(
                minLevel = config.minLevel,
                overrides = config.overrides
            ),
            sinks = config.sinks
        )
    }

    /**
     * Returns a [Logger] instance with the specified tag.
     *
     * @param tag The tag (name) for the logger.
     * @return A [Logger] instance.
     */
    fun get(tag: String): Logger {
        val pipe = pipeline ?: run {
            // Auto-initialize with defaults if not installed
            installDefault()
            pipeline ?: error("LoggerFactory.install() must be called before get()")
        }

        val structured = DefaultLogger(tag, pipe)
        return Logger(delegate = structured)
    }

    private fun installDefault() {
        install(
            LoggerConfig.Builder()
                .minLevel(LogLevel.INFO)
                .addSink(DefaultLogSink())
                .build()
        )
    }
}
