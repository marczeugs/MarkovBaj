package components
import CompletedAchievement
import LocalStorageBackedSnapshotStateMap
import ScrollHandler
import Styles
import achievements
import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

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
    onMutedChanged: (muted: Boolean) -> Unit,
    achievementCompletionMap: LocalStorageBackedSnapshotStateMap<Int, CompletedAchievement>
) {
    val scrollHandler = remember { ScrollHandler() }

    LaunchedEffect(messages) {
        scrollHandler.scrollBy(0.0, 20000.0)
    }

    Div(attrs = { classes(Styles.chatContainer) }) {
        Img(
            src = "img/achievements/trophy.webp",
            attrs = {
                classes(Styles.achievementCompleteTrophyIcon, *(if (achievementCompletionMap.size == achievements.size) arrayOf() else arrayOf(Styles.hidden)))
                title("You have completed all the achievements! Never doubt the god gamer forsenSmug")

                onClick {
                    window.alert("You have completed all the achievements! Never doubt the god gamer forsenSmug")
                }
            }
        )

        Div(
            attrs = {
                classes(Styles.ttsMutedSettingContainer)

                onClick {
                    onMutedChanged(!muted)
                }
            }
        ) {
            Img(
                src = "img/tts_off.webp",
                attrs = { classes(Styles.ttsMutedSettingIcon, *(if (muted) arrayOf() else arrayOf(Styles.hidden))) }
            )

            Img(
                src = "img/tts_on.webp",
                attrs = { classes(Styles.ttsMutedSettingIcon, *(if (!muted) arrayOf() else arrayOf(Styles.hidden))) }
            )
        }

        Div(attrs = { classes(Styles.chatBackground) })

        Div(attrs = { classes(Styles.chatBorderContainer) }) {
            for (i in 0 until 4) {
                val cornerImageIndex = remember { Random.nextInt(CORNER_IMAGE_COUNT) }

                Img(
                    src = "img/chatmessages/box_corner_$cornerImageIndex.webp",
                    attrs = {
                        classes(Styles.chatBorderCorner)

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
                            backgroundImage("url('img/chatmessages/box_side_${sideImageIndex}_horizontal.webp')")
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
                ) { }
            }

            for (i in 0 until 2) {
                val sideImageIndex = remember { Random.nextInt(SIDE_IMAGE_COUNT) }

                Div(
                    attrs = {
                        style {
                            backgroundImage("url('img/chatmessages/box_side_${sideImageIndex}_vertical.webp')")
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
                    Div(attrs = { classes(Styles.chatMessage) }) {
                        Span(attrs = { classes(if (message.owner == ChatMessage.Owner.User) Styles.chatMessageConsoleUser else Styles.chatMessageConsoleMarkov) }) {
                            Text("${if (message.owner == ChatMessage.Owner.User) "baj" else "markov"}@markovonline")
                        }

                        Span(attrs = { classes(Styles.chatMessageConsoleUnimportant) }) {
                            Text(":")
                        }

                        Span(attrs = { classes(Styles.chatMessageConsoleLocation) }) {
                            Text("~")
                        }

                        Span(attrs = { classes(Styles.chatMessageConsoleUnimportant) }) {
                            Text("$ ")
                        }

                        when (message.content) {
                            ChatMessage.Content.Loading -> {
                                var loadingIconCharacterIndex by remember { mutableStateOf(0) }

                                LaunchedEffect(Unit) {
                                    while (isActive) {
                                        loadingIconCharacterIndex++
                                        delay(200.milliseconds)
                                    }
                                }

                                Span {
                                    Text("[${
                                        when (loadingIconCharacterIndex % 4) {
                                            0 -> '|'
                                            1 -> '/'
                                            2 -> '-'
                                            3 -> '\\'
                                            else -> error("Unreachable")
                                        }
                                    }]")
                                }
                            }
                            is ChatMessage.Content.Text -> {
                                Span {
                                    Text(message.content.text)
                                }
                            }
                            is ChatMessage.Content.Error -> {
                                Span(attrs = { classes(Styles.chatMessageError) }) {
                                    Text("[EXCEPTION] ${message.content.text}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}