package fp.kotlin.extensions

import io.kotlintest.properties.Gen
import io.kotlintest.specs.StringSpec
import io.kotlintest.properties.forAll


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
                val index = Gen.zeroOrNegative().generate()
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
                val index = Gen.choose(input.size, Int.MAX_VALUE).generate()
                val (first, _) = input.splitAt(index)
                first == input
            }
        }

        "split at more than size produces empty second" {
            forAll(gen) { (input, _, _) ->
                val index = Gen.choose(input.size, Int.MAX_VALUE).generate()
                val (_, second) = input.splitAt(index)
                second.isEmpty()
            }
        }
    }

    internal class SplitAtGen<T>(private val gen: Gen<T>) : Gen<Triple<List<T>, List<T>, List<T>>> {
        override fun generate(): Triple<List<T>, List<T>, List<T>> {
            val input = Gen.list(gen).generate()
            val index = Gen.choose(0, input.size).generate()
            val (first, second) = input.splitAt(index)
            return Triple(input, first, second)
        }
    }

    companion object {
        fun Gen.Companion.zeroOrNegative() = this.choose(Int.MIN_VALUE, 0)
    }
}