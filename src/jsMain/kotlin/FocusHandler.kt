
import kotlinx.browser.document
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.w3c.dom.HTMLElement
import kotlin.math.absoluteValue
import kotlin.random.Random

class FocusHandler {
    private val generatedElementIdentifier = "focus-handler-${Random.nextInt().absoluteValue}"
    private var installed = false

    fun InputAttrsScope<String>.install() {
        classes(generatedElementIdentifier)
        installed = true
    }

    fun focus() {
        check(installed) { "Not installed in a component." }
        (document.querySelector(".$generatedElementIdentifier")!! as HTMLElement).focus()
    }
}