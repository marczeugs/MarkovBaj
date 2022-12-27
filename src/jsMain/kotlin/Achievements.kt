import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color

data class Achievement(
    val color: CSSColorValue,
    val icon: String,
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
        color = Color.blanchedalmond,
        icon = "forsenE.png",
        name = "Capybara",
        description = "Make Markov mention capybaras",
        matcher = Achievement.Matcher.KeywordList(listOf("capybara"))
    ),
    Achievement(
        color = Color.lightsalmon,
        icon = "forsenE.png",
        name = "xQc",
        description = "Make Markov mention xQc",
        matcher = Achievement.Matcher.KeywordList(listOf("xqc"))
    ),
    Achievement(
        color = Color.darkorange,
        icon = "forsenE.png",
        name = "Ctrl + C",
        description = "Have Markov copy your message",
        matcher = Achievement.Matcher.Lambda { input, output ->
            input == output
        }
    ),
    Achievement(
        color = Color.blueviolet,
        icon = "forsenE.png",
        name = "forsenPossessed",
        description = "Make Markov use the word \"forsen\" at least 20 times in one response",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.split(Regex("\\bforsen\\b")).size >= 21 && "forsen forsen" !in input.lowercase()
        }
    ),
    Achievement(
        color = Color.burlywood,
        icon = "forsenE.png",
        name = "Chinese",
        description = "Make Markov speak Chinese",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u4e00'..'\u9fff' } && input.none { it in '\u4e00'..'\u9fff' }
        }
    ),
    Achievement(
        color = Color.aquamarine,
        icon = "forsenE.png",
        name = "Japanese",
        description = "Make Markov speak Japanese",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u3040'..'\u309f' } && input.none { it in '\u3040'..'\u309f' }
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Russian",
        description = "Make Markov speak Russian",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u0400'..'\u04ff' } && input.none { it in '\u0400'..'\u04ff' }
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Arabic",
        description = "Make Markov speak Arabic",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.any { it in '\u0600'..'\u06ff' } && input.none { it in '\u0600'..'\u06ff' }
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Turkish",
        description = "Make Markov speak Turkish",
        matcher = Achievement.Matcher.Lambda { input, output ->
            output.lowercase().any { it in turkishCharacters } && input.lowercase().none { it in turkishCharacters }
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = ":tf: 🤜 🔔",
        description = "Make Markov ping someone",
        matcher = Achievement.Matcher.KeywordList(listOf("u/"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Please clarify",
        description = "Make Markov ping u/TheSeaHorseHS",
        matcher = Achievement.Matcher.KeywordList(listOf("u/theseahorsehs"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenFur",
        description = "Make Markov ping u/Furry_Degen",
        matcher = Achievement.Matcher.KeywordList(listOf("u/furry_degen"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsen5G",
        description = "Make Markov talk about shungite",
        matcher = Achievement.Matcher.KeywordList(listOf("shungite"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "amongE",
        description = "Make Markov mention something Among Us related",
        matcher = Achievement.Matcher.KeywordList(listOf("among us", "amogus", "amongE", "sus ", "sussy", "crewmate", "impostor", "imposter", "vent", "emergency meeting", "ඞ"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsen",
        description = "Make Markov use a singular \"forsen\" as a response",
        matcher = Achievement.Matcher.Lambda { _, output ->
            output == "forsen"
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "MrDestructoid",
        description = "Make Markov speak binary",
        matcher = Achievement.Matcher.Lambda { input, output ->
            Regex("\\b[01]{8}\\b").containsMatchIn(output) && !Regex("\\b[01]{8}\\b").containsMatchIn(input)
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Concerned",
        description = "Make Markov ask if you have any questions or concerns",
        matcher = Achievement.Matcher.KeywordList(listOf("if you have any questions or concerns"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenCD",
        description = "Make Markov give you a completely transparent response",
        matcher = Achievement.Matcher.Lambda { _, output ->
            output == "\u200B"
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenBussin",
        description = "Make Markov say zoomer shit",
        matcher = Achievement.Matcher.KeywordList(listOf("fr ", "ong ", "on god", "deadass", "bussin", "no cap", "goated", "fax"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenHead",
        description = "Make Markov talk about your mother",
        matcher = Achievement.Matcher.KeywordList(listOf("your mom", "your mother", "your mum"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Different people Copesen",
        description = "Make Markov mention Gura",
        matcher = Achievement.Matcher.KeywordList(listOf("gura"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenLevel",
        description = "Make Markov level",
        matcher = Achievement.Matcher.KeywordList(listOf("forsenLevel"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "PoroSad",
        description = "Make Markov talk about Arcane",
        matcher = Achievement.Matcher.KeywordList(listOf("caitlyn", "jinx", "arcane"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "BatChest",
        description = "Make Markov bat IRL",
        matcher = Achievement.Matcher.KeywordList(listOf("marvel", "thanos", "batchest"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "gachiBASS",
        description = "Make Markov talk about Gachi",
        matcher = Achievement.Matcher.KeywordList(listOf("gachi", "billy herrington"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenCoomer",
        description = "Make Markov coom to something",
        matcher = Achievement.Matcher.KeywordList(listOf("forsenCoomer"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "💊",
        description = "Make Markov suggest that you take your pills",
        matcher = Achievement.Matcher.KeywordList(listOf("take your pills"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenLevel NOW",
        description = "Make Markov use both the \uD83C\uDF29 emoji as well as \"NOW\" in the same response",
        matcher = Achievement.Matcher.Lambda { input, output ->
            "\uD83C\uDF29" in output && "NOW" in output && "\uD83C\uDF29" !in input && "NOW" !in input
        }
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Punk Kid",
        description = "Make Markov post the Among Us crewmate cock copypasta",
        matcher = Achievement.Matcher.KeywordList(listOf("⣿⣿⣿⠟⢹⣶⣶⣝⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "monkaLaugh",
        description = "Make Markov mention Hitler",
        matcher = Achievement.Matcher.KeywordList(listOf("hitler"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "The Witch",
        description = "Make Markov mention N̵̨̮͉̟̣̥̰̫̹̣̱̩̭̦̿̅̆̈́̌́̍̾́͆͒̄̒̚̚i̷̧̩̯͖̖͎̱̹̿̓̈́̑̐͗͂̓́̇̕͠ņ̶̡̠̞̖͓͓̹̳̦̞͍̈̔̈́̉̀́̓͗̕͜a̴̡̨̫̤͈̳̜̺̩̗̗̖̼̖͔̍̅͑͋̈́͑̾̾̽̍̈́̚",
        matcher = Achievement.Matcher.KeywordList(listOf("nina "))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "FeelsBirthdayMan",
        description = "Make Markov wish you a happy birthday",
        matcher = Achievement.Matcher.KeywordList(listOf("happy birthday"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenInsane",
        description = "Make Markov mention MADMONQ®",
        matcher = Achievement.Matcher.KeywordList(listOf("madmonq"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "Okayeg",
        description = "Make Markov talk about eggs",
        matcher = Achievement.Matcher.KeywordList(listOf("🥚", "eg ", "egg ", "eggs "))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "MODS",
        description = "Make Markov says that he is chicken",
        matcher = Achievement.Matcher.KeywordList(listOf("i am chicken", "im chicken", "i'm chicken"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "forsenPls",
        description = "Make Markov rant about your Dota skills",
        matcher = Achievement.Matcher.KeywordList(listOf("retard player", "ritard player"))
    ),
    Achievement(
        color = Color.cornflowerblue,
        icon = "forsenE.png",
        name = "ZULUL",
        description = "Make Markov mention Uganda or Uganda related topics",
        matcher = Achievement.Matcher.KeywordList(listOf("uganda", "wakaliwood", "captain alex", "tiger mafia", "bruce u", "pastor lul"))
    ),
)