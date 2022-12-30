package components

import Styles
import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLAudioElement
import kotlin.time.Duration.Companion.seconds

@Composable
fun NotificationDisplay(
    notificationQueue: MutableSharedFlow<String?>
) {
    var currentNotificationContent by remember { mutableStateOf("") }
    var notificationShown by remember { mutableStateOf(false) }

    val notificationSound = remember {
        (window.document.createElement("audio") as HTMLAudioElement).apply {
            src = "tada.mp3"
        }
    }

    LaunchedEffect(Unit) {
        notificationQueue.collect {
            if (it == null) {
                return@collect
            }

            currentNotificationContent = it
            notificationShown = true

            notificationSound.apply { currentTime = 0.0 }.play()

            delay(5.seconds)

            notificationShown = false

            delay(1.seconds)
        }
    }

    Div(
        attrs = {
            classes(Styles.notificationContainer)

            style {
                marginTop(if (notificationShown) 0.px else (-100).px)
            }
        }
    ) {
        Div(attrs = { classes(Styles.notificationBackground) })

        Div(attrs = { classes(Styles.smallBorderContainer, Styles.notificationInnerContainer) }) {
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

            Div(attrs = { classes(Styles.notificationContent) }) {
                Text(currentNotificationContent)
            }
        }
    }
}