package dev.shivathapaa.logger.core

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSThread
import platform.Foundation.create

actual object LogContextHolder {
    @OptIn(BetaInteropApi::class)
    private val nsKey = NSString.create(string = "dev.shivathapaa.logger.LogContext")

    actual fun current(): LogContext =
        NSThread.currentThread.threadDictionary.objectForKey(nsKey) as? LogContext ?: LogContext()

    actual fun <T> withContext(ctx: LogContext, block: () -> T): T {
        val previous = current()
        val merged = previous.merge(ctx)
        NSThread.currentThread.threadDictionary.setObject(merged, forKey = nsKey)
        try {
            return block()
        } finally {
            if (previous.values.isEmpty()) {
                NSThread.currentThread.threadDictionary.removeObjectForKey(nsKey)
            } else {
                NSThread.currentThread.threadDictionary.setObject(previous, forKey = nsKey)
            }
        }
    }
}