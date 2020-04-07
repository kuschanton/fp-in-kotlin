package fp.kotlin.extensions

import io.kotlintest.properties.Gen
import io.kotlintest.specs.StringSpec
import io.kotlintest.properties.forAll
import io.kotlintest.properties.generateInfiniteSequence


internal class SplitAtPropertyBasedTest : StringSpec() {

    private val gen = Gen.oneOf(
        SplitAtGen(Gen.int()),
        SplitAtGen(Gen.string()),
        SplitAtGen(Gen.bool()),
        SplitAtGen(Gen.double())
    )

    init {
        "sum of sizes of parts should make size of input" {
            forAll(gen) { (input, first, second) ->
                input.size == first.size + second.size
            }
        }

        "set of elements in input is same as in parts" {
            forAll(gen) { (input, first, second) ->
                input.toSet() == first.toSet() + second.toSet()
            }
        }

        "split at zero or negative produces second equals to input" {
            forAll(gen) { (input, _, _) ->
                val index = Gen.zeroOrNegative().next()
                val (_, second) = input.splitAt(index)
                second == input
            }
        }

        "split at zero or negative produces empty first" {
            forAll(gen) { (input, _, _) ->
                val (first, _) = input.splitAt(0)
                first.isEmpty()
            }
        }

        "split at more than size produces first equals to input" {
            forAll(gen) { (input, _, _) ->
                val index = Gen.choose(input.size, Int.MAX_VALUE).next()
                val (first, _) = input.splitAt(index)
                first == input
            }
        }

        "split at more than size produces empty second" {
            forAll(gen) { (input, _, _) ->
                val index = Gen.choose(input.size, Int.MAX_VALUE).next()
                val (_, second) = input.splitAt(index)
                second.isEmpty()
            }
        }
    }

    internal class SplitAtGen<T>(private val gen: Gen<T>) : Gen<Triple<List<T>, List<T>, List<T>>> {

        override fun constants(): Iterable<Triple<List<T>, List<T>, List<T>>> = listOf(
            Triple(emptyList(), emptyList(), emptyList())
        )

        override fun random(): Sequence<Triple<List<T>, List<T>, List<T>>> = generateInfiniteSequence {
            val input = Gen.list(gen).next()
            val index = when (input.size) {
                0 -> 0
                else -> Gen.choose(0, input.size).next()
            }
            val (first, second) = input.splitAt(index)
            Triple(input, first, second)
        }
    }

    companion object {
        fun Gen.Companion.zeroOrNegative() = this.choose(Int.MIN_VALUE, 0)
    }
}