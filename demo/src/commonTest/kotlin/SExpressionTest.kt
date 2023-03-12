package me.alllex.parsus.demo

import me.alllex.parsus.demo.sexpr.SExpression.*
import me.alllex.parsus.demo.sexpr.SExpressionGrammar
import kotlin.test.Test
import kotlin.test.assertEquals

class SExpressionTest {

    @Test
    fun sexprParsing() {
        val grammar = SExpressionGrammar
        assertEquals(
            actual = grammar.parseEntireOrThrow("""
                ((data "quoted data" 123 4.5) (data (!@# (4.5) "(more" "data)")))
            """.trimIndent()),
            expected = Lst(
                Lst(
                    Sym("data"),
                    Str("quoted data"),
                    Int(123),
                    Num(4.5)
                ),
                Lst(
                    Sym("data"),
                    Lst(
                        Sym("!@#"),
                        Lst(Num(4.5)),
                        Str("(more"),
                        Str("data)")
                    )
                )
            )
        )
    }
}
