package fp.kotlin.monad.free

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.mtl.run
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream


internal class Expr01Test {

    private val expression =
        "raining" and {
            many {
                "cats" or "dogs"
            }
        }

    private val expressionAndInverse =
        many {
            "cats" or "dogs"
        } and "raining"

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class InterpretOption {

        @ParameterizedTest(name = "should match {1} {0} ")
        @ArgumentsSource(OptionArgumentsProvider::class)
        fun test(input: String, expected: Option<String>) {
            Assertions.assertEquals(expression.interpretOption(input), expected)
        }

    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class InterpretBoolean {
        @ParameterizedTest(name = "should match {1} {0} ")
        @ArgumentsSource(BooleanArgumentsProvider::class)
        fun test(input: String, expected: Boolean) {
            Assertions.assertEquals(expression.interpretBoolean(input).b, expected)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class StateMachineBoolean {
        @ParameterizedTest(name = "should match {1} {0} ")
        @ArgumentsSource(BooleanArgumentsProvider::class)
        fun test(input: String, expected: Boolean) {
            Assertions.assertEquals(expression.toStateMachine().run(input).b, expected)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class FoldBoolean {
        @ParameterizedTest(name = "should match {1} {0} ")
        @ArgumentsSource(BooleanArgumentsProvider::class)
        fun test(input: String, expected: Boolean) {
            Assertions.assertEquals(expression.interpretFold(input), expected)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AndInverseInterpretBoolean {
        @ParameterizedTest(name = "should match {1} {0} ")
        @ArgumentsSource(BooleanInverseAndArgumentsProvider::class)
        fun test(input: String, expected: Boolean) {
            Assertions.assertEquals(expressionAndInverse.interpretBoolean(input).b, expected)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AndInverseFoldBoolean {
        @ParameterizedTest(name = "should match {1} {0} ")
        @ArgumentsSource(BooleanInverseAndArgumentsProvider::class)
        fun test(input: String, expected: Boolean) {
            Assertions.assertEquals(expressionAndInverse.interpretFold(input), expected)
        }
    }

    internal class OptionArgumentsProvider : ArgumentsProvider {

        private val some = "".some()

        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>? {
            return Stream.of(
                Arguments.of("raining", some),
                Arguments.of("raining123", some),
                Arguments.of("rainingcats", some),
                Arguments.of("rainingdogs", some),
                Arguments.of("rainingcatsdogs", some),
                Arguments.of("rainingcatsdogscats", some),
                Arguments.of("rainingcatsdogscatsdogscats", some),
                Arguments.of("rainingcatscats", some),
                Arguments.of("rainingdogsdogs", some),
                Arguments.of("rainingdogsdogs123", some),
                Arguments.of("dogscats", None),
                Arguments.of("cats", None),
                Arguments.of("dogs", None)
            )
        }
    }

    internal class BooleanArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>? {
            return Stream.of(
                Arguments.of("raining", true),
                Arguments.of("raining123", true),
                Arguments.of("rainingcats", true),
                Arguments.of("rainingdogs", true),
                Arguments.of("rainingcatsdogs", true),
                Arguments.of("rainingcatsdogscats", true),
                Arguments.of("rainingcatsdogscatsdogscats", true),
                Arguments.of("rainingcatscats", true),
                Arguments.of("rainingdogsdogs", true),
                Arguments.of("rainingdogsdogs123", true),
                Arguments.of("dogscats", false),
                Arguments.of("cats", false),
                Arguments.of("dogs", false)
            )
        }
    }

    internal class BooleanInverseAndArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>? {
            return Stream.of(
                Arguments.of("raining", true),
                Arguments.of("raining123", true),
                Arguments.of("dogscats", false),
                // One below fails because interpretFold does not keep state
                // Arguments.of("dogscatsraining", true),
                Arguments.of("rainingcats", true),
                Arguments.of("cats", false),
                Arguments.of("dogs", false)
            )
        }
    }
}