import org.w3c.files.Blob
import kotlin.js.Promise

external object speechSynthesis {
    fun speak(utterance: SpeechSynthesisUtterance)
}

external class SpeechSynthesisUtterance(text: String)

external class Audio(path: String) {
    fun play(): Promise<Unit>
    var onended: () -> Unit
}

external object URL {
    fun createObjectURL(blob: Blob): String
}

external object Object {
    fun <K, V> fromEntries(iterable: Array<Array<Any>>): Map<K, V>
    fun entries(jsObject: dynamic): Array<Any>
}