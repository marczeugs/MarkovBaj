import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.PublicContributionReference
import java.time.Instant
import kotlin.concurrent.fixedRateTimer
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

val logger = KotlinLogging.logger("MarkovBaj")

object MarkovBaj {
    @JvmStatic
    fun main(args: Array<String>) {
        runBot()
    }
}

fun runBot() {
    val json = Json {
        ignoreUnknownKeys = true
    }

    val markovChain = MarkovChain<String>(Constants.markovChainGenerationValues)
    val chainIgnoreTerms = listOf("markov")

    logger.info("Building Markov chain...")

    val chainBuildTime = measureTime {
        val messages = json.decodeFromStream<List<String>>(MarkovChain::class.java.getResourceAsStream("data.json")!!)

        markovChain.addData(
            messages.map { message ->
                message.split(Constants.wordSeparatorRegex).filter { word ->
                    chainIgnoreTerms.none { it in word }
                }
            }
        )
    }

    logger.info("Building the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")

    val redditBotCredentials = Credentials.script(
        username = System.getenv("markovbaj_username"),
        password = System.getenv("markovbaj_password"),
        clientId = System.getenv("markovbaj_clientid"),
        clientSecret = System.getenv("markovbaj_clientsecret")
    )

    val userAgent = UserAgent(
        platform = "JVM/JRAW",
        appId = "MarkovBaj",
        version = BuildInfo.version,
        redditUsername = "MarkovBaj"
    )

    val redditClient = OAuthHelper.automatic(OkHttpNetworkAdapter(userAgent), redditBotCredentials).apply {
        logHttp = false
    }

    logger.info("Connected to Reddit.")

    val forsenSubreddit = redditClient.subreddit("forsen")

    var alreadyProcessedPostIds = listOf<String>()
    var alreadyProcessedCommentsIds = listOf<String>()

    val botCoroutineScope = CoroutineScope(Dispatchers.Default)

    logger.info("Bot running.")

    fixedRateTimer(period = Constants.checkInterval.inWholeMilliseconds) {
        botCoroutineScope.launch {
            val newInboxMessages = redditClient.me()
                .inbox()
                .iterate("unread")
                .build()
                .accumulateMerged(1)
                .filter { it.subject == "username mention" }

            val newPosts = forsenSubreddit.posts()
                .sorting(SubredditSort.NEW)
                .limit(10)
                .build()
                .accumulateMerged(1)
                .filter { it.created.toInstant().isAfter(Instant.now().minus(Constants.checkInterval.toJavaDuration().multipliedBy(2))) && it.id !in alreadyProcessedPostIds }

            val newComments = forsenSubreddit.comments()
                .limit(100)
                .build()
                .accumulateMerged(1)
                .filter { it.created.toInstant().isAfter(Instant.now().minus(Constants.checkInterval.toJavaDuration().multipliedBy(2))) && it.id !in alreadyProcessedCommentsIds }

            logger.info("${newInboxMessages.size} new mention(s), ${newPosts.size} new post(s), ${newComments.size} new comment(s).")

            alreadyProcessedPostIds = newPosts.map { it.id }
            alreadyProcessedCommentsIds = newComments.map { it.id }

            var commentCounter = 0

            for (message in newInboxMessages) {
                if (commentCounter >= Constants.maxCommentsPerCheck) {
                    logger.warn("Hit comment limit, not posting any more replies.")
                    return@launch
                }

                if (!message.isComment) {
                    logger.warn("Username mention with id ${message.id} was not a comment, skipping...")
                    return@launch
                }

                val wordsInTitle = message.body.lowercase().split(Constants.wordSeparatorRegex)

                val relatedReply = if (Math.random() > Constants.unrelatedAnswerChance) {
                    tryGeneratingReplyFromWords(markovChain, wordsInTitle)
                } else {
                    null
                }

                val actualReply = if (relatedReply != null) {
                    logger.info("Replying to mention by ${message.author} in message ${message.id} in ${message.subreddit?.let { "r/$it" } ?: "-"} ('${message.body}') with related answer...")
                    relatedReply
                } else {
                    markovChain.generateSequence().joinToString(" ").also {
                        logger.info("Default replying to mention by ${message.author} in message ${message.id} in ${message.subreddit?.let { "r/$it" } ?: "-"} ('${message.body}')...")
                    }
                }

                redditClient.comment(message.id).safeReply(actualReply)
                redditClient.me().inbox().markRead(true, message.fullName)
                commentCounter++

                delay(Constants.delayBetweenComments)
            }

            for (post in newPosts) {
                if (commentCounter >= Constants.maxCommentsPerCheck) {
                    logger.warn("Hit comment limit, not posting any more replies.")
                    return@launch
                }

                if (post.isRemoved) {
                    continue
                }

                if ("markov" in post.title.lowercase()) {
                    val wordsInTitle = post.title.lowercase().split(Constants.wordSeparatorRegex)

                    val relatedReply = if (Math.random() > Constants.unrelatedAnswerChance) {
                        tryGeneratingReplyFromWords(markovChain, wordsInTitle)
                    } else {
                        null
                    }

                    val actualReply = if (relatedReply != null) {
                        logger.info("Replying to post ${post.id} ('${post.title}') with related answer...")
                        relatedReply
                    } else {
                        markovChain.generateSequence().joinToString(" ").also {
                            logger.info("Default replied to post ${post.id} ('${post.title}')...")
                        }
                    }

                    post.toReference(redditClient).safeReply(actualReply)
                    commentCounter++

                    delay(Constants.delayBetweenComments)
                }
            }

            for (comment in newComments) {
                if (commentCounter >= Constants.maxCommentsPerCheck) {
                    logger.warn("Hit comment limit, not posting any more replies.")
                    return@launch
                }

                if ("markov" in comment.body.lowercase()) {
                    val wordsInComment = comment.body.lowercase().split(Constants.wordSeparatorRegex)

                    val relatedReply = if (Math.random() > Constants.unrelatedAnswerChance) {
                        tryGeneratingReplyFromWords(markovChain, wordsInComment)
                    } else {
                        null
                    }

                    val actualReply = if (relatedReply != null) {
                        logger.info("Replying to comment ${comment.id} ('${comment.body}') with related answer...")
                        relatedReply
                    } else {
                        markovChain.generateSequence().joinToString(" ").also {
                            logger.info("Default replying to comment ${comment.id} ('${comment.body}')...")
                        }
                    }

                    comment.toReference(redditClient).safeReply(actualReply)
                    commentCounter++

                    delay(Constants.delayBetweenComments)
                }
            }
        }
    }
}

fun tryGeneratingReplyFromWords(markovChain: MarkovChain<String>, words: List<String>): String? {
    words.windowed(Constants.markovChainGenerationValues).shuffled().forEach { potentialChainStart ->
        if (potentialChainStart in markovChain.chainStarts.weightMap.keys) {
            return markovChain.generateSequence(start = potentialChainStart).joinToString(" ")
        }
    }

    logger.info("Unable to generate a response, sending default...")
    return null
}

fun PublicContributionReference.safeReply(text: String) {
    if (System.getenv("markovbaj_actuallysendreplies") == "true") {
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