package scripts

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Comment(
    val id: String,
    val author: String,
    val content: String,
    val posted: Instant
)

fun main() {
    File("output.json").writeText(
        Json.encodeToString(
            File("comments").listFiles()!!
                .flatMap { file ->
                    try {
                        Json.decodeFromString<List<Comment>>(file.readText()).map { it.author to it.content }
                    } catch (_: Exception) {
                        Json.decodeFromString<List<String>>(file.readText()).map { "" to it }
                    }
                        .shuffled()
                        .take(30000)
                }
                .mapNotNull { (author, content) -> sanitizeComment(author, content) }
        )
    )
}