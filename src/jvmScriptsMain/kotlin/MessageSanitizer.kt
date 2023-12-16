package scripts

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private sealed interface MessageExclusionCriteria {
    data class String(val value: kotlin.String) : MessageExclusionCriteria
    data class Regex(val value: kotlin.text.Regex) : MessageExclusionCriteria
}

private val emoteCodeMapping = mapOf(
    "9674" to "forsenE",
    "9677" to "gachiBASS",
    "9673" to "forsenDespair",
    "9685" to "forsenBased",
    "9682" to "OMEGALUL",
    "9669" to "Copesen",
    "9684" to "PagMan",
    "9679" to "Sadeg",
    "9672" to "forsenCD",
    "9671" to "BatChest",
    "9675" to ":tf:",
    "9666" to "Clueless",
    "9678" to "monkaOMEGA",
    "9683" to "WutFace",
    "9668" to "cmonBruh",
    "9680" to "pepeLaugh",
    "9676" to "FeelsOkayMan",
    "9681" to "LULE",
    "9670" to "forsenLevel",
    "9667" to "Okayeg",
    "10257" to "amongE",
)

private val filteredAuthors = listOf(
    System.getenv("markovbaj_username").lowercase(),
    "[deleted]",
)

private val replacedParts = mapOf<Regex, (MatchResult) -> String>(
    // Bot mentions
    Regex(CommonConstants.triggerKeyword, setOf(RegexOption.IGNORE_CASE, RegexOption.LITERAL)) to { "" },
    // Emotes
    Regex("!?\\[img]\\(emote\\|.+?\\|([0-9]+)\\)", RegexOption.IGNORE_CASE) to { match -> emoteCodeMapping[match.groupValues[1]]?.let { " $it " } ?: "" },
    // Reddit embedded GIFs
    Regex("!?\\[gif]\\(.+?\\)", RegexOption.IGNORE_CASE) to { "" },
    // Remove Markdown links
    Regex("\\[(.*?)]\\(.*?\\)", RegexOption.IGNORE_CASE) to { it.groupValues[1] },
    // Remove bare links
//        Regex("https?://.+?(?:$|\\s)", RegexOption.IGNORE_CASE) to { "" },
    // Weird stuff with zero width spaces at the beginning of comments
    Regex("&amp;#x200B;\\s*", RegexOption.IGNORE_CASE) to { "" },
    // Unescape &
    Regex("&amp;", RegexOption.IGNORE_CASE) to { "&" },
    // Unescape <
    Regex("&lt;", RegexOption.IGNORE_CASE) to { "<" },
    // Unescape >
    Regex("&gt;", RegexOption.IGNORE_CASE) to { ">" },
    // Remove line breaks, handling them is just a pain
    Regex("\\n+", RegexOption.IGNORE_CASE) to { " " },
    // Normalise spaces
    Regex("  +", RegexOption.IGNORE_CASE) to { " " },
    // Remove remaining emotes and images
//        Regex("!?\\[(?:img|gif)]\\(.+\\)", RegexOption.IGNORE_CASE) to { "" },
)

private val encodedQuestionableStrings = listOf(
    "c2llZw==",
    "aGVpbA==",
    "a2lrZQ==",
    "Y2hpbms=",
    "ZmFn",
    "a3lz",
    "Z3Jvb20=",
    "MTQ=",
    "ODg=",
    "cmFwZQ==",
)

@OptIn(ExperimentalEncodingApi::class)
private val messageExclusionCriteriaWordParts = listOf(
    MessageExclusionCriteria.String("neg"),
    MessageExclusionCriteria.String("nek"),
    MessageExclusionCriteria.String("tran"),
    MessageExclusionCriteria.String("kfc"),
    MessageExclusionCriteria.String("melon"),
    MessageExclusionCriteria.String("black"),
    MessageExclusionCriteria.String("white"),
    MessageExclusionCriteria.String("afric"),
    MessageExclusionCriteria.String("migra"),
    MessageExclusionCriteria.String("crack"),
    MessageExclusionCriteria.String("kill"),
    MessageExclusionCriteria.String("uicid"),
    MessageExclusionCriteria.String("murd"),
    MessageExclusionCriteria.String("etar"),
    MessageExclusionCriteria.String("underag"),
    MessageExclusionCriteria.String("gun"),
    MessageExclusionCriteria.String("jew"),
    MessageExclusionCriteria.String("semit"),
    MessageExclusionCriteria.String("gender"),
    MessageExclusionCriteria.String("minor"),
    MessageExclusionCriteria.String("lgb"),
    MessageExclusionCriteria.String("sex"),
    MessageExclusionCriteria.String("gas"),
    MessageExclusionCriteria.String("genocid"),
    *encodedQuestionableStrings.map { MessageExclusionCriteria.String(Base64.decode(it).decodeToString()) }.toTypedArray(),
    MessageExclusionCriteria.Regex(Regex("n.?word", RegexOption.IGNORE_CASE)),
    MessageExclusionCriteria.Regex(Regex("shoo?t", RegexOption.IGNORE_CASE)),
    MessageExclusionCriteria.Regex(Regex("self.?harm", RegexOption.IGNORE_CASE)),
    MessageExclusionCriteria.Regex(Regex("\\brac(?:e\\b|is)", RegexOption.IGNORE_CASE)),
    MessageExclusionCriteria.Regex(Regex("hate (?:th|ni|'?em)", RegexOption.IGNORE_CASE)),
    MessageExclusionCriteria.Regex(Regex("\\bni(?:g|$|[^a-z])", RegexOption.IGNORE_CASE)),
)

fun sanitizeComment(author: String, content: String) =
    if (
        author.lowercase() !in filteredAuthors
        && messageExclusionCriteriaWordParts.none {
            when (it) {
                is MessageExclusionCriteria.String -> it.value in content.lowercase()
                is MessageExclusionCriteria.Regex -> it.value.containsMatchIn(content.lowercase())
            }
        }
    ) {
        replacedParts.entries
            .fold(content) { acc, (replacedPart, replacingPart) -> acc.replace(replacedPart, replacingPart) }
            .trim()
            .takeIf { it.isNotEmpty() }
    } else {
        null
    }