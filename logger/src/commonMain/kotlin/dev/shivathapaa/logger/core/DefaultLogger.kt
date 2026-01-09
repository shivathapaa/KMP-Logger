package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.StructuredLogger

internal class DefaultLogger(
    private val name: String,
    private val pipeline: LogPipeline
) : StructuredLogger {

    override fun log(
        level: LogLevel,
        throwable: Throwable?,
        attrs: AttrBuilder.() -> Unit,
        message: () -> String
    ) {
        if (level == LogLevel.OFF) return

        val ctx = LogContextHolder.current()
        val attrBuilder = AttrBuilder().apply(attrs)

        val event = LogEvent(
            timestamp = null,
            level = level,
            loggerName = name,
            message = message(),
            throwable = throwable,
            attributes = attrBuilder.build(),
            context = ctx,
            thread = currentThreadName()
        )

        pipeline.process(event)
    }

    private fun currentThreadName(): String = "main" // Will work with context later
}