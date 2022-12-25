import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private val mouthImages = 6

@Composable
fun Markov(
    talking: Boolean
) {
    var mouthImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(talking) {
        while (talking) {
            mouthImageIndex = generateSequence { Random.nextInt(mouthImages) }.first { it != mouthImageIndex }
            delay(Random.nextDouble(0.1, 0.2).seconds)
        }
    }

    Div(attrs = { classes(Styles.markovContainer) }) {
        Img(
            src = "markov_body.png",
            attrs = { classes(Styles.markovBodyPart) }
        )

        Img(
            src = "markov_head.png",
            attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart) }
        )

        for (i in 0 until mouthImages) {
            Img(
                src = "markov_mouth_talking_${mouthImageIndex}.png",
                attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (i == mouthImageIndex && talking) arrayOf() else arrayOf(Styles.hidden))) }
            )
        }

        Img(
            src = "markov_mouth_idle.png",
            attrs = { classes(Styles.markovHeadPart, Styles.markovBodyPart, *(if (!talking) arrayOf() else arrayOf(Styles.hidden))) }
        )
    }
}