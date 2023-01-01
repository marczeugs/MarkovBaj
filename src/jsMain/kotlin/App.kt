
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
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Clock
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val httpClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}


private val easterEggInputQueue = listOf("ArrowUp", "ArrowUp", "ArrowDown", "ArrowDown", "ArrowLeft", "ArrowRight", "ArrowLeft", "ArrowRight", "b", "a")

@Composable
fun App() {
    var talking by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var muted by remember { mutableStateOf(false) }

    val achievementCompletionMap = remember { LocalStorageBackedSnapshotStateMap<Int, CompletedAchievement>("completedAchievements") }
    val notificationQueue = remember { MutableSharedFlow<String?>(replay = 10) }

    var easterEggInputQueueProgress by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        window.onkeydown = {
            if (it.key == easterEggInputQueue[easterEggInputQueueProgress]) {
                easterEggInputQueueProgress++
            }

            if (easterEggInputQueueProgress == easterEggInputQueue.size) {
                document.body!!.innerHTML = "<div style=\"background-image: url('forsen.gif'); background-size: contain; width: 100vw; height: 100vh; background-repeat: no-repeat;\">"
                easterEggInputQueueProgress = 0
            }
        }
    }

    LaunchedEffect(Unit) {
        // Shitty hack to fix 100vh excluding URL bar on mobile devices, see: https://stackoverflow.com/questions/52848856/100vh-height-when-address-bar-is-shown-chrome-mobile
        (window.document.documentElement as HTMLElement).style.setProperty("--vh", "${window.innerHeight.toDouble() / 100}px")

        window.onresize = {
            (window.document.documentElement as HTMLElement).style.setProperty("--vh", "${window.innerHeight.toDouble() / 100}px")
        }
    }

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
            },
            achievementCompletionMap = achievementCompletionMap
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
                    val response = withTimeout(10.seconds) {
                        httpClient.get(if ("localhost" in window.location.href) "http://localhost:7777/api/v1/query?input=$message" else "/api/v1/query?input=$message").let {
                            if (it.status.isSuccess()) {
                                it.body<String>()
                            } else {
                                throw MarkovBackendException(it.body())
                            }
                        }
                    }

                    val ttsResponse = if (!muted) {
                        withTimeout(10.seconds) {
                            httpClient.get("https://api.streamelements.com/kappa/v2/speech") {
                                parameter("voice", "Brian")
                                parameter("text", response)
                            }.also {
                                check(it.status.isSuccess()) { it.bodyAsText() }
                            }
                        }
                    } else {
                        null
                    }

                    for (achievement in achievements) {
                        if (
                            when (achievement.matcher) {
                                is Achievement.Matcher.KeywordList -> achievement.matcher.keywords.any { it in response.lowercase() && it !in message.lowercase() }
                                is Achievement.Matcher.Lambda -> achievement.matcher.matcher(message, response)
                                is Achievement.Matcher.Regex -> achievement.matcher.regex.containsMatchIn(response.lowercase()) && !achievement.matcher.regex.containsMatchIn(message.lowercase())
                            }
                            && achievement.id !in achievementCompletionMap
                        ) {
                            achievementCompletionMap[achievement.id] = CompletedAchievement(
                                instant = Clock.System.now(),
                                query = message,
                                response = response
                            )

                            console.log("Message \"$message\" with response \"$response\" rewarded user with achievement \"${achievement.name}\".")
                            notificationQueue.tryEmit("Achievement unlocked: ${achievement.name}")
                        }
                    }

                    messages = messagesBeforeAnswer + ChatMessage(
                        owner = ChatMessage.Owner.Markov,
                        content = ChatMessage.Content.Text(response)
                    )

                    talking = true

                    if (ttsResponse != null) {
                        val audio = Audio(URL.createObjectURL(Blob(arrayOf(ttsResponse.readBytes()), BlobPropertyBag(type = "audio/mpeg"))))

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
                } catch (e: MarkovBackendException) {
                    console.error("Backend error while requesting response:", e)

                    messages = messagesBeforeAnswer + ChatMessage(
                        owner = ChatMessage.Owner.Markov,
                        content = ChatMessage.Content.Error(e.information)
                    )

                    false
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

class MarkovBackendException(val information: String) : Exception()