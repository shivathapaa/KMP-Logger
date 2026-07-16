package dev.shivathapaa.logger.core

/**
 * Returns the name of the thread on which this function is called.
 *
 * On Android, JVM, and Apple this reflects the actual OS or runtime thread name.
 *
 * On JS and WasmJS the runtime is single-threaded, so `"main"` is accurate.
 * On Linux and MinGW it also returns `"main"`, but that is a **placeholder, not a
 * fact**: Kotlin/Native coroutines can run on a multi-threaded worker pool
 * (`Dispatchers.Default`), so the reported name may not match the real thread.
 */
internal expect fun currentThreadName(): String