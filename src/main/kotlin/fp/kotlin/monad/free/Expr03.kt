package fp.kotlin.monad.free

import arrow.Kind
import arrow.higherkind
import arrow.typeclasses.Monad

@higherkind
sealed class FreeK<F : Kind<F, A>, A> : FreeKOf<F, A> {
    fun <G> foldMap(monad: Monad<G>, f: (Kind<F, A>) -> Kind<G, A>): Kind<G, A> =
        when(this) {
            is ReturnK -> monad.just(a)
            is SuspendK -> TODO()
        }
}

data class ReturnK<F : Kind<F, A>, A>(val a: A) : FreeK<F, A>()
// TODO: Type of parameter s here is wrong
data class SuspendK<F : Kind<F, A>, A>(val s: FreeKOf<F, A>) : FreeK<F, A>()

@higherkind
sealed class BoolAlg<T> : BoolAlgOf<T>

data class LitK<T>(val b: Boolean) : BoolAlg<T>()
data class AndK<T>(val a: T, val b: T) : BoolAlg<T>()
data class OrK<T>(val a: T, val b: T) : BoolAlg<T>()
data class NotK<T>(val a: T) : BoolAlg<T>()

typealias BoolExp<A> = FreeK<ForBoolAlg, A>

