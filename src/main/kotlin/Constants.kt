import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Constants {
    val markovChainGenerationValues = 2
    val wordSeparatorRegex = Regex("\\s+")
    val checkInterval = 2.minutes
    val maxCommentsPerCheck = 5
    val delayBetweenComments = 15.seconds
    val unrelatedAnswerChance = 0.33
}