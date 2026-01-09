package dev.shivathapaa.logger.core

import android.util.Log

internal actual class PlatformLogger actual constructor() {
    actual fun v(message: String, tag: String) {
        Log.v(tag, message)
    }

    actual fun d(message: String, tag: String) {
        Log.d(tag, message)
    }

    actual fun i(message: String, tag: String) {
        Log.i(tag, message)
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.wtf(tag, message, throwable)
        } else {
            Log.wtf(tag, message)
        }
    }
}