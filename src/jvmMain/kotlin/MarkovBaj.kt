
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.PublicContributionReference
import java.io.File
import kotlin.concurrent.fixedRateTimer
import kotlin.time.DurationUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger("MarkovBaj:Reddit")
private val generalLogger = KotlinLogging.logger("MarkovBaj:General")

fun main() {
    val botCoroutineScope = CoroutineScope(Dispatchers.Default)

    generalLogger.info { "Starting MarkovBaj Backend version ${BuildInfo.PROJECT_VERSION}, Build ${Instant.fromEpochMilliseconds(BuildInfo.PROJECT_BUILD_TIMESTAMP_MILLIS)}..." }


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

    val redditBotCredentials = Credentials.script(
        username = RuntimeVariables.Reddit.botUsername,
        password = RuntimeVariables.Reddit.botPassword,
        clientId = RuntimeVariables.Reddit.botClientId,
        clientSecret = RuntimeVariables.Reddit.botClientSecret
    )

    val userAgent = UserAgent(
        platform = "JVM/JRAW",
        appId = RuntimeVariables.Reddit.botAppId,
        version = BuildInfo.PROJECT_VERSION,
        redditUsername = RuntimeVariables.Reddit.botAuthorRedditUsername
    )

    val redditClient = OAuthHelper.automatic(OkHttpNetworkAdapter(userAgent), redditBotCredentials).apply {
        logHttp = false
    }

    logger.info("Connected to Reddit.")


    setupBackendServer(redditClient, json, markovChain)


    botCoroutineScope.launch {
        setupDiscordBot(markovChain)
    }

    botCoroutineScope.launch(Dispatchers.IO) {
        setupTwitchBot(markovChain)
    }


    val activeSubreddit = redditClient.subreddit(RuntimeVariables.Reddit.activeSubreddit)

    var alreadyProcessedPostIds = listOf<String>()
    var alreadyProcessedCommentsIds = listOf<String>()

    logger.info("Bot running.")

    fixedRateTimer(period = RuntimeVariables.Reddit.checkInterval.inWholeMilliseconds) {
        botCoroutineScope.launch {
            try {
                val newInboxMessages = redditClient.me()
                    .inbox()
                    .iterate("unread")
                    .build()
                    .accumulateMerged(1)
                    .filter {
                        it.subject == "username mention" ||
                        it.subject.startsWith("comment reply") && CommonConstants.triggerKeyword.lowercase() in it.body.lowercase() && it.subreddit != RuntimeVariables.Reddit.activeSubreddit
                    }

                val newPosts = activeSubreddit.posts()
                    .sorting(SubredditSort.NEW)
                    .limit(10)
                    .build()
                    .accumulateMerged(1)
                    .filter { it.created.toInstant().toKotlinInstant() > Clock.System.now() - RuntimeVariables.Reddit.checkInterval * 2 && it.id !in alreadyProcessedPostIds }

                val newComments = activeSubreddit.comments()
                    .limit(100)
                    .build()
                    .accumulateMerged(1)
                    .filter {
                        it.created.toInstant().toKotlinInstant() > Clock.System.now() - RuntimeVariables.Reddit.checkInterval * 2 &&
                        it.id !in alreadyProcessedCommentsIds &&
                        it.id !in newInboxMessages.filter { message -> message.subreddit == RuntimeVariables.Reddit.activeSubreddit }.map { message -> message.id }
                    }
                
                logger.info("${newInboxMessages.size} new mention(s), ${newPosts.size} new post(s), ${newComments.size} new comment(s).")

                alreadyProcessedPostIds = newPosts.map { it.id }
                alreadyProcessedCommentsIds = newInboxMessages.map { it.id }.union(newComments.map { it.id }).toList()

                var commentCounter = 0

                if (RuntimeVariables.Reddit.answerMentions) {
                    for (message in newInboxMessages) {
                        if (commentCounter >= RuntimeVariables.Reddit.maxCommentsPerCheck) {
                            logger.warn("Hit comment limit, not posting any more replies.")
                            return@launch
                        }

                        if (!message.isComment) {
                            logger.warn("Username mention with id ${message.id} was not a comment, skipping...")
                            return@launch
                        }

                        val wordsInTitle = message.body.toWordParts()

                        val relatedReply = if (Math.random() > RuntimeVariables.unrelatedAnswerChance) {
                            markovChain.tryGeneratingReplyFromWords(wordsInTitle, platform = "Reddit")
                        } else {
                            null
                        }

                        val actualReply = if (relatedReply != null) {
                            logger.info("Replying to mention by ${message.author} in message ${message.id} in ${message.subreddit?.let { "r/$it" } ?: "-"} ('${message.body}') with related answer...")
                            relatedReply
                        } else {
                            markovChain.generateRandomReply().also {
                                logger.info("Default replying to mention by ${message.author} in message ${message.id} in ${message.subreddit?.let { "r/$it" } ?: "-"} ('${message.body}')...")
                            }
                        }

                        redditClient.comment(message.id).safeReply(actualReply)
                        redditClient.me().inbox().markRead(true, message.fullName)
                        commentCounter++

                        delay(RuntimeVariables.Reddit.delayBetweenComments)
                    }
                }

                for (post in newPosts) {
                    if (commentCounter >= RuntimeVariables.Reddit.maxCommentsPerCheck) {
                        logger.warn("Hit comment limit, not posting any more replies.")
                        return@launch
                    }

                    if (post.isRemoved) {
                        continue
                    }

                    if (CommonConstants.triggerKeyword.lowercase() in post.title.lowercase()) {
                        val wordsInTitle = post.title.toWordParts()

                        val relatedReply = if (Math.random() > RuntimeVariables.unrelatedAnswerChance) {
                            markovChain.tryGeneratingReplyFromWords(wordsInTitle, platform = "Reddit")
                        } else {
                            null
                        }

                        val actualReply = if (relatedReply != null) {
                            logger.info("Replying to post ${post.id} ('${post.title}') with related answer...")
                            relatedReply
                        } else {
                            markovChain.generateRandomReply().also {
                                logger.info("Default replied to post ${post.id} ('${post.title}')...")
                            }
                        }

                        post.toReference(redditClient).safeReply(actualReply)
                        commentCounter++

                        delay(RuntimeVariables.Reddit.delayBetweenComments)
                    }
                }

                for (comment in newComments) {
                    if (commentCounter >= RuntimeVariables.Reddit.maxCommentsPerCheck) {
                        logger.warn("Hit comment limit, not posting any more replies.")
                        return@launch
                    }

                    if (comment.id in newInboxMessages.map { it.id }) {
                        continue
                    }

                    if (CommonConstants.triggerKeyword.lowercase() in comment.body.lowercase()) {
                        val wordsInComment = comment.body.toWordParts()

                        val relatedReply = if (Math.random() > RuntimeVariables.unrelatedAnswerChance) {
                            markovChain.tryGeneratingReplyFromWords(wordsInComment, platform = "Reddit")
                        } else {
                            null
                        }

                        val actualReply = if (relatedReply != null) {
                            logger.info("Replying to comment ${comment.id} ('${comment.body}') with related answer...")
                            relatedReply
                        } else {
                            markovChain.generateRandomReply().also {
                                logger.info("Default replying to comment ${comment.id} ('${comment.body}')...")
                            }
                        }

                        comment.toReference(redditClient).safeReply(actualReply)
                        commentCounter++

                        delay(RuntimeVariables.Reddit.delayBetweenComments)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error while running timer loop:", e)
            }
        }
    }
}

private fun PublicContributionReference.safeReply(text: String) {
    if (RuntimeVariables.Reddit.actuallySendReplies) {
        try {
            reply(text.take(5000))
            logger.info("Replied with '${text.take(5000)}'.")
        } catch (e: Exception) {
            logger.error("Reply failed:", e)
        }
    } else {
        logger.info("[NOT ACTUALLY REPLYING] Would have replied with '$text'.")
    }
}