
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlin.random.Random

data class ChatMessage(
    val owner: Owner,
    val content: Content
) {
    enum class Owner {
        Markov,
        User
    }

    sealed interface Content {
        object Loading : Content
        value class Text(val text: String) : Content
        value class Error(val text: String) : Content
    }
}

private const val CORNER_IMAGE_COUNT = 7
private const val SIDE_IMAGE_COUNT = 4

@Composable
fun ChatMessages(
    messages: List<ChatMessage>,
    muted: Boolean,
    onMutedChanged: (muted: Boolean) -> Unit
) {
    val scrollHandler = remember { ScrollHandler() }

    LaunchedEffect(messages) {
        scrollHandler.scrollBy(0.0, 20000.0)
    }

    Div(
        attrs = {
            classes(Styles.chatContainer)
        }
    ) {
        Div(
            attrs = {
                classes(Styles.ttsMutedSettingContainer)
            }
        ) {
            Span {
                Text("Mute TTS:")
            }

            Input(
                type = InputType.Checkbox,
                attrs = {
                    checked(muted)

                    onChange {
                        onMutedChanged(it.value)
                    }
                }
            )
        }

        Div(attrs = { classes(Styles.chatBackground) })

        Div(
           attrs = {
               classes(Styles.chatMessageBorderContainer)
           }
        ) {
            for (i in 0 until 4) {
                val cornerImageIndex = remember { Random.nextInt(CORNER_IMAGE_COUNT) }

                Img(
                    src = "box_corner_$cornerImageIndex.png",
                    attrs = {
                        classes(Styles.chatCorner)

                        style {
                            gridColumn((1 + (i / 2) * 2).toString())
                            gridRow((1 + (i % 2) * 2).toString())

                            transform {
                                rotate(
                                    when (i) {
                                        0 -> 90.deg
                                        1 -> 0.deg
                                        2 -> 180.deg
                                        3 -> 270.deg
                                        else -> error("Unreachable")
                                    }
                                )
                            }
                        }
                    }
                )
            }

            for (i in 0 until 2) {
                val sideImageIndex = remember { Random.nextInt(SIDE_IMAGE_COUNT) }

                Div(
                    attrs = {
                        style {
                            backgroundImage("url('box_side_${sideImageIndex}_horizontal.png')")
                            backgroundRepeat("repeat-x")
                            margin(10.px)
                            height(23.px)
                            backgroundSize("auto 23px")
                            gridColumn(1, 4)
                            gridRow((1 + i * 2).toString())

                            if (i == 1) {
                                alignSelf(AlignSelf.End)
                            }
                        }
                    }
                ) {

                }
            }

            for (i in 0 until 2) {
                val sideImageIndex = remember { Random.nextInt(SIDE_IMAGE_COUNT) }

                Div(
                    attrs = {
                        style {
                            backgroundImage("url('box_side_${sideImageIndex}_vertical.png')")
                            backgroundRepeat("repeat-y")
                            margin(10.px)
                            width(23.px)
                            backgroundSize("23px auto")
                            gridColumn((1 + i * 2).toString())
                            gridRow(1, 4)

                            if (i == 1) {
                                justifySelf("end")
                            }
                        }
                    }
                ) {

                }
            }

            Div(
                attrs = {
                    classes(Styles.chatMessageContainer)
                    scrollHandler.run { install() } // Should probably be implemented with a context receiver as soon as they are stable
                }
            ) {
                for (message in messages) {
                    Div(attrs = { classes(Styles.chatMessage, if (message.owner == ChatMessage.Owner.Markov) Styles.chatMessageMarkov else Styles.chatMessageUser) }) {
                        when (message.content) {
                            ChatMessage.Content.Loading -> {
                                Img(
                                    src = "loading.svg",
                                    attrs = { classes(Styles.loadingIcon) }
                                )
                            }
                            is ChatMessage.Content.Text -> {
                                Span {
                                    Text(message.content.text)
                                }
                            }
                            is ChatMessage.Content.Error -> {
                                Span(attrs = { classes(Styles.chatMessageError) }) {
                                    Text(message.content.text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}