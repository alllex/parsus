package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.tree.SyntaxTree
import me.alllex.parsus.tree.lexeme
import kotlin.test.Test

class GrammarTests {

    @Test
    fun singleTokenGrammar() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            override val root = parser { lexeme(a) }
        }.let { g ->
            assertThat(g.parseOrThrow("a")).isEqualTo(
                g.a.lex(0)
            )
        }
    }

    @Test
    fun emptyGrammar() {
        object : Grammar<Unit>() {
            override val root = parser {}
        }.let { g ->
            assertThat(g.parseOrThrow("")).isEqualTo(Unit)
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val empty = parser {}
            override val root = parser { skip(empty) * lexeme(a) }
        }.let { g ->
            assertThat(g.parseOrThrow("a")).isEqualTo(g.a.lex(0))
        }
    }

    @Test
    fun recursiveGrammar() {
        object : Grammar<SyntaxTree>() {
            val lp by literalToken("(")
            val rp by literalToken(")")
            val a by literalToken("a")
            val ap by parser { lexeme(a) }
            val p: Parser<SyntaxTree> by ap or parser { skip(lp) * p() * skip(rp) }
            override val root = p
        }.run {
            assertParsed("a").isEqualTo(a.lex(0))
            assertParsed("((a))").isEqualTo(a.lex(2))
        }

        object : Grammar<SyntaxTree>() {
            val lp by literalToken("(")
            val rp by literalToken(")")
            val a by literalToken("a")
            val ap by parser { lexeme(a) }
            val p: Parser<SyntaxTree> by ap or (-lp * ref(::p) * -rp)
            override val root = p
        }.run {
            assertParsed("a").isEqualTo(a.lex(0))
            assertParsed("((a))").isEqualTo(a.lex(2))
        }
    }

    @Test
    fun parseWithNonRootParser() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val nonRootParser by parser { lexeme(b) }
            override val root = parser { lexeme(a) }
        }.run {
            assertParsed("a").isEqualTo(a.lex())
            assertThatParsing("b").failedWithUnmatchedToken(a, 0)
            assertThat(parse(nonRootParser, "b").getOrThrow()).isEqualTo(b.lex())
        }
    }

}
