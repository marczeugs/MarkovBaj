
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.TextDecorationLine
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.Comment
import net.dean.jraw.models.Listing
import net.dean.jraw.models.OAuthData
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.NoopTokenStore
import org.slf4j.event.Level
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Serializable
data class Session(
    val redditAccessToken: String,
    val redditRefreshToken: String?,
    @Serializable(with = InstantSerializer::class) val redditAccessTokenExpiration: Instant,
)

private object Routes {
    @Resource("/")
    @Serializable
    class Index

    @Resource("/login")
    @Serializable
    class Login

    @Resource("/callback")
    @Serializable
    class Callback

    @Resource("/manage")
    @Serializable
    class Manage

    @Resource("/deletecomment")
    @Serializable
    data class DeleteComment(@SerialName("comment_link") val commentLink: String)

    @Resource("/styles.css")
    @Serializable
    class StylesCss

    @Resource("/api/v1")
    @Serializable
    class Api {
        @Resource("query")
        @Serializable
        data class Query(val api: Api = Api(), val input: String? = null)
    }
}

private val logger = KotlinLogging.logger("MarkovBaj:Backend")

private val redditLoginRedirectUrl = "${RuntimeVariables.backendServerUrl}:${RuntimeVariables.backendServerPort}/callback"

fun setupBackendServer(markovRedditClient: RedditClient, json: Json, markovChain: MarkovChain<String>) {
    embeddedServer(
        CIO,
        port = RuntimeVariables.backendServerPort,
        host = "0.0.0.0",
        module = {
            myApplicationModule(markovRedditClient, json, markovChain)
        }
    ).start(wait = false)
}

private suspend fun ApplicationCall.respondReturnToLogin() {
    respondHtml {
        head {
            title("Invalid Reddit Login")
            styleLink("/styles.css")
        }

        body {
            p {
                +"Invalid Reddit login."
            }

            br { }

            a(href = "/") {
                +"Return to login"
            }
        }
    }
}

private fun setupRedditClient(session: Session, validateUser: Boolean = true): RedditClient? {
    val userAgent = UserAgent(
        platform = "JVM/JRAW",
        appId = "${RuntimeVariables.botAppId} Comment Sanitation and Waste Management Engineer Duties",
        version = BuildInfo.PROJECT_VERSION,
        redditUsername = RuntimeVariables.botAuthorRedditUsername
    )

    val redditClient = RedditClient::class.constructors.first().call(
        OkHttpNetworkAdapter(userAgent),
        OAuthData.create(session.redditAccessToken, listOf("identity"), session.redditRefreshToken, Date.from(session.redditAccessTokenExpiration.toJavaInstant())),
        Credentials.webapp(
            clientId = RuntimeVariables.backendRedditClientId,
            clientSecret = RuntimeVariables.backendRedditClientSecret,
            redirectUrl = redditLoginRedirectUrl
        ),
        NoopTokenStore(),
        null
    ).apply {
        logHttp = false
    }

    return if (redditClient.me().username.lowercase() in RuntimeVariables.backendPermittedUsers || !validateUser) {
        redditClient
    } else {
        null
    }
}

fun Application.myApplicationModule(markovRedditClient: RedditClient, json: Json, markovChain: MarkovChain<String>) {
    install(CallLogging) {
        level = Level.TRACE
    }

    install(StatusPages) {
        exception<Exception> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                "Internal server error:\n${StringWriter().also { cause.printStackTrace(PrintWriter(it)) }}"
            )
        }
    }

    install(Sessions) {
        cookie<Session>("reddit_login") {
            serializer = object : SessionSerializer<Session> {
                override fun deserialize(text: String) = json.decodeFromString<Session>(text)
                override fun serialize(session: Session) = json.encodeToString(session)
            }
        }
    }

    install(CORS) {
        anyHost()
        allowHeaders { true }
        allowMethod(HttpMethod.Get)
    }

    install(Resources)

    val redditOAuthName = "reddit-oauth"

    authentication {
        oauth(redditOAuthName) {
            urlProvider = { redditLoginRedirectUrl }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "reddit",
                    authorizeUrl = "https://www.reddit.com/api/v1/authorize",
                    accessTokenUrl = "https://www.reddit.com/api/v1/access_token",
                    accessTokenRequiresBasicAuth = true,
                    requestMethod = HttpMethod.Post,
                    clientId = RuntimeVariables.backendRedditClientId,
                    clientSecret = RuntimeVariables.backendRedditClientSecret,
                    defaultScopes = listOf("identity")
                )
            }
            client = HttpClient(io.ktor.client.engine.cio.CIO)
        }
    }

    routing {
        get<Routes.Index> {
            call.respondHtml {
                head {
                    title("MarkovBaj Janitor Backend Login")
                    styleLink("/styles.css")
                }

                body {
                    button {
                        onClick = "location.href = '/login'"

                        +"Login"
                    }

                    footer {
                        +"Version ${BuildInfo.PROJECT_VERSION}, Build ${Instant.fromEpochMilliseconds(BuildInfo.PROJECT_BUILD_TIMESTAMP_MILLIS)}"
                    }
                }
            }
        }

        authenticate(redditOAuthName) {
            get<Routes.Login> {
                // Redirects to `authorizeUrl` automatically
            }

            get<Routes.Callback> {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>() ?: run {
                    call.respondReturnToLogin()
                    return@get
                }

                val newSession = Session(
                    redditAccessToken = principal.accessToken,
                    redditRefreshToken = principal.refreshToken,
                    redditAccessTokenExpiration = Clock.System.now() + principal.expiresIn.seconds
                )

                val userRedditClientName = setupRedditClient(newSession, validateUser = false)!!.me().username

                if (userRedditClientName.lowercase() in RuntimeVariables.backendPermittedUsers) {
                    logger.info { "User '$userRedditClientName' has logged into the janitor backend." }
                    call.sessions.set(newSession)
                } else {
                    logger.info { "User '$userRedditClientName' has tried to log into the janitor backend, but isn't permitted to do so." }
                }

                call.respondRedirect("/manage")
            }
        }

        get<Routes.Manage> {
            val session = call.sessions.get<Session>() ?: run {
                call.respondReturnToLogin()
                return@get
            }

            val userRedditClient = setupRedditClient(session) ?: run {
                call.respondReturnToLogin()
                return@get
            }

            call.respondHtml {
                head {
                    title("MarkovBaj Janitor Backend")
                    styleLink("/styles.css")
                }

                body {
                    img {
                        src = "https://styles.redditmedia.com/t5_4wpxrc/styles/profileIcon_1ypmzxwn0hn71.png?width=256&height=256&crop=256:256,smart&s=da81d12487728dfa78e33f9ae2af8a5df87ee317"
                    }

                    p {
                        +"Hello /u/${userRedditClient.me().username}, welcome to the "
                        span(classes = "strikethrough") { +"Janitor Room" }
                        +" MarkovBaj Comment Sanitation and Waste Management Engineer Duties Centre."
                    }

                    br { }

                    form(action = call.application.href(Routes.DeleteComment("")), method = FormMethod.get) {
                        h1 {
                            +"Comment deleter"
                        }

                        p {
                            +"Only comments with a maximum age of 24 hours can be deleted. Only delete TOS comments. Deletions will be logged. Link can be obtained with Share -> Copy Link."
                        }

                        input {
                            type = InputType.text
                            name = "comment_link"
                            placeholder = "Direct comment perma link (e.g. https://www.reddit.com/r/forsen/comments/pgamez/mods_update/hbbiwe7/)"
                        }

                        input {
                            type = InputType.submit
                            value = "Delete"
                        }
                    }
                }
            }
        }

        get<Routes.DeleteComment> { deleteCommentRequest ->
            val session = call.sessions.get<Session>() ?: run {
                call.respondReturnToLogin()
                return@get
            }

            val userRedditClientName = setupRedditClient(session)?.me()?.username ?: run {
                call.respondReturnToLogin()
                return@get
            }

            try {
                val pathSegments = Url(deleteCommentRequest.commentLink).pathSegments

                val commentToDelete = @Suppress("UNCHECKED_CAST") (markovRedditClient.lookup("t1_${pathSegments[6]}") as Listing<Comment>)
                    .children
                    .first()

                if ((Clock.System.now() - commentToDelete.created.toInstant().toKotlinInstant() - 1.days).isPositive()) {
                    call.respondText("Comment is too old to be deleted.", status = HttpStatusCode.BadRequest)
                    return@get
                }

                if (commentToDelete.author.lowercase() != RuntimeVariables.botRedditUsername.lowercase()) {
                    call.respondText("Comment was not posted by authenticated account.", status = HttpStatusCode.BadRequest)
                    return@get
                }

                markovRedditClient.comment(pathSegments[6]).delete()
                logger.info { "Comment '${pathSegments[6]}' at '${deleteCommentRequest.commentLink}' with the content '${commentToDelete.body}' was deleted by user '${userRedditClientName}'." }
                call.respondText("Comment was deleted.")
            } catch (e: Exception) {
                call.respondText("Invalid comment URL.", status = HttpStatusCode.BadRequest)
                logger.warn { "Comment deletion by '$userRedditClientName' failed: $e" }
                return@get
            }
        }

        get<Routes.StylesCss> {
            call.respondText(
                text = CssBuilder().apply {
                    rule("body") {
                        width = 800.px
                        paddingTop = 50.px
                        margin(LinearDimension.auto)
                        fontFamily = "Arial"
                    }

                    rule("body *") {
                        width = 100.pct
                        boxSizing = BoxSizing.borderBox
                    }

                    rule("footer") {
                        position = Position.fixed
                        bottom = 0.px
                        padding(16.px)
                    }

                    rule("button") {
                        height = 48.px
                    }

                    rule("input[type=\"text\"], input[type=\"submit\"]") {
                        marginTop = 8.px
                        padding(8.px)
                    }

                    rule("img") {
                        width = 256.px
                        display = Display.block
                        margin(LinearDimension.auto)
                    }

                    rule(".strikethrough") {
                        textDecoration = TextDecoration(setOf(TextDecorationLine.lineThrough))
                    }
                }.toString(),
                contentType = ContentType.Text.CSS
            )
        }

        get<Routes.Api.Query> { queryInput ->
            val query = queryInput.input

            if (query != null && query.length > 500) {
                logger.warn { "Rejected Markov chain query request from ${context.request.header("X-Real-Ip")}, input has length ${queryInput.input.length}, starts with: \"${queryInput.input.take(200)}\"" }
                call.respondText("Input too long.", status = HttpStatusCode.BadRequest)
                return@get
            }

            logger.info { "Serving Markov chain query request from ${context.request.header("X-Real-Ip")}, input has length ${queryInput.input?.length ?: 0}, starts with: \"${queryInput.input?.take(200)}\"" }

            val response = if (Math.random() > BotConstants.unrelatedAnswerChance) {
                query?.split(CommonConstants.wordSeparatorRegex)?.windowed(CommonConstants.consideredValuesForGeneration)?.shuffled()?.firstNotNullOfOrNull { potentialChainStart ->
                    if (markovChain.chainStarts.weightMap.keys.any { words -> words.map { it.lowercase() } == potentialChainStart.map { it.lowercase() } }) {
                        markovChain.generateSequence(start = potentialChainStart).joinToString(" ").take(5000)
                    } else {
                        null
                    }
                }
            } else {
                null
            } ?: run {
                markovChain.generateSequence().joinToString(" ").take(5000)
            }

            call.respondText(response)
        }
    }
}