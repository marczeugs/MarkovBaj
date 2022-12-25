
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.TextInput

@Composable
fun ChatInput(
    onMessageSent: suspend (message: String) -> Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var currentlyProcessing by remember { mutableStateOf(false) }

    val focusHandler = remember { FocusHandler() }

    LaunchedEffect(currentlyProcessing) {
        if (!currentlyProcessing) {
            focusHandler.focus()
        }
    }

    LaunchedEffect(Unit) {
        focusHandler.focus()
    }

    TextInput(
        value = text,
        attrs = {
            classes(Styles.chatInput)

            focusHandler.run { install() } // Should probably be implemented with a context receiver as soon as they are stable

            if (currentlyProcessing) {
                disabled()
            }

            placeholder("Type a message and send with Enter.")

            onInput {
                text = it.value
            }

            onKeyDown {
                if (it.key == "Enter") {
                    coroutineScope.launch {
                        currentlyProcessing = true

                        val success = onMessageSent(text)

                        if (success) {
                            text = ""
                        }

                        currentlyProcessing = false
                    }
                }
            }
        }
    )
}