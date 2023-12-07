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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dean.jraw.RedditClient

private val logger = KotlinLogging.logger("MarkovBaj:BackendApi")

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
    @SerialName("messagesCollected")
    data class CommentsCollected(
        val comments: List<Message>,
    ) : ApiEvent {
        @Serializable
        data class Message(
            val id: String,
            val author: String,
            val content: String,
            val posted: Instant
        )
    }
}

fun setupBackendApiWebsocketServer(redditClient: RedditClient, json: Json, eventFlow: Flow<ApiEvent>) {
    embeddedServer(
        factory = CIO,
        host = "127.0.0.1",
        port = 14113,
        module = {
            install(WebSockets)

            routing {
                webSocket("/api/v1/oneshot") {
                    logger.info { "Got /api/v1/oneshot connection." }

                    try {
                        for (frame in incoming) {
                            try {
                                when (val request = json.decodeFromString<OneShotBackendApiRequest>((frame as Frame.Text).readText())) {
                                    is OneShotBackendApiRequest.PostRedditMessage -> {
                                        redditClient.comment(request.parentId).reply(request.content)
                                        logger.info { "Commented '${request.content}' on comment ${request.parentId}." }
                                        send(Json.encodeToString<OneShotBackendApiResponse>(OneShotBackendApiResponse.Success))
                                    }
                                }
                            } catch (e: Exception) {
                                logger.error(e) { "Error while handling one shot API request:" }
                                send(Json.encodeToString<OneShotBackendApiResponse>(OneShotBackendApiResponse.Error(error = e.stackTraceToString())))
                            }
                        }

                        close(CloseReason(CloseReason.Codes.NORMAL, "Message posted."))
                    } catch (e: Exception) {
                        logger.error(e) { "Error while handling one shot API request connection:" }
                        close(CloseReason(CloseReason.Codes.NOT_CONSISTENT, "Error while handling one shot API request connection."))
                    }

                    logger.info { "/api/v1/oneshot connection disconnected." }
                }

                webSocket("/api/v1/events") {
                    logger.info { "Got /api/v1/events connection." }

                    try {
                        eventFlow.collect {
                            try {
                                send(Frame.Text(Json.encodeToString(it)))
                            } catch (e: CancellationException) {
                                // This happens on every send even if it is successful for some reason, so just ignore it.
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Error while sending API event:" }
                        close(CloseReason(CloseReason.Codes.NOT_CONSISTENT, "Error while sending API event."))
                    }

                    logger.info { "/api/v1/events connection disconnected." }
                }
            }
        }
    ).start(wait = false)
}