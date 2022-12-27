package scripts

import CommonConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
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

private sealed interface MessageExclusionCriteria {
    data class String(val value: kotlin.String) : MessageExclusionCriteria
    data class Regex(val value: kotlin.text.Regex) : MessageExclusionCriteria
}


suspend fun main() {
    val filteredAuthors = listOf(
        System.getenv("markovbaj_username").lowercase(),
        "[deleted]",
    )

    val replacedParts = mapOf(
        Regex("(?:^|\\s)*${CommonConstants.triggerKeyword}(?:$|\\s)*", RegexOption.IGNORE_CASE) to "", // Bot mentions
        Regex("(?:^|\\s)?:\\d+:(?:$|\\s)?", RegexOption.IGNORE_CASE) to " ", // Emotes
        Regex("&amp;#x200B;", RegexOption.IGNORE_CASE) to "\u200b", // Weird stuff with zero width spaces
        Regex("!?\\[(?:img|gif)]\\(.+\\)", RegexOption.IGNORE_CASE) to "", // Remove emotes and images
        Regex("\\n", RegexOption.IGNORE_CASE) to "", // Remove line breaks, handling them is just a pain
    )

    val messageExclusionCriteriaWordParts = listOf(
        MessageExclusionCriteria.String("neg"),
        MessageExclusionCriteria.String("nek"),
        MessageExclusionCriteria.String("sie"),
        MessageExclusionCriteria.String("hei"),
        MessageExclusionCriteria.String("ike"),
        MessageExclusionCriteria.String("tran"),
        MessageExclusionCriteria.String("14"),
        MessageExclusionCriteria.String("88"),
        MessageExclusionCriteria.String("kfc"),
        MessageExclusionCriteria.String("melon"),
        MessageExclusionCriteria.String("black"),
        MessageExclusionCriteria.String("white"),
        MessageExclusionCriteria.String("afric"),
        MessageExclusionCriteria.String("migra"),
        MessageExclusionCriteria.String("crack"),
        MessageExclusionCriteria.String("kill"),
        MessageExclusionCriteria.String("uicid"),
        MessageExclusionCriteria.String("murd"),
        MessageExclusionCriteria.String("etar"),
        MessageExclusionCriteria.String("underag"),
        MessageExclusionCriteria.String("gun"),
        MessageExclusionCriteria.String("jew"),
        MessageExclusionCriteria.String("semit"),
        MessageExclusionCriteria.String("gender"),
        MessageExclusionCriteria.String("fag"),
        MessageExclusionCriteria.String("minor"),
        MessageExclusionCriteria.String("lgb"),
        MessageExclusionCriteria.String("sex"),
        MessageExclusionCriteria.String("kys"),
        MessageExclusionCriteria.String("groom"),
        MessageExclusionCriteria.Regex(Regex("n.?word", RegexOption.IGNORE_CASE)),
        MessageExclusionCriteria.Regex(Regex("shoo?t", RegexOption.IGNORE_CASE)),
        MessageExclusionCriteria.Regex(Regex("self.?harm", RegexOption.IGNORE_CASE)),
        MessageExclusionCriteria.Regex(Regex("\\brac(?:e\\b|is)", RegexOption.IGNORE_CASE)),
        MessageExclusionCriteria.Regex(Regex("hate (?:th|ni|'?em)", RegexOption.IGNORE_CASE)),
        MessageExclusionCriteria.Regex(Regex("\\bni(?:g|$|[^a-z])", RegexOption.IGNORE_CASE)),
    )

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

    val logger = KotlinLogging.logger("GenerateExampleValues.kts")

    val allFetchedComments = mutableListOf<CommentsResponse.Comment>()

    var lastTimestamp = System.currentTimeMillis() / 1000
    var lastJsonOutput = ""
    var last10PercentBarrier = 0

    do {
        try {
            val nextComments = client.get(urlTemplate + lastTimestamp).body<CommentsResponse>().data

            val filteredComments = nextComments
                .filter { comment ->
                    comment.author.lowercase() !in filteredAuthors
                    && messageExclusionCriteriaWordParts.none {
                        when (it) {
                            is MessageExclusionCriteria.String -> it.value in comment.body.lowercase()
                            is MessageExclusionCriteria.Regex -> it.value.containsMatchIn(comment.body.lowercase())
                        }
                    }
                    && !comment.locked
                }
                .map { comment ->
                    var editedBody = comment.body

                    replacedParts.entries.forEach { (key, value) ->
                        editedBody = editedBody.replace(key, value)
                    }

                    comment.copy(body = editedBody.trim())
                }
                .filter { it.body.isNotBlank() }

            allFetchedComments.addAll(filteredComments)

            lastJsonOutput = json.encodeToString(allFetchedComments.map { it.body })

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