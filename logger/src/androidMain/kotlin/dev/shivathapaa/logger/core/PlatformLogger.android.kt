package dev.shivathapaa.logger.core

import android.util.Log

internal actual class PlatformLogger actual constructor(tag: String) {
    actual fun v(message: String, tag: String, throwable: Throwable?) {
        Log.v(tag, withStackTrace(message, throwable))
    }

    actual fun d(message: String, tag: String, throwable: Throwable?) {
        Log.d(tag, withStackTrace(message, throwable))
    }

    actual fun i(message: String, tag: String, throwable: Throwable?) {
        Log.i(tag, withStackTrace(message, throwable))
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        Log.w(tag, withStackTrace(message, throwable))
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        Log.e(tag, withStackTrace(message, throwable))
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        Log.wtf(tag, withStackTrace(message, throwable))
    }

    // Appended here rather than passed to Log's throwable overloads: Log.getStackTraceString
    // drops the whole trace when any cause in the chain is an UnknownHostException.
    private fun withStackTrace(message: String, throwable: Throwable?): String =
        if (throwable == null) message else "$message\n${throwable.stackTraceToString().trimEnd()}"
}