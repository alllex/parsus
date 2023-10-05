package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
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

    @Test
    fun naiveJsonGrammarTest() {
        NaiveJsonGrammar.run {
            assertParsed("""{${'\n'}"a": 1,${'\n'}"b": {"c":false}${'\n'}}""").isEqualTo(
                Json.Obj(
                    mapOf(
                        "a" to Json.Num(1.0),
                        "b" to Json.Obj(mapOf("c" to Json.Bool(false)))
                    )
                )
            )
        }
    }

    sealed class Json {
        object Null : Json() {
            override fun toString(): String = "Null"
        }

        data class Bool(val value: Boolean) : Json()
        data class Num(val value: Double) : Json()
        data class Str(val value: String) : Json()
        data class Arr(val values: List<Json>) : Json()
        data class Obj(val values: Map<String, Json>) : Json()
    }

    object NaiveJsonGrammar : Grammar<Json>() {
        init {
            regexToken("\\s+", ignored = true)
        }

        private val comma by literalToken(",")
        private val colon by literalToken(":")
        private val lbrace by literalToken("{")
        private val rbrace by literalToken("}")
        private val lbracket by literalToken("[")
        private val rbracket by literalToken("]")
        private val str by regexToken("\"[^\\\\\"]*(\\\\[\"nrtbf\\\\][^\\\\\"]*)*\"") map { it.text.run { substring(1, lastIndex) } }
        private val jsonTrue by literalToken("true") map { Json.Bool(true) }
        private val jsonFalse by literalToken("false") map { Json.Bool(false) }
        private val jsonNull by literalToken("null") map Json.Null
        private val jsonNum by regexToken("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?") map { Json.Num(it.text.toDouble()) }
        private val jsonStr by str map { Json.Str(it) }

        private val keyValue by str * -colon and ref(::jsonValue) map { it.toPair() }
        private val jsonObj by -lbrace * separated(keyValue, comma) * -rbrace map { Json.Obj(it.toMap()) }

        private val jsonArr by -lbracket * separated(ref(::jsonValue), comma) * -rbracket map { Json.Arr(it) }
        private val jsonValue: Parser<Json> by jsonNull or jsonTrue or jsonFalse or jsonNum or jsonStr or jsonArr or jsonObj
        override val root by jsonValue
    }

}
