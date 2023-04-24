@file:OptIn(ExperimentalTime::class)

package scripts

import CommonConstants
import MarkovChain
import generateRandomReply
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
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
        val messages = json.decodeFromStream<List<String>>(File("data.json").inputStream())
        val messageData = messages.map { it.toWordParts() }

        markovChain.addData(
            messageData,
            messageData.flatMap { values ->
                listOf(
                    values.take(CommonConstants.consideredValuesForGeneration),
                    values.drop(1).take(CommonConstants.consideredValuesForGeneration)
                )
            }
        )
    }

    logger.info("Building the chain took ${chainBuildTime.toDouble(DurationUnit.SECONDS)}s.")

    repeat(100) {
        logger.info(markovChain.generateRandomReply())
    }
}