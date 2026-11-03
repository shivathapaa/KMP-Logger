package dev.shivathapaa.logger.core

/**
 * Marks API that is internal to the KMP Logger library and reserved exclusively for
 * the `logger-coroutines` module.
 *
 * Currently applied to [LogContextHolder.setContext][dev.shivathapaa.logger.core.LogContextHolder.setContext],
 * which is the bridge between the coroutine context machinery in `logger-coroutines`
 * and the platform-specific context storage in `logger`. Application code should use
 * [Logger.withContext][dev.shivathapaa.logger.api.Logger.withContext],
 * [LogContextHolder.withContext][dev.shivathapaa.logger.core.LogContextHolder.withContext],
 * or [withLogContext][dev.shivathapaa.logger.coroutines.withLogContext] instead.
 *
 * These APIs may change incompatibly or be removed in future releases without notice.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal KMP Logger API reserved for the logger-coroutines module. " +
            "Use Logger.withContext, LogContextHolder.withContext, or withLogContext instead. " +
            "This API may change incompatibly or be removed without notice."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class InternalLoggerApi