
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.enums.CommandPermission

fun setupTwitchBot(markovChain: MarkovChain<String>) {
    val credentials = OAuth2Credential("twitch", RuntimeVariables.Twitch.botToken)

    val twitchClient = TwitchClientBuilder.builder()
        .withEnableChat(true)
        .withChatAccount(credentials)
        .build()

    twitchClient.run {
        chat.run {
            connect()
            joinChannel(RuntimeVariables.Twitch.activeChannel)
        }
    }

    var enabled = true

    twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) {
        if (!RuntimeVariables.Twitch.actuallySendReplies || it.user.name == RuntimeVariables.Twitch.botUsername) {
            return@onEvent
        }

        if (it.message == "!toggle" && CommandPermission.MODERATOR in it.permissions) {
            enabled = !enabled
            twitchClient.chat.sendMessage(it.channel.name, "Bot is now ${if (enabled) "enabled" else "disabled"}.")
        } else if (enabled && CommonConstants.triggerKeyword in it.message.lowercase()) {
            val reply = (
                tryGeneratingReplyFromWords(markovChain, it.message.split(CommonConstants.wordSeparatorRegex), platform = "Twitch")
                    ?: markovChain.generateSequence().joinToString(" ")
            ).take(500)

            twitchClient.chat.sendMessage(it.channel.name, reply)
        }
    }
}