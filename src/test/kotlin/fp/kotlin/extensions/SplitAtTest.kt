package fp.kotlin.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream


internal class SplitAtTest {

    @ParameterizedTest(name = "split at index {0}")
    @ArgumentsSource(SplitAtArgumentsProvider::class)
    fun test(index: Int, expected: Pair<List<Int>, List<Int>>) {
        assertEquals(input.splitAt(index), expected)
    }

    internal class SplitAtArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>? {
            return Stream.of(
                Arguments.of(-1, empty to input),
                Arguments.of(0, empty to input),
                Arguments.of(1, listOf(1) to listOf(2, 3, 4, 5)),
                Arguments.of(2, listOf(1, 2) to listOf(3, 4, 5)),
                Arguments.of(3, listOf(1, 2, 3) to listOf(4, 5)),
                Arguments.of(4, listOf(1, 2, 3, 4) to listOf(5)),
                Arguments.of(5, input to empty),
                Arguments.of(10, input to empty)
            )
        }
    }

    companion object {
        private val input = listOf(1, 2, 3, 4, 5)
        private val empty = emptyList<Int>()
    }
}