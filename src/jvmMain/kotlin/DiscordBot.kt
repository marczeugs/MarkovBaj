
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger("MarkovBaj:Discord")

suspend fun setupDiscordBot(markovChain: MarkovChain<String>) {
    Kord(RuntimeVariables.discordToken).apply {
        on<ReadyEvent> {
            logger.info { "Discord bot ready." }

            if (getGlobalApplicationCommands().firstOrNull { it.name == "markov" } == null) {
                createGlobalChatInputCommand("markov", "Make Markov respond to your query.") {
                    string("query", "Input")
                }
            }
        }

        on<GuildCreateEvent> {
            logger.info { "Added to server \"${guild.name}\" (${guild.id})" }
        }

        on<MessageCreateEvent> {
            if (
                (selfId in message.mentionedUserIds || CommonConstants.triggerKeyword in message.content.lowercase())
                && RuntimeVariables.discordActuallySendReplies
                && (message.author?.id ?: message.data.author.id) != selfId
            ) {
                val response = tryGeneratingReplyFromWords(markovChain, message.content.split(CommonConstants.wordSeparatorRegex), platform = "Discord")
                    ?: markovChain.generateSequence().joinToString(" ")

                message.channel.createMessage(response.take(2000))
            }
        }

        on<ChatInputCommandInteractionCreateEvent> {
            if (interaction.data.applicationId != selfId || interaction.data.data.name.value != "markov" || !RuntimeVariables.discordActuallySendReplies) {
                return@on
            }

            val response = interaction.command.strings["query"]?.let { tryGeneratingReplyFromWords(markovChain, it.split(CommonConstants.wordSeparatorRegex), platform = "Discord") }
                ?: markovChain.generateSequence().joinToString(" ")

            interaction.respondPublic {
                content = "Input: ${interaction.command.strings["query"] ?: "-"}\n\nOutput: ${response.take(2000)}"
            }
        }

        launch {
            login {
                @OptIn(PrivilegedIntent::class)
                intents = Intents.nonPrivileged + Intent.MessageContent
            }
        }
    }
}