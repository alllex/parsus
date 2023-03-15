package me.alllex.parsus

import me.alllex.parsus.ReadmeTests.BoolExpr.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadmeTests {

    sealed class BoolExpr {
        data class Var(val name: String) : BoolExpr()
        data class Not(val body: BoolExpr) : BoolExpr()
        data class And(val left: BoolExpr, val right: BoolExpr) : BoolExpr()
        data class Or(val left: BoolExpr, val right: BoolExpr) : BoolExpr()
        data class Impl(val left: BoolExpr, val right: BoolExpr) : BoolExpr()
    }

    @Test
    fun leadSample() {
        val booleanGrammar = object : Grammar<BoolExpr>() {
            init {
                regexToken("\\s+", ignored = true)
            }

            val id by regexToken("\\w+")
            val lpar by literalToken("(")
            val rpar by literalToken(")")
            val not by literalToken("!")
            val and by literalToken("&")
            val or by literalToken("|")
            val impl by literalToken("->")

            val negation by -not * ref(::term) map { Not(it) }
            val braced by -lpar * ref(::root) * -rpar

            val term: Parser<BoolExpr> by (id map { Var(it.text) }) or negation or braced

            val andChain by leftAssociative(term, and, ::And)
            val orChain by leftAssociative(andChain, or, ::Or)
            val implChain by rightAssociative(orChain, impl, ::Impl)

            override val root by implChain
        }

        val ast = booleanGrammar.parseEntire("a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a").getOrThrow()

        assertEquals(
            actual = ast,
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
