package dev.shivathapaa.logger.core

/**
 * Returns the name of the thread on which this function is called.
 *
 * On Android and JVM this reflects the actual runtime thread name. On Apple it is `"main"` on
 * the main thread and the thread's own name elsewhere, falling back to `"background"` for
 * unnamed threads (most GCD and coroutine worker threads).
 *
 * On JS and WasmJS the runtime is single-threaded, so `"main"` is accurate.
 * On Linux and MinGW it also returns `"main"`, but that is a **placeholder, not a
 * fact**: Kotlin/Native coroutines can run on a multi-threaded worker pool
 * (`Dispatchers.Default`), so the reported name may not match the real thread.
 */
internal expect fun currentThreadName(): String