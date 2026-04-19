package sample.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SampleScreen { SIMPLE, STRUCTURED, COROUTINE }

@Composable
fun App() {
    val screen = rememberSaveable { mutableStateOf(SampleScreen.SIMPLE) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    .animateContentSize(),
                onClick = {
                    screen.value = when (screen.value) {
                        SampleScreen.SIMPLE -> SampleScreen.STRUCTURED
                        SampleScreen.STRUCTURED -> SampleScreen.COROUTINE
                        SampleScreen.COROUTINE -> SampleScreen.SIMPLE
                    }
                },
                shape = MaterialTheme.shapes.small
            ) {
                val buttonText = when (screen.value) {
                    SampleScreen.SIMPLE -> "Switch to Structured API"
                    SampleScreen.STRUCTURED -> "Switch to Coroutine API"
                    SampleScreen.COROUTINE -> "Switch to Simple API"
                }
                Text(buttonText, modifier = Modifier.animateContentSize())
            }

            AnimatedContent(targetState = screen.value) { current ->
                when (current) {
                    SampleScreen.SIMPLE -> SimpleLogSample()
                    SampleScreen.STRUCTURED -> StructuredLogApiSample()
                    SampleScreen.COROUTINE -> CoroutineLogApiSample()
                }
            }
        }
    }
}