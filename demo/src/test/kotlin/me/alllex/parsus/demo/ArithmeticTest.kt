package me.alllex.parsus.demo

import org.junit.Test
import kotlin.test.assertEquals
import me.alllex.parsus.demo.Expr.*

class ArithmeticTest {

    @Test
    fun `Expr parsing`() {
        assertEquals(
            actual = ExprParser.parseToEnd("-1 + 2 * 3 + 4 ^ 5 + 6 * (7 - 8)"),
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
            actual = ExprCalculator.parseToEnd("-1 + 2 * 3 + 4 ^ 5 + 6 * (7 - 8)"),
            expected = 1023
        )
    }
}
