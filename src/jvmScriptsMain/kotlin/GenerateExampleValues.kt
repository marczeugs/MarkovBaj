@file:OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)

package scripts

import Hash
import MarkovChain
import Word
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main() {
    val logger = KotlinLogging.logger("GenerateExampleValues.kts")

    val json = Json {
        ignoreUnknownKeys = true
    }

    val markovChain = json.decodeFromStream<MarkovChain<Word, Hash>>(MarkovChain::class.java.getResourceAsStream("data.json")!!)

    val sequenceGenerationTime = measureTime {
        repeat(100) {
            logger.info(markovChain.generateSequence().joinToString(" ") { it.word })
        }
    }

    logger.info("Generating 100 sequences took ${sequenceGenerationTime.toDouble(DurationUnit.SECONDS)}s.")
}