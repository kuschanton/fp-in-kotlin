package fp.kotlin.monad.free

import arrow.core.None
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.orElse
import arrow.core.some
import arrow.mtl.State

fun Expr.interpretFold(input: String): Boolean =
    fold(
        lit = { input.startsWith(it) },
        and = Boolean::and,
        many = Boolean::not,
        or = Boolean::or
    )

sealed class Expr {
    fun <T> fold(
        lit: (String) -> T,
        and: (T, T) -> T,
        many: (T) -> T,
        or: (T, T) -> T
    ): T {
        fun go(expr: Expr): T = when (expr) {
            is Lit -> lit(expr.s)
            is And -> and(go(expr.a), go(expr.b))
            is Many -> many(go(expr.e))
            is Or -> or(go(expr.a), go(expr.b))
        }
        return go(this)
    }
}

data class Lit(val s: String) : Expr()
data class And(val a: Expr, val b: Expr) : Expr()
data class Many(val e: Expr) : Expr()
data class Or(val a: Expr, val b: Expr) : Expr()

fun many(f: () -> Expr): Many = Many(f())

infix fun String.and(f: () -> Expr): And =
    And(Lit(this), f())

infix fun String.and(s: String): And =
    And(Lit(this), Lit(s))

infix fun Expr.and(f: () -> Expr): And =
    And(this, f())

infix fun Expr.and(s: String): And =
    And(this, Lit(s))

infix fun String.or(f: () -> Expr): Or =
    Or(Lit(this), f())

infix fun String.or(s: String): Or =
    Or(Lit(this), Lit(s))

infix fun Expr.or(f: () -> Expr): Or =
    Or(this, f())

infix fun Expr.or(s: String): Or =
    Or(this, Lit(s))


fun Expr.interpretOption(input: String): Option<String> =
    when (this) {
        is Lit -> input.startsWith(s)
            .asOption { input.drop(s.length) }
        is And -> a.interpretOption(input)
            .flatMap { b.interpretOption(it) }
        is Or -> a.interpretOption(input)
            .orElse { b.interpretOption(input) }
        is Many -> e.interpretOption(input)
            .fold(
                ifEmpty = { "".some() },
                ifSome = { interpretOption(it) }
            )
//            .flatMap {
//                if (it.isEmpty()) "".some()
//                else interpretOption(it)
//            }
    }

fun Expr.interpretBoolean(input: String): Tuple2<String, Boolean> =
    when (this) {
        is Lit ->
            if (input.startsWith(s)) Tuple2(input.drop(s.length), true)
            else Tuple2(s, false)
        is And -> {
            val (sx, bx) = a.interpretBoolean(input)
            if (bx) b.interpretBoolean(sx)
            else Tuple2(input, false)
        }
        is Or -> {
            val (sx, bx) = a.interpretBoolean(input)
            if (bx) Tuple2(sx, true)
            else b.interpretBoolean(input)
        }
        is Many -> {
            val (sx, bx) = e.interpretBoolean(input)
            if (bx) interpretBoolean(sx)
            else Tuple2(input, true)
//            when {
//                !bx -> Tuple2(sx, false)
//                sx.isEmpty() -> Tuple2(sx, true)
//                else -> interpretBoolean(sx)
//            }
        }
    }

fun Expr.toStateMachine(): State<String, Boolean> = State(this::interpretBoolean)

fun <T> Boolean.asOption(f: () -> T): Option<T> =
    if (this) f().some()
    else None