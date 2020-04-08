package fp.kotlin.monad.free

import arrow.core.None
import arrow.core.Option
import arrow.core.orElse
import arrow.core.some


sealed class ExprT<T> {
    fun <A> fold(
        lit: (T) -> A,
        and: (A, A) -> A,
        many: (A) -> A,
        or: (A, A) -> A
    ): A {
        fun go(expr: ExprT<T>): A = when (expr) {
            is LitT -> lit(expr.s)
            is AndT -> and(go(expr.a), go(expr.b))
            is ManyT -> many(go(expr.e))
            is OrT -> or(go(expr.a), go(expr.b))
        }
        return go(this)
    }
}

fun <T> manyT(f: () -> ExprT<T>): ManyT<T> = ManyT(f())

infix fun <T> T.andT(f: () -> ExprT<T>): AndT<T> =
    AndT(LitT(this), f())

infix fun <T> T.andT(s: T): AndT<T> =
    AndT(LitT(this), LitT(s))

infix fun <T> ExprT<T>.andT(f: () -> ExprT<T>): AndT<T> =
    AndT(this, f())

infix fun <T> ExprT<T>.andT(s: T): AndT<T> =
    AndT(this, LitT(s))

infix fun <T> T.orT(f: () -> ExprT<T>): OrT<T> =
    OrT(LitT(this), f())

infix fun <T> T.orT(s: T): OrT<T> =
    OrT(LitT(this), LitT(s))

infix fun <T> ExprT<T>.orT(f: () -> ExprT<T>): OrT<T> =
    OrT(this, f())

infix fun <T> ExprT<T>.orT(s: T): OrT<T> =
    OrT(this, LitT(s))

data class LitT<T>(val s: T) : ExprT<T>()
data class AndT<T>(val a: ExprT<T>, val b: ExprT<T>) : ExprT<T>()
data class ManyT<T>(val e: ExprT<T>) : ExprT<T>()
data class OrT<T>(val a: ExprT<T>, val b: ExprT<T>) : ExprT<T>()