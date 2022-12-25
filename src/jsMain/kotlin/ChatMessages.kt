
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

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

@Composable
fun ChatMessages(
    messages: List<ChatMessage>,
) {
    val scrollHandler = remember { ScrollHandler() }

    LaunchedEffect(messages) {
        scrollHandler.scrollBy(0.0, 20000.0)
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