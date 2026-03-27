package dev.shivathapaa.logger.core

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun currentTimeMillis(): Long =
    js("Date.now()").unsafeCast<Double>().toLong()