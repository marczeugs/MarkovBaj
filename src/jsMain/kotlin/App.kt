
import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val httpClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

@Composable
fun App() {
    var talking by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var muted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        console.log("the pog people: Scrafi1, ugworm_, okay_dudee, john7623, capybaraguyß, nishabtam, gakibas")
    }

    Style(Styles)

    Div(attrs = { classes(Styles.rootElement) }) {
        Markov(
            talking = talking,
            muted = muted
        )

        ChatMessages(
            messages = messages,
            muted = muted,
            onMutedChanged = {
                muted = it
            }
        )

        ChatInput(
            onMessageSent = { message ->
                messages = messages + ChatMessage(
                    owner = ChatMessage.Owner.User,
                    content = ChatMessage.Content.Text(message)
                )

                delay(1.seconds)

                val messagesBeforeAnswer = messages

                messages = messagesBeforeAnswer + ChatMessage(
                    owner = ChatMessage.Owner.Markov,
                    content = ChatMessage.Content.Loading
                )

                delay(0.5.seconds)

                try {
                    val response = httpClient.get("http://localhost/api/v1/query?input=$message").body<String>()

                    val ttsResponse = if (!muted) {
                        httpClient.get("https://api.streamelements.com/kappa/v2/speech") {
                            parameter("voice", "Brian")
                            parameter("text", response)
                        }.also {
                            check(it.status.isSuccess()) { it.bodyAsText() }
                        }
                    } else {
                        null
                    }

                    messages = messagesBeforeAnswer + ChatMessage(
                        owner = ChatMessage.Owner.Markov,
                        content = ChatMessage.Content.Text(response)
                    )

                    talking = true

                    if (ttsResponse != null) {
                        val audio = Audio(URL.createObjectURL(Blob(arrayOf(ttsResponse.readBytes()))))

                        suspendCoroutine {
                            var cancelled = false

                            audio.onended = {
                                if (!cancelled) {
                                    it.resume(Unit)
                                }
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                while (isActive) {
                                    if (muted) {
                                        cancelled = true
                                        it.resume(Unit)
                                        audio.pause()

                                        break
                                    }

                                    delay(100.milliseconds)
                                }
                            }

                            audio.play()
                        }
                    }

                    true
                } catch (e: Exception) {
                    console.error("Error while requesting response:", e)

                    messages = messagesBeforeAnswer + ChatMessage(
                        owner = ChatMessage.Owner.Markov,
                        content = ChatMessage.Content.Error("Could not fetch response.")
                    )

                    false
                } finally {
                    talking = false
                }
            }
        )
    }
}