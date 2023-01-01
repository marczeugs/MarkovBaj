
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement
import kotlin.math.absoluteValue
import kotlin.random.Random

class ScrollHandler {
    private val generatedElementIdentifier = "scroll-handler-${Random.nextInt().absoluteValue}"
    private var installed = false

    fun AttrsScope<*>.install() {
        classes(generatedElementIdentifier)
        installed = true
    }

    fun scrollBy(x: Double, y: Double) {
        check(installed) { "Not installed in a component." }
        (document.querySelector(".$generatedElementIdentifier")!! as HTMLElement).scrollBy(x, y)
    }
}