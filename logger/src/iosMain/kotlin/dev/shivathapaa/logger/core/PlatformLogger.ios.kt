package dev.shivathapaa.logger.core

import platform.Foundation.NSLog

internal actual class PlatformLogger actual constructor() {
    actual fun v(message: String, tag: String) {
        NSLog("💜VERBOSE $tag: $message")
    }

    actual fun d(message: String, tag: String) {
        NSLog("💚DEBUG $tag: $message")
    }

    actual fun i(message: String, tag: String) {
        NSLog("💙INFO $tag: $message")
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        NSLog("💛WARN $tag: $message")
        throwable?.let {
            NSLog("💛WARN $tag: ${it.message}\n${it.stackTraceToString()}")
        }
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        NSLog("❤️ERROR $tag: $message")
        throwable?.let {
            NSLog("❤️ERROR $tag: ${it.message}\n${it.stackTraceToString()}")
        }
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        NSLog("💔FATAL $tag: $message")
        throwable?.let {
            NSLog("💔FATAL $tag: ${it.message}\n${it.stackTraceToString()}")
        }
    }

}