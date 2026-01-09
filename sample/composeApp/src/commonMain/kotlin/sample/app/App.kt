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

@Composable
fun App() {
    val isSimpleApiLoggerSampleVisible = rememberSaveable { mutableStateOf(true) }

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
                    isSimpleApiLoggerSampleVisible.value = !isSimpleApiLoggerSampleVisible.value
                },
                shape = MaterialTheme.shapes.small
            ) {
                val buttonText = if (isSimpleApiLoggerSampleVisible.value) {
                    "Checkout structured api samples"
                } else {
                    "Checkout simple api samples"
                }
                Text(buttonText, modifier = Modifier.animateContentSize())
            }

            AnimatedContent(
                targetState = isSimpleApiLoggerSampleVisible.value
            ) { isSimpleVisible ->
                when (isSimpleVisible) {
                    true -> SimpleLogSample()
                    false -> StructuredLogApiSample()
                }
            }
        }
    }
}