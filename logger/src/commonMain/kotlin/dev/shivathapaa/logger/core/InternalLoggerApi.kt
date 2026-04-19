package dev.shivathapaa.logger.core

/**
 * Marks API that is internal to the KMP Logger library and reserved exclusively for
 * the `logger-coroutines` module.
 *
 * Currently applied to [LogContextHolder.setContext][dev.shivathapaa.logger.core.LogContextHolder.setContext],
 * which is the bridge between the coroutine context machinery in `logger-coroutines`
 * and the platform-specific context storage in `logger`. Application code should use
 * [LogContextHolder.withContext][dev.shivathapaa.logger.core.LogContextHolder.withContext],
 * [LogContextHolder.withSuspendingContext][dev.shivathapaa.logger.core.LogContextHolder.withSuspendingContext],
 * or [withLogContext][dev.shivathapaa.logger.coroutines.withLogContext] instead.
 *
 * These APIs may change incompatibly or be removed in future releases without notice.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal KMP Logger API reserved for the logger-coroutines module. " +
            "Use LogContextHolder.withContext, withSuspendingContext, or withLogContext instead. " +
            "This API may change incompatibly or be removed without notice."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class InternalLoggerApi