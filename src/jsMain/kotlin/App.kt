
import androidx.compose.runtime.*
import components.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random
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

    val achievementCompletionMap = remember { LocalStorageBackedSnapshotStateMap<Int, Boolean>("achievements") }
    val notificationQueue = remember { MutableSharedFlow<String?>(replay = 10) }

    Style(Styles)

    Div(attrs = { classes(Styles.rootElement) }) {
        NotificationDisplay(
            notificationQueue = notificationQueue
        )

        MenuBox(
            achievementCompletionMap = achievementCompletionMap
        )

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

                delay(0.5.seconds)

                val messagesBeforeAnswer = messages

                messages = messagesBeforeAnswer + ChatMessage(
                    owner = ChatMessage.Owner.Markov,
                    content = ChatMessage.Content.Loading
                )

                delay(1.seconds)

                try {
                    val response = httpClient.get(if ("localhost" in window.location.href) "http://localhost:7777/api/v1/query?input=$message" else "/api/v1/query?input=$message").body<String>()

                    for (achievement in achievements) {
                        if (
                            when (achievement.matcher) {
                                is Achievement.Matcher.KeywordList -> achievement.matcher.keywords.any { it in response.lowercase() && it !in message.lowercase() }
                                is Achievement.Matcher.Lambda -> achievement.matcher.matcher(message, response)
                            }
                            && achievementCompletionMap[achievement.id] != true
                        ) {
                            console.log("Message \"$message\" with response \"$response\" rewarded user with achievement \"${achievement.name}\".")
                            achievementCompletionMap[achievement.id] = true
                            notificationQueue.tryEmit("Achievement unlocked: ${achievement.name}")
                        }
                    }

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

                            val scope = CoroutineScope(Dispatchers.Main)

                            val muteListener = scope.launch {
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

                            audio.onended = {
                                if (!cancelled) {
                                    scope.launch {
                                        muteListener.cancelAndJoin()
                                    }

                                    it.resume(Unit)
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