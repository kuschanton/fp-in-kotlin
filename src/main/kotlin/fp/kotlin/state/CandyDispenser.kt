package fp.kotlin.state

import arrow.core.Tuple2
import arrow.mtl.State
import arrow.mtl.run

sealed class Input

object Coin : Input()
object Turn : Input()

sealed class Machine {
    abstract val candies: Int
    abstract val coins: Int
}

data class LockedMachine(override val candies: Int, override val coins: Int) : Machine()
data class UnlockedMachine(override val candies: Int, override val coins: Int) : Machine()
data class EmptyMachine(override val coins: Int) : Machine() {
    override val candies = 0
}

fun main() {
    val initialState = LockedMachine(5, 10)
    val input = mutableListOf<Input>().apply {
        repeat(4) {
            add(Coin)
            add(Turn)
        }
    }.toList()
    val res = simulateMachine(input).run(initialState)
    println(res.a)
}

fun simulateMachine(inputs: List<Input>) = State<Machine, Tuple2<Int, Int>> { machine ->
    inputs.map { applyInput(it) }
            .fold(machine) { acc, next ->
                next.run(acc).a
            }
            .toResult()
}

fun applyInput(input: Input) = State<Machine, Tuple2<Int, Int>> { machine ->
    when {
        machine is LockedMachine && input is Coin -> machine.insertCoin()
        machine is UnlockedMachine && input is Turn -> machine.turnKnob()
        else -> machine
    }.toResult()
}

fun LockedMachine.insertCoin() = UnlockedMachine(candies, coins + 1)
fun UnlockedMachine.turnKnob(): Machine {
    val candiesLeft = candies - 1
    return if (candiesLeft == 0) EmptyMachine(coins)
    else LockedMachine(candiesLeft, coins)
}

fun Machine.toResult(): Tuple2<Machine, Tuple2<Int, Int>> = Tuple2(this, coinsCandyTuple())
fun Machine.coinsCandyTuple(): Tuple2<Int, Int> = Tuple2(coins, candies)