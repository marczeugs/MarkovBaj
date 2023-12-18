
import backend.setupBackendServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger("MarkovBaj:General")

suspend fun main() = coroutineScope {
    logger.info { "Starting MarkovBaj Backend version ${BuildInfo.PROJECT_VERSION}, Build ${Instant.fromEpochMilliseconds(BuildInfo.PROJECT_BUILD_TIMESTAMP_MILLIS)}..." }


    val json = Json {
        ignoreUnknownKeys = true
    }

    val markovChain = MarkovChain<String?>(CommonConstants.consideredValuesForGeneration) { it?.trim() }

    logger.info("Building Markov chain...")

    val chainBuildTime = measureTime {
        val messages = json.decodeFromStream<List<String>>(File("data.json").inputStream())
        val messageData = messages.map { it.toWordParts() }

        markovChain.addData(
            messageData,
            messageData.flatMap { values ->
                listOf(
                    values.take(CommonConstants.consideredValuesForGeneration),
                    values.drop(1).take(CommonConstants.consideredValuesForGeneration)
                )
            }
        )
    }

    logger.info("Building the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")


    val eventFlow = MutableSharedFlow<ApiEvent>(extraBufferCapacity = 1)

    val redditClient = if (RuntimeVariables.Reddit.enabled) setupRedditClient() else null

    if (RuntimeVariables.Reddit.enabled) {
        launch {
            setupRedditBot(redditClient!!, markovChain, eventFlow)
        }
    } else {
        logger.warn("Reddit bot is not enabled.")
    }

    if (RuntimeVariables.Discord.enabled) {
        launch {
            setupDiscordBot(markovChain)
        }
    } else {
        logger.warn("Discord bot is not enabled.")
    }

    if (RuntimeVariables.Twitch.enabled) {
        launch {
            setupTwitchBot(markovChain)
        }
    } else {
        logger.warn("Twitch bot is not enabled.")
    }

    launch {
        setupBackendApiWebsocketServer(redditClient, json, eventFlow)
    }

    withContext(Dispatchers.IO) {
        setupBackendServer(redditClient, json, markovChain)
    }
}