import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    MaterialTheme {
        var isDragging by remember { mutableStateOf(false) }
        var offset by remember { mutableStateOf(0f) }

        val scrollState = rememberLazyListState()
        val data = (0 until 1000).map { "Item $it" }
        var draggingStart by remember { mutableStateOf(0L) }
        var draggingTime by remember { mutableStateOf(0L) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = rememberDraggableState { delta ->
                        isDragging = true
                        println("Delta: $delta")
                        scope.launch {
                            scrollState.scrollBy(-delta)
                        }
                        offset += delta
                        offset = offset.coerceIn(-800f, 800f)
                    },
                    onDragStarted = {
                        draggingStart = System.currentTimeMillis()
                        offset = 0F
                    },
                    onDragStopped = {
                        isDragging = false
                        val draggingEnd = System.currentTimeMillis()
                        draggingTime = draggingEnd - draggingStart
                    },
                    orientation = Orientation.Vertical
                ),
            state = scrollState
        ) {
            items(items = data) { i ->
                Text(
                    i,
                    Modifier
                        .padding(14.dp)
                        .fillMaxWidth()
                )
            }
        }

        LaunchedEffect(isDragging) {
            if (!isDragging) {
                var dragging = (draggingTime.toFloat() / 100F)
                if (dragging < 1.5) dragging /= 3F
                if (dragging > 3) dragging *= 3F

                val finalValue = -offset / dragging
                println("Offset: $offset, draggingTime: $draggingTime, draggingOffset: $dragging")
                println("Final value: $finalValue (ignoring < 35)")
                if (abs(finalValue) > 35){
                    scrollState.animateScrollBy(
                        value = -offset / dragging,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
