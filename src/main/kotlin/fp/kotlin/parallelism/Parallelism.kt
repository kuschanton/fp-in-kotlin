package fp.kotlin.parallelism

import arrow.core.extensions.list.foldable.firstOption
import arrow.core.getOrElse
import fp.kotlin.extensions.splitAt

// 7.1
fun sum71(ints: List<Int>): Int =
    if (ints.size <= 1)
        ints.firstOption().getOrElse { 0 }
else {
    val (l, r) = ints.splitAt(ints.size / 2)
    sum(l) + sum(r)
}

// 7.2
class Par<A>(val get: A)

fun <A> unit(a: () -> A): Par<A> = Par(a())

fun <A> get(a: Par<A>): A = a.get

// 7.3
fun sum(ints: List<Int>): Int =
    if (ints.size <= 1)
        ints.firstOption().getOrElse { 0 }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        val sumL: Par<Int> = unit { sum(l) }
        val sumR: Par<Int> = unit { sum(r) }
        sumL.get + sumR.get
    }

