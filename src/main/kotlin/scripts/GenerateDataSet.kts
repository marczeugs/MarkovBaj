import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

@Serializable
data class CommentsResponse(
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


val filteredAuthors = listOf(
    Constants.redditUserName.lowercase(),
    "[deleted]",
)

val replacedParts = mapOf(
    Regex("(?:^|\\s)*${Constants.triggerKeyword}(?:\$|\\s)*", RegexOption.IGNORE_CASE) to "", // Bot mentions
    Regex("(?:^|\\s)?:\\d+:(?:\$|\\s)?", RegexOption.IGNORE_CASE) to "", // Emotes
    Regex("&amp;#x200B;", RegexOption.IGNORE_CASE) to "\u200b", // Weird stuff with zero width spaces
    Regex("!?\\[(?:img|gif)]\\(.+\\)", RegexOption.IGNORE_CASE) to "", // Remove emotes and images
    Regex("\\n", RegexOption.IGNORE_CASE) to "", // Remove line breaks, handling them is just a pain
)

val messageExclusionCriteriaWordParts = listOf(
    "igg",
    "ike",
    "trann",
    "14/88",
    "1488",
    "kfc",
    "watermelon",
)

val json = Json {
    ignoreUnknownKeys = true
}

val client = HttpClient(CIO) {
    install(Logging)

    install(ContentNegotiation) {
        json(json)
    }
}

val urlTemplate = "https://api.pushshift.io/reddit/search/comment/?subreddit=forsen&size=500&before="

val maxJsonSize = 20 * 1024 * 1024 // 20 MiB


var lastTimestamp = System.currentTimeMillis() / 1000
var allFetchedComments = mutableListOf<CommentsResponse.Comment>()
var lastJsonOutput = ""
var last10PercentBarrier = 0


fun save(fileName: String, jsonOutput: String) {
    File(fileName).writeText(jsonOutput)
}


runBlocking {
    do {
        try {
            val nextComments = client.get(urlTemplate + lastTimestamp).body<CommentsResponse>().data

            val filteredComments = nextComments.filter { comment ->
                    comment.author.lowercase() !in filteredAuthors
                    && messageExclusionCriteriaWordParts.none { it in comment.body.lowercase() }
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

            println(
                "Fetched ${filteredComments.size} comments before ${
                    DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(lastTimestamp))
                }, total comments: ${allFetchedComments.size}, JSON string size: ${lastJsonOutput.length} / $maxJsonSize (${
                    lastJsonOutput.length.toFloat() / maxJsonSize * 100
                }%)"
            )

            lastTimestamp = nextComments.last().createdUtc
        } catch (e: Exception) {
            println("Unable to fetch comments before ${DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(lastTimestamp))}, retrying...")
            save("data-error.json", lastJsonOutput)
        }

        if ((lastJsonOutput.length.toFloat() / maxJsonSize * 100).toInt() / 10 != last10PercentBarrier) {
            last10PercentBarrier = (lastJsonOutput.length.toFloat() / maxJsonSize * 100).toInt() / 10
            save("data-${last10PercentBarrier * 10}.json", lastJsonOutput)
            println("Reached ${last10PercentBarrier * 10}%, saving backup...")
        }

        delay(1.seconds)
    } while (lastJsonOutput.length < maxJsonSize)

    save("data.json", lastJsonOutput)
    println("Fetched ${allFetchedComments.size} comments in total.")
}