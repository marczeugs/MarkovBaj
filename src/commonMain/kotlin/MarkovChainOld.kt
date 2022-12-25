class MarkovChainOld<T>(private val consideredValuesForGeneration: Int) {
    val chainStarts = WeightedSet<List<T>>()
    private val followingValues = mutableMapOf<List<T>, WeightedSet<T>>()

    fun addData(data: List<List<T>>) {
        chainStarts.addData(data.map { it.take(consideredValuesForGeneration) })

        data.forEach { sequence ->
            sequence.windowed(consideredValuesForGeneration + 1).forEach {
                val consideredValues = it.dropLast(1)
                val generatedValue = it.takeLast(1)

                followingValues.getOrPut(consideredValues) { WeightedSet() }.addData(generatedValue)
            }
        }
    }

    fun generateSequence(start: List<T> = chainStarts.randomValue(), maxLength: Int = 100.coerceAtLeast(consideredValuesForGeneration)): List<T> {
        require(maxLength >= consideredValuesForGeneration) {
            "Max length must be at least as large as the number of considered values. Is ${maxLength}, should be >= $consideredValuesForGeneration"
        }

        if (start.size < consideredValuesForGeneration) {
            return start
        }

        val generatedValues = start.toMutableList()

        for (index in start.size until maxLength) {
            followingValues[generatedValues.slice(index - consideredValuesForGeneration until index)]?.randomValue()?.let {
                generatedValues.add(it)
            } ?: break
        }

        return generatedValues
    }
}