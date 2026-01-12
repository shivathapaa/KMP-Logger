import dev.shivathapaa.logger.api.Log

fun main() {
    Log.d("Default tag message")
    Log.d("Network module message", tag = "Network")
    Log.i("Auth module message", tag = "Auth")
    Log.w("Database warning", tag = "Database")
    Log.e("Cache error", tag = "Cache")
}