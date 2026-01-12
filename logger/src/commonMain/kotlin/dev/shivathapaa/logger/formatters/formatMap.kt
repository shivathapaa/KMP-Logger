package dev.shivathapaa.logger.formatters

internal fun formatMap(map: Map<String, Any?>): String =
    map.entries.joinToString(
        prefix = "{",
        postfix = "}"
    ) { (k, v) -> "$k=$v" }