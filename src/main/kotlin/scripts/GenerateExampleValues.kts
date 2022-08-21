@file:OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)

package scripts

import MarkovChain
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


val logger = KotlinLogging.logger("GenerateExampleValues.kts")

val json = Json {
    ignoreUnknownKeys = true
}

val markovChain = MarkovChain<String>(Constants.markovChainGenerationValues)

logger.info("Building Markov chain...")

val chainBuildTime = measureTime {
    val messages = json.decodeFromStream<List<String>>(MarkovChain::class.java.getResourceAsStream("data.json")!!)
    markovChain.addData(messages.map { message -> message.split(Constants.wordSeparatorRegex) })
    println(messages.size)
}

logger.info("Building the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")

repeat(100) {
    logger.info(markovChain.generateSequence().joinToString(" "))
}