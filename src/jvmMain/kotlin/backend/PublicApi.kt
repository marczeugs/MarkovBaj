package backend

import MarkovChain
import RuntimeVariables
import generateRandomReply
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import toWordParts
import tryGeneratingReplyFromWords

suspend fun PipelineContext<Unit, ApplicationCall>.apiQuery(queryInput: Routes.Api.Query, markovChain: MarkovChain<String?>) {
    if (RuntimeVariables.Backend.checkedReferrer != null && context.request.header("Referer")?.startsWith(RuntimeVariables.Backend.checkedReferrer) != true) {
        logger.warn { "Rejected Markov chain query request from ${context.request.header("X-Real-Ip")} because of invalid referrer, input has length ${queryInput.input?.length}, starts with: \"${queryInput.input?.take(200)}\"" }
        call.respondText("This API is not public. Please refrain from using it from external sources.", status = HttpStatusCode.Forbidden)
        return
    }

    val query = queryInput.input

    if (query != null && query.length > 500) {
        logger.warn { "Rejected Markov chain query request from ${context.request.header("X-Real-Ip")} because of invalid query, input has length ${queryInput.input.length}, starts with: \"${queryInput.input.take(200)}\"" }
        call.respondText("Input too long.", status = HttpStatusCode.BadRequest)
        return
    }

    logger.info { "Serving Markov chain query request from ${context.request.header("X-Real-Ip")}, input has length ${queryInput.input?.length ?: 0}, starts with: \"${queryInput.input?.take(200)}\"" }

    val response = if (Math.random() > RuntimeVariables.Common.unrelatedAnswerChance) {
        query?.let { markovChain.tryGeneratingReplyFromWords(it.toWordParts(), "Backend") }
    } else {
        null
    } ?: run {
        markovChain.generateRandomReply()
    }.take(500)

    call.respondText(response)
}