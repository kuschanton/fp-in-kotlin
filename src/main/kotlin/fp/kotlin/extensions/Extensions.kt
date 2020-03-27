package fp.kotlin.extensions


fun <T> List<T>.splitAt(index: Int): Pair<List<T>, List<T>> = when {
    index <= 0 -> emptyList<T>() to this
    index > size - 1 -> this to emptyList()
    else -> this.subList(0, index) to this.subList(index, size)
}