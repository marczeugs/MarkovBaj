
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.Message
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger("MarkovBaj:Discord")

suspend fun setupDiscordBot(markovChain: MarkovChain<String?>) {
    val perGuildMessageDebounceFlow = mutableMapOf</* guildId */ Snowflake, MutableSharedFlow<Message>>()

    Kord(RuntimeVariables.Discord.botToken).apply {
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
                && RuntimeVariables.Discord.actuallySendReplies
                && (message.author?.id ?: message.data.author.id) != selfId
            ) {
                guildId?.let { guildId ->
                    val flow = perGuildMessageDebounceFlow.getOrPut(guildId) {
                        MutableSharedFlow<Message>(replay = 1).apply {
                            launch {
                                @OptIn(FlowPreview::class)
                                debounce(2.seconds).collect {
                                    val response = markovChain.tryGeneratingReplyFromWords(it.content.toWordParts(), platform = "Discord")
                                        ?: markovChain.generateRandomReply()

                                    it.channel.createMessage(response)
                                }
                            }
                        }
                    }

                    flow.emit(message)
                }
            }
        }

        on<ChatInputCommandInteractionCreateEvent> {
            if (interaction.data.applicationId != selfId || interaction.data.data.name.value != "markov" || !RuntimeVariables.Discord.actuallySendReplies) {
                return@on
            }

            val response = interaction.command.strings["query"]?.let { markovChain.tryGeneratingReplyFromWords(it.toWordParts(), platform = "Discord") }
                ?: markovChain.generateRandomReply()

            interaction.respondPublic {
                content = "Input: ${interaction.command.strings["query"] ?: "-"}\n\nOutput: ${response.take(1000)}"
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