package scripts

import CommonConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

@Serializable
private data class CommentsResponse(
    val data: List<Comment>
) {
    @Serializable
    data class Comment(
        val author: String,
        val body: String,
        val locked: Boolean,
        @SerialName("created_utc") val createdUtc: Long,
    )
}

suspend fun main() {
    val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    val client = HttpClient(CIO) {
        install(Logging)

        install(ContentNegotiation) {
            json(json)
        }
    }

    val urlTemplate = "https://api.pushshift.io/reddit/search/comment?subreddit=forsen&size=500&before="
    val maxJsonSize = 10 * 1024 * 1024 // 10 MiB

    val logger = KotlinLogging.logger("GenerateDataSetFromPushshift")

    val allFetchedComments = mutableListOf<String>()

    var lastTimestamp = System.currentTimeMillis() / 1000
    var lastJsonOutput = ""
    var last10PercentBarrier = 0

    do {
        try {
            val nextComments = client.get(urlTemplate + lastTimestamp).body<CommentsResponse>().data

            val filteredComments = nextComments
                .filter { !it.locked }
                .mapNotNull { sanitizeComment(it.author, it.body) }

            allFetchedComments.addAll(filteredComments)

            lastJsonOutput = json.encodeToString(allFetchedComments)

            logger.info {
                "Fetched ${filteredComments.size} comments before ${
                    DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(lastTimestamp))
                }, total comments: ${allFetchedComments.size}, JSON string size: ${lastJsonOutput.length} / $maxJsonSize (${
                    lastJsonOutput.length.toFloat() / maxJsonSize * 100
                }%)"
            }

            lastTimestamp = nextComments.last().createdUtc
        } catch (e: Exception) {
            logger.error { "Unable to fetch comments before ${DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(lastTimestamp))}, retrying..." }
            save("data-error.json", lastJsonOutput)
        }

        if ((lastJsonOutput.length.toFloat() / maxJsonSize * 100).toInt() / 10 != last10PercentBarrier) {
            last10PercentBarrier = (lastJsonOutput.length.toFloat() / maxJsonSize * 100).toInt() / 10
            save("data-${last10PercentBarrier * 10}.json", lastJsonOutput)
            logger.info { "Reached ${last10PercentBarrier * 10}%, saving backup..." }
        }

        delay(1.seconds)
    } while (lastJsonOutput.length < maxJsonSize)

    save("data.json", lastJsonOutput)
    logger.info { "Fetched ${allFetchedComments.size} comments in total." }
}

fun save(fileName: String, jsonOutput: String) {
    File(fileName).writeText(jsonOutput)
}