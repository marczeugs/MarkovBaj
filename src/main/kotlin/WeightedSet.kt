import kotlin.random.Random

class WeightedSet<T> {
    val weightMap = mutableMapOf<T, Int>()
    private var weightSum = 0

    fun addData(data: Collection<T>) {
        data.forEach { entry ->
            weightMap.merge(entry, 1, Int::plus)
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