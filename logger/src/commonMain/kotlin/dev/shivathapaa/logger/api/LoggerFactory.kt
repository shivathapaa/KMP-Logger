package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.DefaultLogger
import dev.shivathapaa.logger.core.LogPipeline
import dev.shivathapaa.logger.core.LogPolicy
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.DefaultLogSink
import kotlin.concurrent.Volatile

object LoggerFactory {
    @Volatile
    private var pipeline: LogPipeline? = null

    init {
        installDefault()
    }

    fun install(config: LoggerConfig) {
        pipeline = LogPipeline(
            policy = LogPolicy(
                minLevel = config.minLevel,
                overrides = config.overrides
            ),
            sinks = config.sinks
        )
    }

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
