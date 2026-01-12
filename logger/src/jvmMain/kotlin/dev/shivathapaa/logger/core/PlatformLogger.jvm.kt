package dev.shivathapaa.logger.core

import java.util.logging.Level
import java.util.logging.Logger

internal actual class PlatformLogger actual constructor(tag: String) {
    private val logger = Logger.getLogger(tag).apply {
        level = Level.ALL
        handlers.forEach { it.level = Level.ALL }
    }

    actual fun v(message: String, tag: String) {
        logger.finest(message)
    }

    actual fun d(message: String, tag: String) {
        logger.fine(message)
    }

    actual fun i(message: String, tag: String) {
        logger.info(message)
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.log(Level.WARNING, message, throwable)
        } else {
            logger.warning(message)
        }
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.log(Level.SEVERE, message, throwable)
        } else {
            logger.severe(message)
        }
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.log(Level.SEVERE, message, throwable)
        } else {
            logger.severe(message)
        }
    }
}