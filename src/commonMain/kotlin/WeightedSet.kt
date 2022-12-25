
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
class WeightedSet<T>() {
    val weightMap = mutableMapOf<T, Int>()

    constructor(initialWeightMap: Map<T, Int>) : this() {
        weightMap.putAll(initialWeightMap)
    }

    private var weightSum = weightMap.values.sum()

    fun addData(data: Collection<T>) {
        data.forEach { entry ->
            weightMap[entry] = weightMap[entry]?.let { it + 1 } ?: 1
        }

        weightSum += data.size
    }

    fun randomValue(): T {
        val targetWeight = Random.nextInt(weightSum)
        val (matchingValue, _) = weightMap.entries
            .runningFold(@Suppress("UNCHECKED_CAST") (null as T) to 0) { (_, lastWeight), (nextValue, nextWeight) ->
                nextValue to lastWeight + nextWeight
            }
            .first { (_, weightSum) -> targetWeight < weightSum }

        return matchingValue
    }
}