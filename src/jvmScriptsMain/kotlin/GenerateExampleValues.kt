@file:OptIn(ExperimentalTime::class)

package scripts

import CommonConstants
import MarkovChain
import generateRandomReply
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import toWordParts
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main() {
    val logger = KotlinLogging.logger("GenerateExampleValues.kts")

    val json = Json {
        ignoreUnknownKeys = true
    }

    val markovChain = MarkovChain<String?>(CommonConstants.consideredValuesForGeneration) { it?.trim() }

    logger.info("Building Markov chain...")

    val chainBuildTime = measureTime {
        val messages = json.decodeFromString<List<String>>(File("data.json").readText())
        markovChain.addData(messages.map { it.toWordParts() })
        println(messages.size)
    }

    logger.info("Building the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")

    repeat(100) {
        logger.info(markovChain.generateRandomReply())
    }
}