package dev.shivathapaa.logger.api

enum class LogLevel(val priority: Int, val emoji: String) {
    VERBOSE(0, "💜"),
    DEBUG(1, "💚"),
    INFO(2, "💙"),
    WARN(3, "💛"),
    ERROR(4, "❤️"),
    FATAL(5, "💔"),
    OFF(6, "❌")
}
