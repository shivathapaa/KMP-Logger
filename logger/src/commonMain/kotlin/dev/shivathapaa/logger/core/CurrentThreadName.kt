package dev.shivathapaa.logger.core

/**
 * Returns the name of the thread on which this function is called.
 *
 * On platforms with real threading (Android, JVM, Apple), this reflects the
 * actual OS or runtime thread name. On single-threaded runtimes (JS, WasmJS,
 * Linux, Mingw) it returns `"main"`.
 */
internal expect fun currentThreadName(): String