package components
import FocusHandler
import Styles
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
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

    Div(attrs = { classes(Styles.chatInputContainer) }) {
        Div(attrs = { classes(Styles.chatInputBackground) })

        Div(attrs = { classes(Styles.chatInputBorderContainer, Styles.smallBorderContainer) }) {
            for (i in 0 until 2) {
                Img(
                    src = "img/chatinput/input_${if (i == 0) "left" else "right"}.webp",
                    attrs = {
                        classes(Styles.smallBorderHorizontalImage)

                        style {
                            gridColumn((1 + i * 2).toString())
                            gridRow(1, 4)
                        }
                    }
                )
            }

            for (i in 0 until 2) {
                Div(
                    attrs = {
                        style {
                            backgroundImage("url('img/chatinput/input_${if (i == 0) "top" else "bottom"}.webp')")
                            backgroundRepeat("repeat-x")
                            backgroundSize("auto 15px")
                            gridColumn("2")
                            gridRow((1 + i * 2).toString())
                        }
                    }
                ) { }
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
                                if (text.isBlank()) {
                                    return@launch
                                }

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
    }
}