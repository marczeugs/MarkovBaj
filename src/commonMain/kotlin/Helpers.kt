import mu.KotlinLogging


private val logger = KotlinLogging.logger("MarkovBaj:Helpers")


// language=regexp
private const val WORD_PART_END_MARKER_REGEX = "[/()\\-_?!]"

private val wordPartFinderRegex = Regex(" *[A-Z0]+?.?(?=\\s+|$|$WORD_PART_END_MARKER_REGEX|(?<=$WORD_PART_END_MARKER_REGEX))| *.+?(?=[A-Z]|\\s+|$|$WORD_PART_END_MARKER_REGEX|(?<=$WORD_PART_END_MARKER_REGEX))")

fun String.toWordParts() = listOf(null) + wordPartFinderRegex.findAll(this).map { it.value }.toList()

fun MarkovChain<String?>.generateRandomReply() = generateSequence().filterNotNull().joinToString("").trim()

fun MarkovChain<String?>.tryGeneratingReplyFromWords(words: List<String?>, platform: String): String? {
    words
        .filter { word -> word?.let { CommonConstants.triggerKeyword !in it.lowercase() } ?: true }
        .windowed(CommonConstants.consideredValuesForGeneration)
        .shuffled()
        .forEach { potentialChainStart ->
            if (chainStarts.weightMap.keys.any { words -> words.map { it?.lowercase()?.trim() } == potentialChainStart.map { it?.lowercase()?.trim() } }) {
                logger.info { "[$platform] Generated response for chain start $potentialChainStart." }
                return generateSequence(start = potentialChainStart).filterNotNull().joinToString("").trim()
            }
        }

    logger.info { "[$platform] Unable to generate a response, using default instead..." }
    return null
}