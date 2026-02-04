package dev.shivathapaa.logger.core

/**
 * A builder for creating a map of log attributes.
 */
class AttrBuilder {
    private val map = mutableMapOf<String, Any?>()

    /**
     * Adds an attribute with the given key and value.
     *
     * @param key The attribute key.
     * @param value The attribute value.
     */
    fun attr(key: String, value: Any?) {
        map[key] = value
    }

    /**
     * Builds and returns the final attribute map.
     */
    internal fun build(): Map<String, Any?> = map
}
