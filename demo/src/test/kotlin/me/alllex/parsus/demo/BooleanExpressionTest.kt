package me.alllex.parsus.demo

import org.junit.Test
import kotlin.test.assertEquals
import me.alllex.parsus.demo.BooleanExpression.*

class BooleanExpressionTest {

    @Test
    fun `Boolean Expression parsing`() {
        assertEquals(
            actual = BooleanGrammar.parseEntireOrThrow("a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"),
            expected = Impl(
                Or(
                    Or(
                        And(
                            Var("a"),
                            Impl(Var("b1"), Var("c1"))
                        ),
                        And(Var("a1"), Not(Var("b")))
                    ),
                    Not(Impl(Var("a1"), Var("a2")))
                ),
                Var("a")
            )
        )
    }
}
