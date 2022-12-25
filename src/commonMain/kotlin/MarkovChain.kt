sealed interface MarkovChain<ContentType, HashType> {
    val consideredValuesForGeneration: Int
    val chainStarts: WeightedSet<List<HashType>>
    val followingValues: Map<List<HashType>, WeightedSet<HashType>>

    fun hashTransformation(value: ContentType): HashType
    fun contentTransformation(hash: HashType): ContentType

    fun generateSequence(
        start: List<ContentType> = chainStarts.randomValue().map { contentTransformation(it) },
        maxLength: Int = 100.coerceAtLeast(consideredValuesForGeneration)
    ): List<ContentType> {
        require(maxLength >= consideredValuesForGeneration) {
            "Max length must be at least as large as the number of considered values. Is ${maxLength}, should be >= $consideredValuesForGeneration"
        }

        if (start.size < consideredValuesForGeneration) {
            return start
        }

        val generatedValues = start.toMutableList()
        val generatedHashedValues = start.map { hashTransformation(it) }.toMutableList()

        for (index in start.size until maxLength) {
            followingValues[generatedHashedValues.slice(index - consideredValuesForGeneration until index)]?.randomValue()?.let {
                generatedValues.add(contentTransformation(it))
                generatedHashedValues.add(it)
            } ?: break
        }

        return generatedValues
    }
}