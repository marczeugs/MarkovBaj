package backend

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.TextDecorationLine
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.html.*
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.Comment
import net.dean.jraw.models.Listing
import net.dean.jraw.models.OAuthData
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.NoopTokenStore
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

private suspend fun ApplicationCall.respondReturnToLogin() {
    respondHtml {
        head {
            title("Invalid Reddit Login")
            styleLink(application.run { href(Routes.JanitorBackend.StylesCss()) })
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
        appId = "${RuntimeVariables.Reddit.botAppId} Comment Sanitation and Waste Management Engineer Duties",
        version = BuildInfo.PROJECT_VERSION,
        redditUsername = RuntimeVariables.Reddit.botAuthorRedditUsername
    )

    val redditClient = RedditClient::class.constructors.first().call(
        OkHttpNetworkAdapter(userAgent),
        OAuthData.create(session.redditAccessToken, listOf("identity"), session.redditRefreshToken, Date.from(session.redditAccessTokenExpiration.toJavaInstant())),
        Credentials.webapp(
            clientId = RuntimeVariables.Backend.redditClientId,
            clientSecret = RuntimeVariables.Backend.redditClientSecret,
            redirectUrl = redditLoginRedirectUrl
        ),
        NoopTokenStore(),
        null
    ).apply {
        logHttp = false
    }

    return if (redditClient.me().username.lowercase() in RuntimeVariables.Backend.permittedUsers || !validateUser) {
        redditClient
    } else {
        null
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.janitorBackendLogin() {
    call.respondHtml {
        head {
            title("MarkovBaj Janitor Backend Login")
            styleLink(application.run { href(Routes.JanitorBackend.StylesCss()) })
        }

        body {
            button {
                onClick = "location.href = '${application.run { href(Routes.JanitorBackend.Login()) }}'"

                +"Login"
            }

            footer {
                +"Version ${BuildInfo.PROJECT_VERSION}, Build ${Instant.fromEpochMilliseconds(BuildInfo.PROJECT_BUILD_TIMESTAMP_MILLIS)}"
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.janitorBackendCallback() {
    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>() ?: run {
        call.respondReturnToLogin()
        return
    }

    val newSession = Session(
        redditAccessToken = principal.accessToken,
        redditRefreshToken = principal.refreshToken,
        redditAccessTokenExpiration = Clock.System.now() + principal.expiresIn.seconds
    )

    val userRedditClientName = setupRedditClient(newSession, validateUser = false)!!.me().username

    if (userRedditClientName.lowercase() in RuntimeVariables.Backend.permittedUsers) {
        logger.info { "User '$userRedditClientName' has logged into the janitor backend." }
        call.sessions.set(newSession)
    } else {
        logger.info { "User '$userRedditClientName' has tried to log into the janitor backend, but isn't permitted to do so." }
    }

    call.respondRedirect(application.run { href(Routes.JanitorBackend.Manage()) })
}

suspend fun PipelineContext<Unit, ApplicationCall>.janitorBackendManage() {
    val session = call.sessions.get<Session>() ?: run {
        call.respondReturnToLogin()
        return
    }

    val userRedditClient = setupRedditClient(session) ?: run {
        call.respondReturnToLogin()
        return
    }

    call.respondHtml {
        head {
            title("MarkovBaj Janitor Backend")
            styleLink(application.run { href(Routes.JanitorBackend.StylesCss()) })
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

            form(action = call.application.href(Routes.JanitorBackend.DeleteComment(commentLink = "")), method = FormMethod.get) {
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

suspend fun PipelineContext<Unit, ApplicationCall>.janitorBackendDeleteComment(redditClient: RedditClient?, deleteCommentRequest: Routes.JanitorBackend.DeleteComment) {
    val session = call.sessions.get<Session>() ?: run {
        call.respondReturnToLogin()
        return
    }

    val userRedditClientName = setupRedditClient(session)?.me()?.username ?: run {
        call.respondReturnToLogin()
        return
    }

    if (redditClient == null) {
        call.respondText("Unable to delete comment because Reddit bot is not set up.")
        return
    }

    try {
        val pathSegments = Url(deleteCommentRequest.commentLink).pathSegments

        val commentToDelete = @Suppress("UNCHECKED_CAST") (redditClient.lookup("t1_${pathSegments[6]}") as Listing<Comment>)
            .children
            .first()

        if ((Clock.System.now() - commentToDelete.created.toInstant().toKotlinInstant() - 1.days).isPositive()) {
            call.respondText("Comment is too old to be deleted.", status = HttpStatusCode.BadRequest)
            return
        }

        if (commentToDelete.author.lowercase() != RuntimeVariables.Reddit.botUsername.lowercase()) {
            call.respondText("Comment was not posted by authenticated account.", status = HttpStatusCode.BadRequest)
            return
        }

        redditClient.comment(pathSegments[6]).delete()
        logger.info { "Comment '${pathSegments[6]}' at '${deleteCommentRequest.commentLink}' with the content '${commentToDelete.body}' was deleted by user '${userRedditClientName}'." }
        call.respondText("Comment was deleted.")
    } catch (e: Exception) {
        call.respondText("Invalid comment URL.", status = HttpStatusCode.BadRequest)
        logger.warn { "Comment deletion by '$userRedditClientName' failed: $e" }
        return
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.janitorBackendStyles() {
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