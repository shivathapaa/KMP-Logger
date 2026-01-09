package dev.shivathapaa.logger.core

expect object LogContextHolder {

    /** Returns the currently active log context. */
    fun current(): LogContext

    /**
     * Executes [block] with [ctx] merged into the current context.
     * Context MUST be restored even if [block] throws.
     */
    fun withContext(
        ctx: LogContext,
        block: () -> Unit
    )
}