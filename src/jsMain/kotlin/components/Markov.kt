package components

import Styles
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private const val MOUTH_IMAGE_COUNT = 6
private const val NEEDLE_IMAGE_COUNT = 9

@Composable
fun Markov(
    talking: Boolean,
    muted: Boolean
) {
    var mouthImageIndex by remember { mutableStateOf(0) }
    var powerLevel by remember { mutableStateOf(0) }

    LaunchedEffect(talking) {
        while (talking) {
            mouthImageIndex = generateSequence { Random.nextInt(MOUTH_IMAGE_COUNT) }.first { it != mouthImageIndex }
            delay(Random.nextDouble(0.05, 0.15).seconds)
        }
    }

    LaunchedEffect(talking) {
        while (isActive) {
            if (talking) {
                powerLevel += when (Random.nextInt(100)) {
                    in 0 until 20 -> -1
                    in 70 until 100 -> 1
                    else -> 0
                }
            } else {
                powerLevel -= 1
            }

            if (powerLevel < 0) {
                powerLevel = 0
            } else if (powerLevel >= NEEDLE_IMAGE_COUNT) {
                powerLevel = NEEDLE_IMAGE_COUNT - 1
            }

            delay(0.1.seconds)
        }
    }

    Div(attrs = { classes(Styles.markovContainer) }) {
        Img(
            src = "img/markov/body.webp",
            attrs = { classes(Styles.markovBodyPart) }
        )

        Img(
            src = "img/markov/head.webp",
            attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (!muted) arrayOf() else arrayOf(Styles.hidden))) }
        )

        Img(
            src = "img/markov/mouth_idle.webp",
            attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (!talking) arrayOf() else arrayOf(Styles.hidden)), *(if (!muted) arrayOf() else arrayOf(Styles.hidden))) }
        )

        for (i in 0 until MOUTH_IMAGE_COUNT) {
            Img(
                src = "img/markov/mouth_talking_$i.webp",
                attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (i == mouthImageIndex && talking) arrayOf() else arrayOf(Styles.hidden)), *(if (!muted) arrayOf() else arrayOf(Styles.hidden))) }
            )
        }

        for (i in 0 until NEEDLE_IMAGE_COUNT) {
            Img(
                src = "img/markov/needle_$i.webp",
                attrs = { classes(Styles.markovBodyPart, *(if (i == powerLevel) arrayOf() else arrayOf(Styles.hidden))) }
            )
        }

        Img(
            src = "img/markov/head_tape.webp",
            attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (muted) arrayOf() else arrayOf(Styles.hidden))) }
        )
    }
}