
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Word(val word: String) {
    companion object {
        private val lowercaseWordToHashMap = mutableMapOf<String, Hash>()
    }

    val lowercaseHash get() = lowercaseWordToHashMap.getOrPut(word.lowercase()) { word.lowercase().hashCode() }
}

typealias Hash = Int

@Serializable
data class MarkovWordChain(
    override val consideredValuesForGeneration: Int,
    val wordHashMap: Map<Hash, Word>,
    override val chainStarts: WeightedSet<List<Hash>>,
    override val followingValues: Map<List<Hash>, WeightedSet<Hash>>
) : MarkovChain<Word, Hash> {
    override fun hashTransformation(value: Word) = value.lowercaseHash
    override fun contentTransformation(hash: Hash) = wordHashMap[hash]!!
}