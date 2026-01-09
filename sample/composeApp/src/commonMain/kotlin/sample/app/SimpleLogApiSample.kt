package sample.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.shivathapaa.logger.api.Log
import dev.shivathapaa.logger.api.loggerD
import dev.shivathapaa.logger.api.loggerE
import dev.shivathapaa.logger.api.loggerI
import dev.shivathapaa.logger.api.loggerV
import dev.shivathapaa.logger.api.loggerW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SimpleLogSample(
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        Log.setDefaultTag("SimpleLogDemo")
    }

    Column(
        modifier = modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Simple Log API Demo", style = MaterialTheme.typography.headlineMedium)
        Text("Check console for log outputs", color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Text("Basic Logging", style = MaterialTheme.typography.titleMedium)
        DemoButton("1. All Log Levels", ::allLevels)
        DemoButton("2. With Custom Tags", ::customTags)
        DemoButton("3. With Exceptions", ::exceptions)
        DemoButton("4. Default Tag Behavior", ::defaultTag)

        Spacer(Modifier.height(8.dp))

        Text("Logger Wrappers", style = MaterialTheme.typography.titleMedium)
        DemoButton("5. Class-Based Logger", ::classBased)
        DemoButton("6. Module-Based Logger", ::moduleBased)
        DemoButton("7. Multiple Wrappers", ::multipleWrappers)

        Spacer(Modifier.height(8.dp))

        Text("Extension Functions", style = MaterialTheme.typography.titleMedium)
        DemoButton("8. Extension Basics", ::extensionBasics)
        DemoButton("9. Extension in Class", ::extensionInClass)
        DemoButton("10. Extension with Errors", ::extensionErrors)

        Spacer(Modifier.height(8.dp))

        Text("Real-World Examples", style = MaterialTheme.typography.titleMedium)
        DemoButton("11. API Request Logging", ::apiRequest)
        DemoButton("12. ViewModel Logging", ::viewModel)
        DemoButton("13. Repository Logging", ::repository)
        DemoButton("14. Service Logging", ::service)

        Spacer(Modifier.height(8.dp))

        Text("Edge Cases", style = MaterialTheme.typography.titleMedium)
        DemoButton("15. Empty Messages", ::emptyMessage)
        DemoButton("16. Long Messages", ::longMessages)
        DemoButton("17. Special Characters", ::specialChars)
        DemoButton("18. Null Throwables", ::nullThrowables)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { fatal() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("FATAL LOG (Will Crash)")
        }
    }
}


fun allLevels() {
    printHeader("1. ALL LOG LEVELS - Simple API")

    Log.v("VERBOSE: Most detailed logging")
    Log.d("DEBUG: Debugging information")
    Log.i("INFO: General information")
    Log.w("WARN: Warning message")
    Log.e("ERROR: Error message")
    // Fatal skipped to prevent crash

    println("* All log levels demonstrated (except FATAL)")
}

fun customTags() {
    printHeader("2. WITH CUSTOM TAGS")

    Log.d("Default tag message")
    Log.d("Network module message", tag = "Network")
    Log.i("Auth module message", tag = "Auth")
    Log.w("Database warning", tag = "Database")
    Log.e("Cache error", tag = "Cache")

    println("* Each log can have its own tag")
}

fun exceptions() {
    printHeader("3. WITH EXCEPTIONS")

    // Simple exception
    try {
        throw IllegalArgumentException("Invalid parameter")
    } catch (e: Exception) {
        Log.e("Caught exception", throwable = e)
    }

    // Exception with custom tag
    try {
        val result = 10 / 0
    } catch (e: ArithmeticException) {
        Log.e("Math error", tag = "Calculator", throwable = e)
    }

    // Warning with exception
    try {
        throw IllegalStateException("Recoverable error")
    } catch (e: Exception) {
        Log.w("Non-critical error", throwable = e)
    }

    println("* Exceptions logged with stack traces")
}

fun defaultTag() {
    printHeader("4. DEFAULT TAG BEHAVIOR")

    println("Initial default tag: SimpleLogDemo")
    Log.i("Message with default tag 'SimpleLogDemo'")

    Log.setDefaultTag("CustomApp")
    println("\nChanged default tag to: CustomApp")
    Log.i("Message with default tag 'CustomApp'")

    Log.i("Still using 'CustomApp'")
    Log.d("Debug with 'CustomApp'", tag = "OverrideTag")
    Log.i("Back to 'CustomApp'")

    // Restore
    Log.setDefaultTag("SimpleLogDemo")
    println("\n* Default tag can be changed at runtime")
}

fun classBased() {
    printHeader("5. CLASS-BASED LOGGER")

    val viewModel = SampleViewModel()
    viewModel.initialize()
    viewModel.fetchData()
    viewModel.handleError()

    println("* Class name automatically used as tag")
}

class SampleViewModel {
    private val log = Log.withClassTag<SampleViewModel>()

    fun initialize() {
        log.i("ViewModel initialized")
    }

    fun fetchData() {
        log.d("Fetching data...")
        log.i("Data fetched successfully")
    }

    fun handleError() {
        try {
            throw Exception("Simulated error")
        } catch (e: Exception) {
            log.e("Error occurred", throwable = e)
        }
    }
}

fun moduleBased() {
    printHeader("6. MODULE-BASED LOGGER")

    NetworkModule.connect()
    DatabaseModule.query()
    CacheModule.store()

    println("* Each module has its own logger with custom tag")
}

object NetworkModule {
    private val log = Log.withTag("Network")

    fun connect() {
        log.d("Connecting to server...")
        log.i("Connected successfully")
    }
}

object DatabaseModule {
    private val log = Log.withTag("Database")

    fun query() {
        log.d("Executing query")
        log.i("Query completed: 5 rows")
    }
}

object CacheModule {
    private val log = Log.withTag("Cache")

    fun store() {
        log.d("Storing in cache")
        log.i("Cache updated")
    }
}

fun multipleWrappers() {
    printHeader("7. MULTIPLE WRAPPERS")

    val authLog = Log.withTag("Auth")
    val paymentLog = Log.withTag("Payment")
    val analyticsLog = Log.withTag("Analytics")

    authLog.i("User logged in")
    paymentLog.d("Processing payment...")
    analyticsLog.i("Event tracked: login")

    authLog.d("Validating token")
    paymentLog.i("Payment successful")
    analyticsLog.d("Event queued for upload")

    println("* Multiple wrappers can coexist")
    println("  Auth tag: ${authLog.getTag()}")
    println("  Payment tag: ${paymentLog.getTag()}")
    println("  Analytics tag: ${analyticsLog.getTag()}")
}

fun extensionBasics() {
    printHeader("8. EXTENSION BASICS")

    val demo = ExtensionDemo()
    demo.testExtensions()

    println("* Extensions use class name as tag automatically")
}

class ExtensionDemo {
    fun testExtensions() {
        loggerV("Verbose log")
        loggerD("Debug log")
        loggerI("Info log")
        loggerW("Warning log")
        loggerE("Error log")
        // loggerFatal would crash
    }
}

fun extensionInClass() {
    printHeader("9. EXTENSION IN CLASS")

    val service = UserService()
    service.registerUser("john@example.com")

    println("* Extensions work seamlessly in class methods")
}

class UserService {
    fun registerUser(email: String) {
        loggerD("Starting user registration")
        loggerI("Validating email: $email")

        if (email.contains("@")) {
            loggerI("Email valid")
            loggerI("User registered successfully")
        } else {
            loggerW("Invalid email format")
        }
    }
}

fun extensionErrors() {
    printHeader("10. EXTENSION WITH ERRORS")

    val processor = DataProcessor()
    processor.process()

    println("* Extensions handle errors with throwables")
}

class DataProcessor {
    fun process() {
        loggerD("Processing started")

        try {
            throw RuntimeException("Processing failed")
        } catch (e: Exception) {
            loggerE("Error during processing", throwable = e)
        }

        try {
            throw IllegalStateException("Invalid state")
        } catch (e: Exception) {
            loggerW("Recovered from error", throwable = e)
        }
    }
}

fun apiRequest() {
    printHeader("11. API REQUEST LOGGING")

    val api = ApiClient()
    CoroutineScope(Dispatchers.IO).launch {
        api.get("/users/123")
        api.post("/users", "{'name': 'John'}")

        println("* Real-world API logging example")
    }
}

class ApiClient {
    private val log = Log.withTag("API")

    suspend fun get(endpoint: String) {
        log.d("GET $endpoint")
        delay(50)
        log.i("GET $endpoint - 200 OK (${50}ms)")
    }

    suspend fun post(endpoint: String, body: String) {
        log.d("POST $endpoint")
        log.d("Body: $body")

        try {
            // Simulate error
            if (body.isEmpty()) {
                throw IllegalArgumentException("Empty body")
            }
            delay(75)
            log.i("POST $endpoint - 201 Created (${75}ms)")
        } catch (e: Exception) {
            log.e("POST $endpoint - Failed", throwable = e)
        }
    }
}

fun viewModel() {
    printHeader("12. VIEWMODEL LOGGING")

    val viewModel = ProfileViewModel()
    CoroutineScope(Dispatchers.Default).launch {
        viewModel.loadProfile()
        viewModel.updateProfile()

        println("* ViewModel logging pattern")
    }
}

class ProfileViewModel {
    suspend fun loadProfile() {
        loggerD("Loading profile...")
        delay(50)
        loggerI("Profile loaded")
    }

    suspend fun updateProfile() {
        loggerD("Updating profile...")

        try {
            delay(50)
            loggerI("Profile updated successfully")
        } catch (e: Exception) {
            loggerE("Failed to update profile", throwable = e)
        }
    }
}

fun repository() {
    printHeader("13. REPOSITORY LOGGING")

    CoroutineScope(Dispatchers.Default).launch {
        val repo = UserRepository()
        repo.getUser(123)
        repo.saveUser(123)

        println("* Repository logging pattern")
    }
}

class UserRepository {
    private val log = Log.withTag("UserRepo")

    suspend fun getUser(id: Int) {
        log.d("Fetching user: $id")
        delay(30)
        log.i("User $id fetched from cache")
    }

    suspend fun saveUser(id: Int) {
        log.d("Saving user: $id")

        try {
            delay(40)
            log.i("User $id saved to database")
        } catch (e: Exception) {
            log.e("Failed to save user $id", throwable = e)
        }
    }
}

fun service() {
    printHeader("14. SERVICE LOGGING")

    val service = NotificationService()
    service.sendNotification("Welcome!")

    println("* Service logging pattern")
}

class NotificationService {
    fun sendNotification(message: String) {
        CoroutineScope(Dispatchers.Default).launch {
            loggerD("Preparing notification: $message")
            loggerD("Checking permissions...")
            loggerI("Permissions granted")
            loggerD("Sending notification...")
            delay(30)
            loggerI("Notification sent successfully")
        }
    }
}

fun emptyMessage() {
    printHeader("15. EMPTY MESSAGES")

    Log.d("")
    Log.i("")
    Log.w("", throwable = Exception("But has exception"))

    val log = Log.withTag("EmptyTest")
    log.d("")
    log.i("")

    println("* Empty messages are allowed")
}

fun longMessages() {
    printHeader("16. LONG MESSAGES")

    val longMessage = "This is a very long message. ".repeat(20)

    Log.d(longMessage)
    Log.i(longMessage, tag = "LongMsg")

    val log = Log.withTag("Verbose")
    log.d(longMessage)

    println("* Long messages are handled correctly")
    println("  Message length: ${longMessage.length} chars")
}

fun specialChars() {
    printHeader("17. SPECIAL CHARACTERS")

    Log.d("Message with émojis: 🎉 🚀 ✨")
    Log.i("Unicode: こんにちは 你好 مرحبا")
    Log.w("Special chars: @#$%^&*()")
    Log.e("Newlines:\nLine 1\nLine 2\nLine 3")

    val log = Log.withTag("Special")
    log.d("Tab\tcharacters\there")

    println("* Special characters handled")
}

fun nullThrowables() {
    printHeader("18. NULL THROWABLES")

    Log.w("Warning without exception", throwable = null)
    Log.e("Error without exception", throwable = null)

    val log = Log.withTag("NullTest")
    log.w("Another warning", throwable = null)
    log.e("Another error", throwable = null)

    println("* Null throwables handled gracefully")
}

fun fatal() {
    CoroutineScope(Dispatchers.Default).launch {
        printHeader("FATAL LOG - WILL CRASH")

        println("About to log FATAL - app will crash in 2 seconds...")
        delay(2000)

        Log.fatal("Critical system failure", throwable = IllegalStateException("Unrecoverable"))

        println("This line will never execute")
    }
}