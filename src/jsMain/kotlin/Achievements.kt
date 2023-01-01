import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val matcher: Matcher
) {
    sealed interface Matcher {
        data class KeywordList(val keywords: List<String>) : Matcher
        data class Regex(val regex: kotlin.text.Regex) : Matcher
        data class Lambda(val matcher: (input: String, output: String) -> Boolean) : Matcher
    }
}

@Serializable
data class CompletedAchievement(
    val instant: Instant,
    val query: String,
    val response: String
)

private val lightningEmotes = listOf("\uD83C\uDF29", "\u26A1")

val achievements = listOf(
    Achievement(
        id = 1,
        name = "Capybara",
        description = "Make Markov mention capybaras",
        matcher = Achievement.Matcher.KeywordList(listOf("capybara"))
    ),
    Achievement(
        id = 2,
        name = "xqcL",
        description = "Make Markov mention xQc",
        matcher = Achievement.Matcher.KeywordList(listOf("xqc"))
    ),
    Achievement(
        id = 3,
        name = "CV Paste",
        description = "Have Markov copy your message",
        matcher = Achievement.Matcher.Lambda { input, output ->
            input == output
        }
    ),
    Achievement(
        id = 4,
        name = "Forsen Related",
        description = "Make Markov use the word \"forsen\" at least 20 times in one response",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.lowercase().split(Regex("\\bforsen\\b")).size >= 21 && "forsen forsen" !in input.lowercase()
        }
    ),
    Achievement(
        id = 5,
        name = "+20 Social Credit Points",
        description = "Make Markov speak Chinese",
        matcher = Achievement.Matcher.Regex(Regex("[\\u4e00-\\u9fff]"))
    ),
    Achievement(
        id = 6,
        name = "Arigato",
        description = "Make Markov speak Japanese",
        matcher = Achievement.Matcher.Regex(Regex("[\\u3040-\\u309f]"))
    ),
    Achievement(
        id = 7,
        name = "Cyka Blyat",
        description = "Make Markov speak Russian",
        matcher = Achievement.Matcher.Regex(Regex("[\\u0400-\\u04ff]"))
    ),
    Achievement(
        id = 8,
        name = "Halal",
        description = "Make Markov speak Arabic",
        matcher = Achievement.Matcher.Regex(Regex("[\\u0600-\\u06ff]"))
    ),
    Achievement(
        id = 9,
        name = "TÜRKIYE 💪",
        description = "Make Markov speak Turkish",
        matcher = Achievement.Matcher.Regex(Regex("[\\u011f\\u0131\\u015f]"))
    ),
    Achievement(
        id = 10,
        name = ":tf: 🤜 🔔",
        description = "Make Markov ping someone",
        matcher = Achievement.Matcher.KeywordList(listOf("u/"))
    ),
    Achievement(
        id = 11,
        name = "Please clarify",
        description = "Make Markov ping u/TheSeaHorseHS",
        matcher = Achievement.Matcher.KeywordList(listOf("u/theseahorsehs"))
    ),
    Achievement(
        id = 12,
        name = "uwu",
        description = "Make Markov ping u/Furry_Degen",
        matcher = Achievement.Matcher.KeywordList(listOf("u/furry_degen"))
    ),
    Achievement(
        id = 13,
        name = "Not Suge Knight",
        description = "Make Markov talk about shungite",
        matcher = Achievement.Matcher.KeywordList(listOf("shungite"))
    ),
    Achievement(
        id = 14,
        name = "Kinda Snus",
        description = "Make Markov mention something Among Us related",
        matcher = Achievement.Matcher.KeywordList(listOf("among us", "amogus", "amonge", "sus ", "sussy", "crewmate", "impostor", "imposter", "emergency meeting", "ඞ"))
    ),
    Achievement(
        id = 15,
        name = "forsen",
        description = "Make Markov use a singular \"forsen\" as a response",
        matcher = Achievement.Matcher.Lambda { _, output ->
            output == "forsen"
        }
    ),
    Achievement(
        id = 16,
        name = "MrDestructoid",
        description = "Make Markov speak binary",
        matcher = Achievement.Matcher.Regex(Regex("\\b[01]{8}\\b"))
    ),
    Achievement(
        id = 17,
        name = "Concerned",
        description = "Make Markov ask if you have any questions or concerns",
        matcher = Achievement.Matcher.KeywordList(listOf("you have any questions or concerns"))
    ),
    Achievement(
        id = 18,
        name = "forsenCD",
        description = "Make Markov give you a completely transparent response",
        matcher = Achievement.Matcher.Regex(Regex("^\\u200b$"))
    ),
    Achievement(
        id = 19,
        name = "fr fr ong",
        description = "Make Markov say zoomer shit",
        matcher = Achievement.Matcher.Regex(Regex("\\bfr\\b|\\bong\\b|on god|deadass|bussin|no cap|goated"))
    ),
    Achievement(
        id = 20,
        name = "YOURMOM",
        description = "Make Markov talk about your mother",
        matcher = Achievement.Matcher.KeywordList(listOf("your mom", "your mother", "your mum"))
    ),
    Achievement(
        id = 21,
        name = "Different people Copesen",
        description = "Make Markov mention Gura",
        matcher = Achievement.Matcher.KeywordList(listOf("gura"))
    ),
    Achievement(
        id = 22,
        name = "forsenLevel",
        description = "Make Markov level",
        matcher = Achievement.Matcher.KeywordList(listOf("forsenlevel"))
    ),
    Achievement(
        id = 23,
        name = "Arcane",
        description = "Make Markov talk about Arcane",
        matcher = Achievement.Matcher.KeywordList(listOf("caitlyn", "jinx", "arcane"))
    ),
    Achievement(
        id = 24,
        name = "BatChest",
        description = "Make Markov bat IRL",
        matcher = Achievement.Matcher.KeywordList(listOf("marvel", "thanos", "batchest"))
    ),
    Achievement(
        id = 25,
        name = "FUCK♂YOU",
        description = "Make Markov talk about Gachi",
        matcher = Achievement.Matcher.KeywordList(listOf("gachi", "billy herrington"))
    ),
    Achievement(
        id = 26,
        name = "💦",
        description = "Make Markov coom to something",
        matcher = Achievement.Matcher.KeywordList(listOf("forsencoomer"))
    ),
    Achievement(
        id = 27,
        name = "NURSE??? 🔔",
        description = "Make Markov suggest that you take your pills",
        matcher = Achievement.Matcher.Regex(Regex("take (?:your|my|the) (?:fucking? )?(?:pills|meds|medicine)"))
    ),
    Achievement(
        id = 28,
        name = "You Should ...",
        description = "Make Markov use both the \uD83C\uDF29 (thunderstorm) emoji as well as \"NOW\" in the same response",
        matcher = Achievement.Matcher.Lambda { input, output ->
            lightningEmotes.any { it in output } && "NOW" in output && lightningEmotes.none { it in input } && "NOW" !in input
        }
    ),
    Achievement(
        id = 29,
        name = "Punk Kid",
        description = "Make Markov post the Among Us crewmate cock copypasta",
        matcher = Achievement.Matcher.KeywordList(listOf("⣿⣿⣿⠟⢹⣶⣶⣝⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿"))
    ),
    Achievement(
        id = 30,
        name = "monkaLaugh",
        description = "Make Markov mention Hitler",
        matcher = Achievement.Matcher.KeywordList(listOf("hitler"))
    ),
    Achievement(
        id = 31,
        name = "The Witch",
        description = "Make Markov mention N̵̨̮͉̟̣̥̰̫̹̣̱̩̭̦̿̅̆̈́̌́̍̾́͆͒̄̒̚̚i̷̧̩̯͖̖͎̱̹̿̓̈́̑̐͗͂̓́̇̕͠ņ̶̡̠̞̖͓͓̹̳̦̞͍̈̔̈́̉̀́̓͗̕͜a̴̡̨̫̤͈̳̜̺̩̗̗̖̼̖͔̍̅͑͋̈́͑̾̾̽̍̈́̚",
        matcher = Achievement.Matcher.Regex(Regex("\\bnina\\b|\\bnani\\b"))
    ),
    Achievement(
        id = 32,
        name = "Happy Birthday",
        description = "Make Markov wish you a happy birthday",
        matcher = Achievement.Matcher.KeywordList(listOf("happy birthday"))
    ),
    Achievement(
        id = 33,
        name = "eg?",
        description = "Make Markov talk about eggs",
        matcher = Achievement.Matcher.Regex(Regex("\uD83E\uDD5A|\\beg\\b|\\begg"))
    ),
    Achievement(
        id = 34,
        name = "MODS",
        description = "Make Markov say that he is chicken",
        matcher = Achievement.Matcher.KeywordList(listOf("i am chicken", "im chicken", "i'm chicken"))
    ),
    Achievement(
        id = 35,
        name = "Vi Sitter Här",
        description = "Make Markov rant about your Dota skills",
        matcher = Achievement.Matcher.Regex(Regex("(?:da|that)'?s a big problem|f[auo]king? tr[ea]sh|26 minute?|big r[ei]tard pl[ae]yer"))
    ),
    Achievement(
        id = 36,
        name = "Bruce U",
        description = "Make Markov mention Uganda or Uganda related topics",
        matcher = Achievement.Matcher.KeywordList(listOf("uganda", "wakaliwood", "captain alex", "tiger mafia", "bruce u", "pastor lul"))
    ),
)