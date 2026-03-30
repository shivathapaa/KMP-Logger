package dev.shivathapaa.logger.core

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun dateNow(): Double

internal actual fun currentTimeMillis(): Long = dateNow().toLong()