
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger("MarkovBaj:General")

suspend fun main() {
    val botCoroutineScope = CoroutineScope(Dispatchers.Default)

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
                    values.take(CommonConstants.consideredValuesForGeneration).map { it?.trim() },
                    values.drop(1).take(CommonConstants.consideredValuesForGeneration).map { it?.trim() }
                )
            }
        )
    }

    logger.info("Building the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")


    val redditClient = setupRedditClient()

    botCoroutineScope.launch {
        setupBackendServer(redditClient, json, markovChain)
    }

    botCoroutineScope.launch {
        setupRedditMessageSenderWebSocket(redditClient, json)
    }

    botCoroutineScope.launch {
        setupDiscordBot(markovChain)
    }

    botCoroutineScope.launch(Dispatchers.IO) {
        setupTwitchBot(markovChain)
    }

    setupRedditBot(redditClient, markovChain)
}