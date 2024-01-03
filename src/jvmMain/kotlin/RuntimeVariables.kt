import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@Serializable
data class TableDefinition(
    val name: String,
    val displayName: String,
    val columns: List<Column>
) {
    @Serializable
    data class Column(
        val name: String,
        val displayName: String,
        val type: Type
    ) {
        @Serializable
        enum class Type {
            @SerialName("varchar32") VarChar32,
            @SerialName("text") Text,
            @SerialName("integer") Integer,
            @SerialName("boolean") Boolean,
            @SerialName("timestamp") Timestamp,
        }
    }
}

object RuntimeVariables {
    object Common {
        val unrelatedAnswerChance = System.getenv("markovbaj_unrelatedanswerchance").toFloat()
    }

    object Reddit {
        val enabled: Boolean = System.getenv("markovbaj_reddit_enabled") == "true"
        val botUsername: String by lazy { System.getenv("markovbaj_reddit_username") }
        val botPassword: String by lazy { System.getenv("markovbaj_reddit_password") }
        val botClientId: String by lazy { System.getenv("markovbaj_reddit_clientid") }
        val botClientSecret: String by lazy { System.getenv("markovbaj_reddit_clientsecret") }
        val botAppId: String by lazy { System.getenv("markovbaj_reddit_appid") }
        val botAuthorRedditUsername: String by lazy { System.getenv("markovbaj_reddit_authorusername") }
        val actuallySendReplies by lazy { System.getenv("markovbaj_reddit_actuallysendreplies") == "true" }
        val answerMentions by lazy { System.getenv("markovbaj_reddit_answermentions") == "true" }
        val activeSubreddit: String by lazy { System.getenv("markovbaj_reddit_activesubreddit") }
        val checkInterval by lazy { System.getenv("markovbaj_reddit_checkintervalinseconds").toInt().seconds }
        val maxCommentsPerCheck by lazy { System.getenv("markovbaj_reddit_maxcommentspercheck").toInt() }
        val delayBetweenComments by lazy { System.getenv("markovbaj_reddit_delaybetweencommentsinseconds").toInt().seconds }
    }

    object Backend {
        val serverUrl: String = System.getenv("markovbaj_backend_serverurl")
        val serverPort = System.getenv("markovbaj_backend_serverport").toInt()
        val databaseUrl: String = System.getenv("markovbaj_backend_databaseurl")
        val databaseUser: String = System.getenv("markovbaj_backend_databaseuser")
        val databasePassword: String = System.getenv("markovbaj_backend_databasepassword")
        val databaseTables = Json.decodeFromString<List<TableDefinition>>(System.getenv("markovbaj_backend_databasetables"))
        val databaseTableRowLimit = System.getenv("markovbaj_backend_databasetablerowlimit").toInt()
        val redditClientId: String = System.getenv("markovbaj_backend_clientid")
        val redditClientSecret: String = System.getenv("markovbaj_backend_clientsecret")
        val permittedUsers = System.getenv("markovbaj_backend_permittedusers").split(",")
        val checkedReferrer: String? = System.getenv("markovbaj_backend_checkedreferrer")
    }

    object Discord {
        val enabled: Boolean = System.getenv("markovbaj_discord_enabled") == "true"
        val botToken: String by lazy { System.getenv("markovbaj_discord_token") }
        val actuallySendReplies by lazy { System.getenv("markovbaj_discord_actuallysendreplies") == "true" }
    }

    object Twitch {
        val enabled: Boolean = System.getenv("markovbaj_twitch_enabled") == "true"
        val botToken: String by lazy { System.getenv("markovbaj_twitch_token") }
        val botUsername: String by lazy { System.getenv("markovbaj_twitch_username") }
        val activeChannel: String by lazy { System.getenv("markovbaj_twitch_activechannel") }
        val actuallySendReplies by lazy { System.getenv("markovbaj_twitch_actuallysendreplies") == "true" }
    }
}