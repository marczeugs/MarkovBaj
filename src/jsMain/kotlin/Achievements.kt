import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color

data class Achievement(
    val id: Int,
    val color: CSSColorValue,
    val name: String,
    val description: String,
    val matcher: Matcher
) {
    sealed interface Matcher {
        data class KeywordList(val keywords: List<String>) : Matcher
        data class Lambda(val matcher: (input: String, output: String) -> Boolean) : Matcher
    }
}

val turkishCharacters = listOf('\u011f', '\u0131', '\u015f')

val achievements = listOf(
    Achievement(
        id = 1,
        color = Color.blanchedalmond,
        name = "Capybara",
        description = "Make Markov mention capybaras",
        matcher = Achievement.Matcher.KeywordList(listOf("capybara"))
    ),
    Achievement(
        id = 2,
        color = Color.lightsalmon,
        name = "xqcL",
        description = "Make Markov mention xQc",
        matcher = Achievement.Matcher.KeywordList(listOf("xqc"))
    ),
    Achievement(
        id = 3,
        color = Color.darkorange,
        name = "CV Paste",
        description = "Have Markov copy your message",
        matcher = Achievement.Matcher.Lambda { input, output ->
            input == output
        }
    ),
    Achievement(
        id = 4,
        color = Color.blueviolet,
        name = "Forsen Related",
        description = "Make Markov use the word \"forsen\" at least 20 times in one response",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.split(Regex("\\bforsen\\b")).size >= 21 && "forsen forsen" !in input.lowercase()
        }
    ),
    Achievement(
        id = 5,
        color = Color.burlywood,
        name = "+20 Social Credit Points",
        description = "Make Markov speak Chinese",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u4e00'..'\u9fff' } && input.none { it in '\u4e00'..'\u9fff' }
        }
    ),
    Achievement(
        id = 6,
        color = Color.aquamarine,
        name = "Arigato",
        description = "Make Markov speak Japanese",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u3040'..'\u309f' } && input.none { it in '\u3040'..'\u309f' }
        }
    ),
    Achievement(
        id = 7,
        color = Color.cornflowerblue,
        name = "Cyka Blyat",
        description = "Make Markov speak Russian",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u0400'..'\u04ff' } && input.none { it in '\u0400'..'\u04ff' }
        }
    ),
    Achievement(
        id = 8,
        color = Color.cornflowerblue,
        name = "Halal",
        description = "Make Markov speak Arabic",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u0600'..'\u06ff' } && input.none { it in '\u0600'..'\u06ff' }
        }
    ),
    Achievement(
        id = 9,
        color = Color.cornflowerblue,
        name = "TÜRKIYE 💪",
        description = "Make Markov speak Turkish",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.lowercase().any { it in turkishCharacters } && input.lowercase().none { it in turkishCharacters }
        }
    ),
    Achievement(
        id = 10,
        color = Color.cornflowerblue,
        name = ":tf: 🤜 🔔",
        description = "Make Markov ping someone",
        matcher = Achievement.Matcher.KeywordList(listOf("u/"))
    ),
    Achievement(
        id = 11,
        color = Color.cornflowerblue,
        name = "Please clarify",
        description = "Make Markov ping u/TheSeaHorseHS",
        matcher = Achievement.Matcher.KeywordList(listOf("u/theseahorsehs"))
    ),
    Achievement(
        id = 12,
        color = Color.cornflowerblue,
        name = "uwu",
        description = "Make Markov ping u/Furry_Degen",
        matcher = Achievement.Matcher.KeywordList(listOf("u/furry_degen"))
    ),
    Achievement(
        id = 13,
        color = Color.cornflowerblue,
        name = "Not Suge Knight",
        description = "Make Markov talk about shungite",
        matcher = Achievement.Matcher.KeywordList(listOf("shungite"))
    ),
    Achievement(
        id = 14,
        color = Color.cornflowerblue,
        name = "Kinda Snus",
        description = "Make Markov mention something Among Us related",
        matcher = Achievement.Matcher.KeywordList(listOf("among us", "amogus", "amonge", "sus ", "sussy", "crewmate", "impostor", "imposter", "emergency meeting", "ඞ"))
    ),
    Achievement(
        id = 15,
        color = Color.cornflowerblue,
        name = "forsen",
        description = "Make Markov use a singular \"forsen\" as a response",
        matcher = Achievement.Matcher.Lambda { _, output ->
            output == "forsen"
        }
    ),
    Achievement(
        id = 16,
        color = Color.cornflowerblue,
        name = "MrDestructoid",
        description = "Make Markov speak binary",
        matcher = Achievement.Matcher.Lambda { input, output ->
            Regex("\\b[01]{8}\\b").containsMatchIn(output) && !Regex("\\b[01]{8}\\b").containsMatchIn(input)
        }
    ),
    Achievement(
        id = 17,
        color = Color.cornflowerblue,
        name = "Concerned",
        description = "Make Markov ask if you have any questions or concerns",
        matcher = Achievement.Matcher.KeywordList(listOf("if you have any questions or concerns"))
    ),
    Achievement(
        id = 18,
        color = Color.cornflowerblue,
        name = "forsenCD",
        description = "Make Markov give you a completely transparent response",
        matcher = Achievement.Matcher.Lambda { _, output ->
            output == "\u200B"
        }
    ),
    Achievement(
        id = 19,
        color = Color.cornflowerblue,
        name = "fr fr ong",
        description = "Make Markov say zoomer shit",
        matcher = Achievement.Matcher.KeywordList(listOf("fr ", "ong ", "on god", "deadass", "bussin", "no cap", "goated", "fax"))
    ),
    Achievement(
        id = 20,
        color = Color.cornflowerblue,
        name = "YOURMOM",
        description = "Make Markov talk about your mother",
        matcher = Achievement.Matcher.KeywordList(listOf("your mom", "your mother", "your mum"))
    ),
    Achievement(
        id = 21,
        color = Color.cornflowerblue,
        name = "Different people Copesen",
        description = "Make Markov mention Gura",
        matcher = Achievement.Matcher.KeywordList(listOf("gura"))
    ),
    Achievement(
        id = 22,
        color = Color.cornflowerblue,
        name = "forsenLevel",
        description = "Make Markov level",
        matcher = Achievement.Matcher.KeywordList(listOf("forsenlevel"))
    ),
    Achievement(
        id = 23,
        color = Color.cornflowerblue,
        name = "Arcane",
        description = "Make Markov talk about Arcane",
        matcher = Achievement.Matcher.KeywordList(listOf("caitlyn", "jinx", "arcane"))
    ),
    Achievement(
        id = 24,
        color = Color.cornflowerblue,
        name = "BatChest",
        description = "Make Markov bat IRL",
        matcher = Achievement.Matcher.KeywordList(listOf("marvel", "thanos", "batchest"))
    ),
    Achievement(
        id = 25,
        color = Color.cornflowerblue,
        name = "FUCK♂YOU",
        description = "Make Markov talk about Gachi",
        matcher = Achievement.Matcher.KeywordList(listOf("gachi", "billy herrington"))
    ),
    Achievement(
        id = 26,
        color = Color.cornflowerblue,
        name = "💦",
        description = "Make Markov coom to something",
        matcher = Achievement.Matcher.KeywordList(listOf("forsencoomer"))
    ),
    Achievement(
        id = 27,
        color = Color.cornflowerblue,
        name = "NURSE??? 🔔",
        description = "Make Markov suggest that you take your pills",
        matcher = Achievement.Matcher.KeywordList(listOf("take your pills"))
    ),
    Achievement(
        id = 28,
        color = Color.cornflowerblue,
        name = "You Should ...",
        description = "Make Markov use both the \uD83C\uDF29 (thunderstorm) emoji as well as \"NOW\" in the same response",
        matcher = Achievement.Matcher.Lambda { input, output ->
            "\uD83C\uDF29" in output && "NOW" in output && "\uD83C\uDF29" !in input && "NOW" !in input
        }
    ),
    Achievement(
        id = 29,
        color = Color.cornflowerblue,
        name = "Punk Kid",
        description = "Make Markov post the Among Us crewmate cock copypasta",
        matcher = Achievement.Matcher.KeywordList(listOf("⣿⣿⣿⠟⢹⣶⣶⣝⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿"))
    ),
    Achievement(
        id = 30,
        color = Color.cornflowerblue,
        name = "monkaLaugh",
        description = "Make Markov mention Hitler",
        matcher = Achievement.Matcher.KeywordList(listOf("hitler"))
    ),
    Achievement(
        id = 31,
        color = Color.cornflowerblue,
        name = "The Witch",
        description = "Make Markov mention N̵̨̮͉̟̣̥̰̫̹̣̱̩̭̦̿̅̆̈́̌́̍̾́͆͒̄̒̚̚i̷̧̩̯͖̖͎̱̹̿̓̈́̑̐͗͂̓́̇̕͠ņ̶̡̠̞̖͓͓̹̳̦̞͍̈̔̈́̉̀́̓͗̕͜a̴̡̨̫̤͈̳̜̺̩̗̗̖̼̖͔̍̅͑͋̈́͑̾̾̽̍̈́̚",
        matcher = Achievement.Matcher.KeywordList(listOf("nina ", "nani "))
    ),
    Achievement(
        id = 32,
        color = Color.cornflowerblue,
        name = "Happy Birthday",
        description = "Make Markov wish you a happy birthday",
        matcher = Achievement.Matcher.KeywordList(listOf("happy birthday"))
    ),
    Achievement(
        id = 33,
        color = Color.cornflowerblue,
        name = "eg?",
        description = "Make Markov talk about eggs",
        matcher = Achievement.Matcher.KeywordList(listOf("🥚", " eg ", "egg ", "eggs "))
    ),
    Achievement(
        id = 34,
        color = Color.cornflowerblue,
        name = "MODS",
        description = "Make Markov says that he is chicken",
        matcher = Achievement.Matcher.KeywordList(listOf("i am chicken", "im chicken", "i'm chicken"))
    ),
    Achievement(
        id = 35,
        color = Color.cornflowerblue,
        name = "Vi Sitter Här",
        description = "Make Markov rant about your Dota skills",
        matcher = Achievement.Matcher.KeywordList(listOf("retard player", "ritard player", "retard pleyer", "ritard pleyer"))
    ),
    Achievement(
        id = 36,
        color = Color.cornflowerblue,
        name = "Bruce U",
        description = "Make Markov mention Uganda or Uganda related topics",
        matcher = Achievement.Matcher.KeywordList(listOf("uganda", "wakaliwood", "captain alex", "tiger mafia", "bruce u", "pastor lul"))
    ),
)