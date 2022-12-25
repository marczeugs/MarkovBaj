import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.PublicContributionReference
import java.io.File
import kotlin.concurrent.fixedRateTimer

val logger = KotlinLogging.logger("MarkovBaj")

fun main() {
    val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    logger.info("Starting backend services...")

    val markovChain = json.decodeFromString<MarkovWordChain>(File("data.json").readText())

    logger.info("Loaded Markov chain from file.")

    val redditBotCredentials = Credentials.script(
        username = RuntimeVariables.botRedditUsername,
        password = RuntimeVariables.botRedditPassword,
        clientId = RuntimeVariables.botRedditClientId,
        clientSecret = RuntimeVariables.botRedditClientSecret
    )

    val userAgent = UserAgent(
        platform = "JVM/JRAW",
        appId = "MarkovBaj",
        version = BuildInfo.version,
        redditUsername = "the_marcster"
    )

    val redditClient = OAuthHelper.automatic(OkHttpNetworkAdapter(userAgent), redditBotCredentials).apply {
        logHttp = false
    }

    logger.info("Connected to Reddit.")

    setupJanitorBackendServer(redditClient, json)

    val activeSubreddit = redditClient.subreddit(BotConstants.activeSubreddit)

    var alreadyProcessedPostIds = listOf<String>()
    var alreadyProcessedCommentsIds = listOf<String>()

    val botCoroutineScope = CoroutineScope(Dispatchers.Default)

    logger.info("Bot running.")

    fixedRateTimer(period = BotConstants.checkInterval.inWholeMilliseconds) {
        botCoroutineScope.launch {
            try {
                val newInboxMessages = redditClient.me()
                    .inbox()
                    .iterate("unread")
                    .build()
                    .accumulateMerged(1)
                    .filter {
                        it.subject == "username mention" ||
                        it.subject.startsWith("comment reply") && BotConstants.triggerKeyword.lowercase() in it.body.lowercase() && it.subreddit != BotConstants.activeSubreddit
                    }

                val newPosts = activeSubreddit.posts()
                    .sorting(SubredditSort.NEW)
                    .limit(10)
                    .build()
                    .accumulateMerged(1)
                    .filter { it.created.toInstant().toKotlinInstant() > Clock.System.now() - BotConstants.checkInterval * 2 && it.id !in alreadyProcessedPostIds }

                val newComments = activeSubreddit.comments()
                    .limit(100)
                    .build()
                    .accumulateMerged(1)
                    .filter {
                        it.created.toInstant().toKotlinInstant() > Clock.System.now() - BotConstants.checkInterval * 2 &&
                        it.id !in alreadyProcessedCommentsIds &&
                        it.id !in newInboxMessages.filter { message -> message.subreddit == BotConstants.activeSubreddit }.map { message -> message.id }
                    }
                
                logger.info("${newInboxMessages.size} new mention(s), ${newPosts.size} new post(s), ${newComments.size} new comment(s).")

                alreadyProcessedPostIds = newPosts.map { it.id }
                alreadyProcessedCommentsIds = newInboxMessages.map { it.id }.union(newComments.map { it.id }).toList()

                var commentCounter = 0

                for (message in newInboxMessages) {
                    if (commentCounter >= BotConstants.maxCommentsPerCheck) {
                        logger.warn("Hit comment limit, not posting any more replies.")
                        return@launch
                    }

                    if (!message.isComment) {
                        logger.warn("Username mention with id ${message.id} was not a comment, skipping...")
                        return@launch
                    }

                    val wordsInTitle = message.body.split(BotConstants.wordSeparatorRegex).map { Word(it) }

                    val relatedReply = if (Math.random() > BotConstants.unrelatedAnswerChance) {
                        tryGeneratingReplyFromWords(markovChain, wordsInTitle)
                    } else {
                        null
                    }

                    val actualReply = if (relatedReply != null) {
                        logger.info("Replying to mention by ${message.author} in message ${message.id} in ${message.subreddit?.let { "r/$it" } ?: "-"} ('${message.body}') with related answer...")
                        relatedReply
                    } else {
                        markovChain.generateSequence().joinToString(" ") { it.word }.also {
                            logger.info("Default replying to mention by ${message.author} in message ${message.id} in ${message.subreddit?.let { "r/$it" } ?: "-"} ('${message.body}')...")
                        }
                    }

                    redditClient.comment(message.id).safeReply(actualReply)
                    redditClient.me().inbox().markRead(true, message.fullName)
                    commentCounter++

                    delay(BotConstants.delayBetweenComments)
                }

                for (post in newPosts) {
                    if (commentCounter >= BotConstants.maxCommentsPerCheck) {
                        logger.warn("Hit comment limit, not posting any more replies.")
                        return@launch
                    }

                    if (post.isRemoved) {
                        continue
                    }

                    if (BotConstants.triggerKeyword.lowercase() in post.title.lowercase()) {
                        val wordsInTitle = post.title.split(BotConstants.wordSeparatorRegex).map { Word(it) }

                        val relatedReply = if (Math.random() > BotConstants.unrelatedAnswerChance) {
                            tryGeneratingReplyFromWords(markovChain, wordsInTitle)
                        } else {
                            null
                        }

                        val actualReply = if (relatedReply != null) {
                            logger.info("Replying to post ${post.id} ('${post.title}') with related answer...")
                            relatedReply
                        } else {
                            markovChain.generateSequence().joinToString(" ") { it.word }.also {
                                logger.info("Default replied to post ${post.id} ('${post.title}')...")
                            }
                        }

                        post.toReference(redditClient).safeReply(actualReply)
                        commentCounter++

                        delay(BotConstants.delayBetweenComments)
                    }
                }

                for (comment in newComments) {
                    if (commentCounter >= BotConstants.maxCommentsPerCheck) {
                        logger.warn("Hit comment limit, not posting any more replies.")
                        return@launch
                    }

                    if (comment.id in newInboxMessages.map { it.id }) {
                        continue
                    }

                    if (BotConstants.triggerKeyword.lowercase() in comment.body.lowercase()) {
                        val wordsInComment = comment.body.split(BotConstants.wordSeparatorRegex).map { Word(it) }

                        val relatedReply = if (Math.random() > BotConstants.unrelatedAnswerChance) {
                            tryGeneratingReplyFromWords(markovChain, wordsInComment)
                        } else {
                            null
                        }

                        val actualReply = if (relatedReply != null) {
                            logger.info("Replying to comment ${comment.id} ('${comment.body}') with related answer...")
                            relatedReply
                        } else {
                            markovChain.generateSequence().joinToString(" ") { it.word }.also {
                                logger.info("Default replying to comment ${comment.id} ('${comment.body}')...")
                            }
                        }

                        comment.toReference(redditClient).safeReply(actualReply)
                        commentCounter++

                        delay(BotConstants.delayBetweenComments)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error while running timer loop:", e)
            }
        }
    }
}

private fun tryGeneratingReplyFromWords(markovChain: MarkovWordChain, words: List<Word>): String? {
    words.windowed(markovChain.consideredValuesForGeneration).shuffled().forEach { potentialChainStart ->
        if (markovChain.chainStarts.weightMap.keys.any { words -> words == potentialChainStart.map { markovChain.hashTransformation(it) } }) {
            return markovChain.generateSequence(start = potentialChainStart).joinToString(" ") { it.word }.take(5000)
        }
    }

    logger.info("Unable to generate a response, sending default...")
    return null
}

private fun PublicContributionReference.safeReply(text: String) {
    if (RuntimeVariables.botActuallySendReplies) {
        try {
            reply(text)
            logger.info("Replied with '$text'.")
        } catch (e: Exception) {
            logger.error("Reply failed:", e)
        }
    } else {
        logger.info("[NOT ACTUALLY REPLYING] Would have replied with '$text'.")
    }
}