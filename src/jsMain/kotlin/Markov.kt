import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private const val MOUTH_IMAGE_COUNT = 6

@Composable
fun Markov(
    talking: Boolean,
    muted: Boolean
) {
    var mouthImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(talking) {
        while (talking) {
            mouthImageIndex = generateSequence { Random.nextInt(MOUTH_IMAGE_COUNT) }.first { it != mouthImageIndex }
            delay(Random.nextDouble(0.1, 0.2).seconds)
        }
    }

    Div(attrs = { classes(Styles.markovContainer) }) {
        Img(
            src = "markov_body.png",
            attrs = { classes(Styles.markovBodyPart) }
        )

        if (!muted) {
            Img(
                src = "markov_head.png",
                attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart) }
            )

            for (i in 0 until MOUTH_IMAGE_COUNT) {
                Img(
                    src = "markov_mouth_talking_${mouthImageIndex}.png",
                    attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (i == mouthImageIndex && talking) arrayOf() else arrayOf(Styles.hidden))) }
                )
            }

            Img(
                src = "markov_mouth_idle.png",
                attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (!talking) arrayOf() else arrayOf(Styles.hidden))) }
            )
        } else {
            Img(
                src = "markov_head_tape.png",
                attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart) }
            )
        }
    }
}