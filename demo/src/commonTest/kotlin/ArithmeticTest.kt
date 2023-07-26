package me.alllex.parsus.demo

import me.alllex.parsus.demo.math.Expr.*
import me.alllex.parsus.demo.math.ExprCalculator
import me.alllex.parsus.demo.math.ExprParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ArithmeticTest {

    @Test
    fun exprParsing() {
        assertEquals(
            actual = ExprParser.parseOrThrow("-1 + 2 * 3 + 4 ^ 5 + 6 * (7 - 8)"),
            expected = Add(
                Add(
                    Add(
                        Neg(Con(1)),
                        Mul(Con(2), Con(3))
                    ),
                    Pow(Con(4), Con(5))
                ),
                Mul(Con(6), Sub(Con(7), Con(8)))
            )
        )
    }

    @Test
    fun exprCalculator() {
        assertEquals(
            actual = ExprCalculator.parseOrThrow("-1 + 2 * 3 + 4 ^ 5 + 6 * (7 - 8)"),
            expected = 1023
        )
    }
}
