package me.alllex.parsus.demo

import me.alllex.parsus.demo.bool.BooleanExpression.*
import me.alllex.parsus.demo.bool.BooleanGrammar
import kotlin.test.Test
import kotlin.test.assertEquals

class BooleanExpressionTest {

    @Test
    fun boolExprParsing() {
        assertEquals(
            actual = BooleanGrammar.parseOrThrow("a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a"),
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
