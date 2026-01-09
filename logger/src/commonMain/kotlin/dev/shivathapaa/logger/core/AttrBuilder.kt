package dev.shivathapaa.logger.core

class AttrBuilder {
    private val map = mutableMapOf<String, Any?>()

    fun attr(key: String, value: Any?) {
        map[key] = value
    }

    internal fun build(): Map<String, Any?> = map
}
