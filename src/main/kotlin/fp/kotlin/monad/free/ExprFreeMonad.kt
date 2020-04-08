//package fp.kotlin.monad.free
//
//import arrow.core.None
//import arrow.core.Option
//import arrow.core.orElse
//import arrow.core.some
//import arrow.higherkind
//import arrow.Kind
//
//@higherkind
//sealed class Expr<Kind<F, A>> : ExprOf<A>
//
//data class Lit(val s: String) : Expr()
//data class And(val a: Expr, val b: Expr) : Expr()
//data class Many(val e: Expr) : Expr()
//data class Or(val a: Expr, val b: Expr) : Expr()
//
//
//@higherkind
//sealed class Free<A> : FreeOf<A> {
//
//    data class
//
//}