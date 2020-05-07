package fp.kotlin.parallelism

import arrow.core.extensions.list.foldable.firstOption
import arrow.core.getOrElse
import arrow.core.orElse
import fp.kotlin.extensions.splitAt
import java.util.concurrent.TimeUnit

//// 7.1
//fun sum71(ints: List<Int>): Int =
//    if (ints.size <= 1)
//        ints.firstOption().getOrElse { 0 }
//    else {
//        val (l, r) = ints.splitAt(ints.size / 2)
//        sum71(l) + sum71(r)
//    }
//
//// 7.2
//class Par<A>(val get: A)
//
//fun <A> unit(a: () -> A): Par<A> = Par(a())
//
//fun <A> get(a: Par<A>): A = a.get
//
//// 7.3
//fun sum73(ints: List<Int>): Int =
//    if (ints.size <= 1)
//        ints.firstOption().getOrElse { 0 }
//    else {
//        val (l, r) = ints.splitAt(ints.size / 2)
//        val sumL: Par<Int> = unit { sum73(l) }
//        val sumR: Par<Int> = unit { sum73(r) }
//        sumL.get + sumR.get
//    }
//
//fun sum2(ints: List<Int>): Par<Int> =
//    if (ints.size <= 1)
//        unit { ints.firstOption().getOrElse { 0 } }
//    else {
//        val (l, r) = ints.splitAt(ints.size / 2)
//        map2(sum2(l), sum2(r)) { lx: Int, rx: Int -> lx + rx }
//    }
//
//
//fun <T> map2(a: Par<T>, b: Par<T>, f: (T, T) -> T): Par<T> {
//    TODO("Not yet implemented")
//}

//fun <A> fork(a: () -> Par<A>): Par<A> = TODO()
//
//// 7.6
//fun <A> unit(a: A): Par<A> = Par(a)
//
//fun <A> lazyUnit(a: () -> A): Par<A> =
//    fork { unit(a()) }
//
//// 7.7
interface Callable<A> {
    fun call(): A
}

interface Future<A> {
    fun get(): A
    fun get(timeout: Long, timeUnit: TimeUnit): A
    fun cancel(evenIfRunning: Boolean): Boolean
    fun isDone(): Boolean
    fun isCancelled(): Boolean
}

interface ExecutorService {
    fun <A> submit(c: Callable<A>): Future<A>
}

typealias Par<A> = (ExecutorService) -> Future<A>

fun <A> run(es: ExecutorService, a: Par<A>): Future<A> = a(es)

object Pars {
    fun <A> unit(a: A): Par<A> = { _: ExecutorService -> UnitFuture(a) }

    data class UnitFuture<A>(val a: A) : Future<A> {
        override fun get(): A = a
        override fun get(timeout: Long, timeUnit: TimeUnit): A = a
        override fun cancel(evenIfRunning: Boolean): Boolean = false
        override fun isDone(): Boolean = true
        override fun isCancelled(): Boolean = false
    }

    fun <A, B> map(pa: Par<A>, f: (A) -> B): Par<B> = { es ->
        UnitFuture(f(pa(es).get()))
    }

    fun <A : Comparable<A>> sortPar(parList: Par<List<A>>): Par<List<A>> = map(parList) { it.sorted() }

    fun <A, B> parMap(
        ps: List<A>,
        f: (A) -> B
    ): Par<List<B>> = fork {
        val fbs: List<Par<B>> = ps.map(asyncF(f))
        sequence(fbs)
    }

    fun <A> sequence(ps: List<Par<A>>): Par<List<A>> = when (ps) {
        Nil -> unit(Nil)
        else -> map2(ps.head, sequence(ps.tail)) { a, b ->
            listOf(a) + b
        }
    }

    fun <A> sequence1(ps: List<Par<A>>): Par<List<A>> = when {
        ps.isEmpty() -> unit(Nil)
        ps.size == 1 -> map(ps.head) { listOf(it) }
        else -> {
            val (l, r) = ps.splitAt(ps.size / 2)
            map2(sequence1(l), sequence1(r)) { la, lb -> la + lb }
        }
    }

    fun <A> parFilter(sa: List<A>, f: (A) -> Boolean): Par<List<A>> {
        val pars = sa.map { lazyUnit { it } }
        return map(sequence(pars)) { la ->
            la.flatMap { a ->
                if (f(a)) listOf(a) else emptyList()
            }
        }
    }

    fun <A, B, C> map2(
        a: Par<A>,
        b: Par<B>,
        f: (A, B) -> C
    ): Par<C> =
        { es: ExecutorService ->
            val af: Future<A> = a(es)
            val bf: Future<B> = b(es)
            UnitFuture(f(af.get(), bf.get()))
        }

    fun <A> fork(
        a: () -> Par<A>
    ): Par<A> =
        { es: ExecutorService ->
            es.submit(
                object : Callable<A> {
                    override fun call() = a()(es).get()
                }
            )
        }

    fun <A, B> asyncF(f: (A) -> B): (A) -> Par<B> = {
        lazyUnit { f(it) }
    }

    fun <A> lazyUnit(a: () -> A): Par<A> =
        fork { unit(a()) }
}

val <T> List<T>.head: T
    get() = first()

val <T> List<T>.tail: List<T>
    get() = drop(1)

val Nil = listOf<Nothing>()

fun sum2(ints: List<Int>): Par<Int> =
    if (ints.size <= 1)
        Pars.unit(ints.firstOption().getOrElse { 0 })
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        Pars.map2(sum2(l), sum2(r)) { lx: Int, rx: Int -> lx + rx }
    }

fun <A : Comparable<A>> parMax(input: List<A>): Par<A?> =
    if (input.size <= 1)
        Pars.unit(input.firstOption().orNull())
    else {
        val (l, r) = input.splitAt(input.size / 2)
        Pars.map2(parMax(l), parMax(r)) { lx: A?, rx: A? ->
            when {
                lx != null && rx != null -> maxOf(lx, rx)
                lx != null -> lx
                rx != null -> rx
                else -> null
            }
        }
    }

val asd: String = ""

fun count(input: List<String>): Par<Int> =
    if (input.size <= 1)
        Pars.unit(input.firstOption().map { it.length }.getOrElse { 0 })
    else {
        val (l, r) = input.splitAt(input.size / 2)
        Pars.map2(count(l), count(r)) { lx: Int, rx: Int -> lx + rx }
    }

fun main() {
//    val par = parMax(listOf(1, 2, 3, 4, 5, 6, 0))
//    val par = parMax(emptyList<Int>())
//    val par = parMax(listOf(2,2))
    val par = count(
        listOf(
            "123",
            "4567890"
        )
    )
    val future = par(object : ExecutorService {
        override fun <A> submit(c: Callable<A>): Future<A> =
            Pars.UnitFuture(c.call())
    })

    println(future.get())
}