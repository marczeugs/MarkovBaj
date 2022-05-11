import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Constants {
    const val redditUserName = "MarkovBaj"
    const val activeSubreddit = "forsen"
    const val markovChainGenerationValues = 2
    val wordSeparatorRegex = Regex("\\s+")
    val checkInterval = 2.minutes
    const val maxCommentsPerCheck = 5
    val delayBetweenComments = 15.seconds
    const val unrelatedAnswerChance = 0.33
}