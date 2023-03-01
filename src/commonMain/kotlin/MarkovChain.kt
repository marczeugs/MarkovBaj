class MarkovChain<T>(private val consideredValuesForGeneration: Int, private val inputValueMapperFunction: (T) -> T = { it }) {
    val chainStarts = WeightedSet<List<T>>()
    private val followingValues = mutableMapOf<List<T>, WeightedSet<T?>>()

    fun addData(data: List<List<T>>, chainStarts: List<List<T>> = data.map { values -> values.take(consideredValuesForGeneration).map { inputValueMapperFunction(it) } }) {
        this.chainStarts.addData(chainStarts)

        data.forEach { sequence ->
            (sequence + listOf(null)).windowed(consideredValuesForGeneration + 1).forEach { values ->
                // Cast is always safe because sequence ending marker (null) is only added at the end and always dropped by `dropLast(1)`
                @Suppress("UNCHECKED_CAST")
                val consideredValues = values.dropLast(1).map { inputValueMapperFunction(it as T) }

                val generatedValue = values.takeLast(1)

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

        for (index in start.size..<maxLength) {
            followingValues[generatedValues.slice((index - consideredValuesForGeneration)..<index).map { inputValueMapperFunction(it) }]?.randomValue()?.let {
                generatedValues.add(it)
            } ?: break
        }

        return generatedValues
    }
}