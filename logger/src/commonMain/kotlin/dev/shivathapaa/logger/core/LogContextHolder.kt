package dev.shivathapaa.logger.core

/**
 * A platform-specific holder for the current [LogContext].
 * This object is expected to be implemented on each platform to manage the active log context.
 */
expect object LogContextHolder {

    /** Returns the currently active log context. */
    fun current(): LogContext

    /**
     * Executes [block] with [ctx] merged into the current context.
     * The context MUST be restored to its original state even if [block] throws an exception.
     *
     * @param ctx The context to merge into the current context for the duration of the block.
     * @param block The block of code to execute with the merged context.
     */
    fun withContext(
        ctx: LogContext,
        block: () -> Unit
    )
}