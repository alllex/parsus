package me.alllex.parsus.demo

import me.alllex.parsus.demo.Expr.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ArithmeticTest {

    @Test
    fun `Expr parsing`() {
        assertEquals(
            actual = ExprParser.parseEntireOrThrow("-1 + 2 * 3 + 4 ^ 5 + 6 * (7 - 8)"),
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
    fun `Expr calculation`() {
        assertEquals(
            actual = ExprCalculator.parseEntireOrThrow("-1 + 2 * 3 + 4 ^ 5 + 6 * (7 - 8)"),
            expected = 1023
        )
    }
}
