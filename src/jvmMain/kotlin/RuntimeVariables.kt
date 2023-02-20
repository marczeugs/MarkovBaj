import kotlin.time.Duration.Companion.seconds

object RuntimeVariables {
    val unrelatedAnswerChance = System.getenv("markovbaj_unrelatedanswerchance").toFloat()

    object Reddit {
        val botUsername: String = System.getenv("markovbaj_reddit_username")
        val botPassword: String = System.getenv("markovbaj_reddit_password")
        val botClientId: String = System.getenv("markovbaj_reddit_clientid")
        val botClientSecret: String = System.getenv("markovbaj_reddit_clientsecret")
        val botAppId: String = System.getenv("markovbaj_reddit_appid")
        val botAuthorRedditUsername: String = System.getenv("markovbaj_reddit_authorusername")
        val actuallySendReplies = System.getenv("markovbaj_reddit_actuallysendreplies") == "true"
        val answerMentions = System.getenv("markovbaj_reddit_answermentions") == "true"
        val activeSubreddit: String = System.getenv("markovbaj_reddit_activesubreddit")
        val checkInterval = System.getenv("markovbaj_reddit_checkintervalinseconds").toInt().seconds
        val maxCommentsPerCheck = System.getenv("markovbaj_reddit_maxcommentspercheck").toInt()
        val delayBetweenComments = System.getenv("markovbaj_reddit_delaybetweencommentsinseconds").toInt().seconds
    }

    object Backend {
        val serverUrl: String = System.getenv("markovbaj_backend_serverurl")
        val serverPort = System.getenv("markovbaj_backend_serverport").toInt()
        val redditClientId: String = System.getenv("markovbaj_backend_clientid")
        val redditClientSecret: String = System.getenv("markovbaj_backend_clientsecret")
        val permittedUsers = System.getenv("markovbaj_backend_permittedusers").split(",")
        val checkedReferrer: String? = System.getenv("markovbaj_backend_checkedreferrer")
    }

    object Discord {
        val botToken: String = System.getenv("markovbaj_discord_token")
        val actuallySendReplies = System.getenv("markovbaj_discord_actuallysendreplies") == "true"
    }

    object Twitch {
        val botToken: String = System.getenv("markovbaj_twitch_token")
        val botUsername: String = System.getenv("markovbaj_twitch_username")
        val activeChannel: String = System.getenv("markovbaj_twitch_activechannel")
        val actuallySendReplies = System.getenv("markovbaj_twitch_actuallysendreplies") == "true"
    }
}