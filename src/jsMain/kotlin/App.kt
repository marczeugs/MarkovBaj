
import androidx.compose.runtime.*
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

val httpClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

private val json = Json {
    allowStructuredMapKeys = true
}

@OptIn(ExperimentalTime::class)
@Composable
fun App() {
    var talking by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }

    val markovChain by remember {
        flow {
            console.log("Getting Markov chain data...")

            val data = httpClient.get("https://raw.githubusercontent.com/marczeugs/MarkovBaj/data/data.json").bodyAsText()

            console.log("Decoding chain...")

            val (markovChain, chainBuildTime) = measureTimedValue {
                val jsObject = JSON.parse<dynamic>(data)

                console.log(jsObject.chainStarts.weightMap as Array<Any>)
                console.log((jsObject.chainStarts.weightMap as Array<Any>).toList())
                console.log((jsObject.chainStarts.weightMap as Array<Any>).toList().chunked(2))
                console.log(Object.fromEntries<Int, Int>((jsObject.chainStarts.weightMap as Array<Any>).toList().chunked(2).map { it.toTypedArray() }.toTypedArray()))
                console.log(Object.fromEntries<Int, Int>(Object.entries(jsObject.wordHashMap).toList().chunked(2).map { it.toTypedArray() }.toTypedArray()))

                MarkovWordChain(
                    consideredValuesForGeneration = jsObject.consideredValuesForGeneration as Int,
                    wordHashMap = jsObject.wordHashMap as Map<Hash, Word>,
                    chainStarts = WeightedSet(Object.fromEntries((jsObject.chainStarts.weightMap as Array<Any>).toList().chunked(2) as Array<Array<Any>>)),
                    followingValues = Object.fromEntries((jsObject.followingValues as Array<Any>).toList().chunked(2) as Array<Array<Any>>)
                )
                //json.decodeFromString<MarkovWordChain>(data)
            }

            console.log("Decoding the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")
            console.log(markovChain)
            console.log(markovChain.consideredValuesForGeneration)

            emit(markovChain)
        }
    }.collectAsState(null)

    Style(Styles)

    Div(attrs = { classes(Styles.rootElement) }) {
        Markov(
            talking = talking
        )

        ChatMessages(
            messages = messages
        )

        ChatInput(
            onMessageSent = { message ->

                console.log("asdf2")
                messages = messages + ChatMessage(
                    owner = ChatMessage.Owner.User,
                    content = ChatMessage.Content.Text(message)
                )

                delay(1.seconds)

                val messagesBeforeAnswer = messages
                console.log("asdf3")

                messages = messagesBeforeAnswer + ChatMessage(
                    owner = ChatMessage.Owner.Markov,
                    content = ChatMessage.Content.Loading
                )

                delay(0.5.seconds)
                console.log(markovChain)
                console.log("asdf4 ${markovChain?.chainStarts?.weightMap?.keys}")

                try {
                    val response = markovChain?.let { markovChain ->
                        message.split(Regex("\\s+"))
                            .map { Word(it) }
                            .windowed(markovChain.consideredValuesForGeneration).shuffled()
                            .firstNotNullOfOrNull { potentialChainStart ->
                                if (markovChain.chainStarts.weightMap.keys.any { words -> words == potentialChainStart.map { markovChain.hashTransformation(it) } }) {
                                    markovChain.generateSequence(start = potentialChainStart).joinToString(" ") { it.word }.take(500)
                                } else {
                                    null
                                }
                            }
                            ?: markovChain.generateSequence().joinToString(" ") { it.word }.take(500)
                    } ?: run {
                        "Markov chain not ready yet."
                    }

                    console.log("asdf")

                    val ttsResponse = httpClient.get("https://api.streamelements.com/kappa/v2/speech") {
                        parameter("voice", "Brian")
                        parameter("text", response)
                    }

                    check(ttsResponse.status.isSuccess()) { ttsResponse.bodyAsText() }

                    messages = messagesBeforeAnswer + ChatMessage(
                        owner = ChatMessage.Owner.Markov,
                        content = ChatMessage.Content.Text(response)
                    )

                    talking = true

                    val audio = Audio(URL.createObjectURL(Blob(arrayOf(ttsResponse.readBytes()))))

                    suspendCancellableCoroutine {
                        audio.onended = {
                            it.resume(Unit)
                        }

                        audio.play()
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