
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dean.jraw.RedditClient

private val logger = KotlinLogging.logger("MarkovBaj:InternalApi")

@Serializable
private sealed interface OneShotBackendApiRequest {
    @Serializable
    @SerialName("postRedditMessage")
    data class PostRedditMessage(
        val parentId: String,
        val content: String
    ) : OneShotBackendApiRequest
}

@Serializable
private sealed interface OneShotBackendApiResponse {
    @Serializable
    @SerialName("success")
    data object Success : OneShotBackendApiResponse

    @Serializable
    @SerialName("error")
    data class Error(
        val error: String
    ) : OneShotBackendApiResponse
}

@Serializable
sealed interface ApiEvent {
    @Serializable
    @SerialName("commentsCollected")
    data class CommentsCollected(
        val comments: List<Comment>,
    ) : ApiEvent {
        @Serializable
        data class Comment(
            val created: Instant,
            val distinguished: String,
            val id: String,
            val fullName: String,
            val author: String,
            val body: String,
            val url: String?,
            val authorFlairText: String?,
            val submissionFullName: String,
            val submissionTitle: String?,
            val subredditType: String,
            val parentFullName: String,
            val subredditFullName: String,
        )
    }

    @Serializable
    @SerialName("submissionsCollected")
    data class SubmissionsCollected(
        val submissions: List<Submission>,
    ) : ApiEvent {
        @Serializable
        data class Submission(
            val created: Instant,
            val distinguished: String,
            val id: String,
            val author: String,
            val body: String?,
            val title: String,
            val url: String,
            val authorFlairText: String?,
            val domain: String,
            val embeddedMedia: Boolean,
            val isNsfw: Boolean,
            val isSelfPost: Boolean,
            val isSpoiler: Boolean,
            val linkFlairCssClass: String?,
            val linkFlairText: String?,
            val permalink: String,
            val postHint: String?,
            val preview: Boolean,
            val selfText: String?,
            val thumbnail: String?,
            val fullName: String,
            val subreddit: String,
            val subredditFullName: String,
        )
    }
}

fun setupBackendApiWebsocketServer(redditClient: RedditClient?, json: Json, eventFlow: Flow<ApiEvent>) {
    embeddedServer(
        factory = CIO,
        host = "127.0.0.1",
        port = 14113,
        module = {
            install(WebSockets)

            routing {
                webSocket("/api/v1/oneshot") {
                    backend.logger.info { "Got /api/v1/oneshot connection." }

                    try {
                        for (frame in incoming) {
                            try {
                                when (val request = json.decodeFromString<OneShotBackendApiRequest>((frame as Frame.Text).readText())) {
                                    is OneShotBackendApiRequest.PostRedditMessage -> {
                                        if (redditClient != null) {
                                            backend.logger.info { "Commented '${request.content}' on comment ${request.parentId}." }
                                            send(Json.encodeToString<OneShotBackendApiResponse>(OneShotBackendApiResponse.Success))
                                        } else {
                                            backend.logger.warn { "Unable to comment as Reddit bot is not set up." }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                backend.logger.error(e) { "Error while handling one shot API request:" }
                                send(Json.encodeToString<OneShotBackendApiResponse>(OneShotBackendApiResponse.Error(error = e.stackTraceToString())))
                            }
                        }

                        close(CloseReason(CloseReason.Codes.NORMAL, "Message posted."))
                    } catch (e: Exception) {
                        backend.logger.error(e) { "Error while handling one shot API request connection:" }
                        close(CloseReason(CloseReason.Codes.NOT_CONSISTENT, "Error while handling one shot API request connection."))
                    }

                    backend.logger.info { "/api/v1/oneshot connection disconnected." }
                }

                webSocket("/api/v1/events") {
                    backend.logger.info { "Got /api/v1/events connection." }

                    try {
                        eventFlow.collect {
                            try {
                                send(Frame.Text(Json.encodeToString(it)))
                            } catch (e: CancellationException) {
                                // This happens on every send even if it is successful for some reason, so just ignore it.
                            }
                        }
                    } catch (e: Exception) {
                        backend.logger.error(e) { "Error while sending API event:" }
                        close(CloseReason(CloseReason.Codes.NOT_CONSISTENT, "Error while sending API event."))
                    }

                    backend.logger.info { "/api/v1/events connection disconnected." }
                }
            }
        }
    ).start(wait = false)
}