package dev.shivathapaa.logger.formatters

/**
 * Utility function to format a map into a string representation.
 */
internal fun formatMap(map: Map<String, Any?>): String =
    map.entries.joinToString(
        prefix = "{",
        postfix = "}"
    ) { (k, v) -> "$k=$v" }