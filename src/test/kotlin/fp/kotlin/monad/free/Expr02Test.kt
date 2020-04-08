package fp.kotlin.monad.free

import arrow.core.identity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class Expr02Test {

    private val expression =
        true andT {
            manyT {
                false orT true
            }
        }

    @Test
    fun test() {
        Assertions.assertFalse(
            expression.fold(
                ::identity,
                Boolean::and,
                Boolean::not,
                Boolean::or
            )
        )
    }
}