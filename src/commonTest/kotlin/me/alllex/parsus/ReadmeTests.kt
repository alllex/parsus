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

            val variable by id map { Var(it.text) }
            val negation by -not * ref(::term) map { Not(it) }
            val braced by -lpar * ref(::root) * -rpar

            val term: Parser<BoolExpr> by variable or negation or braced

            val andChain by leftAssociative(term, and, ::And)
            val orChain by leftAssociative(andChain, or, ::Or)
            val implChain by rightAssociative(orChain, impl, ::Impl)

            override val root by implChain
        }

        val ast = booleanGrammar.parseOrThrow("a & (b1 -> c1) | a1 & !b | !(a1 -> a2) -> a")

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

    @Test
    fun quickRefTokenText() {
        // Parsing a token and getting its text
        val testCases = listOf(
            "ab" to ("ab"),
            "aB" to ("aB"),
        )

        val proc = object : Grammar<String>() {
            val ab by regexToken("a[bB]")
            override val root by parser {
                val abMatch = ab()
                abMatch.text
            }
        }

        val comb = object : Grammar<String>() {
            val ab by regexToken("a[bB]")
            override val root by ab map { it.text }
        }

        checkAll(proc, comb, testCases = testCases)
    }

    @Test
    fun quickRefSequential() {
        // Parsing two tokens sequentially
        val testCases = listOf(
            "ab" to ("a" to "b"),
            "aB" to ("a" to "B"),
        )

        val proc = object : Grammar<Pair<String, String>>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by parser {
                val aMatch = a()
                val bMatch = b()
                aMatch.text to bMatch.text
            }
        }

        val comb = object : Grammar<Pair<String, String>>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by a and b map
                { (aM, bM) -> aM.text to bM.text }
        }

        checkAll(proc, comb, testCases = testCases)
    }

    @Test
    fun quickRefAlternative() {
        // Parsing one of two tokens
        val testCases = listOf(
            "a" to ("a"),
            "b" to ("b"),
            "B" to ("B"),
        )

        val proc = object : Grammar<String>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by parser {
                val abMatch = choose(a, b)
                abMatch.text
            }
        }

        val comb = object : Grammar<String>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by a or b map { it.text }
        }

        checkAll(proc, comb, testCases = testCases)
    }

    @Test
    fun quickRefOptional() {
        // Parsing an optional token
        val testCases = listOf(
            "ab" to ("a" to "b"),
            "aB" to ("a" to "B"),
            "b" to (null to "b"),
            "B" to (null to "B"),
        )

        val proc = object : Grammar<Pair<String?, String>>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by parser {
                val aMatch = poll(a)
                val bMatch = b()
                aMatch?.text to bMatch.text
            }
        }

        val comb = object : Grammar<Pair<String?, String>>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by maybe(a) and b map
                { (aM, bM) -> aM?.text to bM.text }
        }

        checkAll(proc, comb, testCases = testCases)
    }

    @Test
    fun quickRefIgnored() {
        // Parsing a token and ignoring its value
        val testCases = listOf(
            "ab" to ("b"),
            "aB" to ("B"),
        )

        val proc = object : Grammar<String>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by parser {
                skip(a) // or just a() without using the value
                val bMatch = b()
                bMatch.text
            }
        }

        val comb = object : Grammar<String>() {
            val a by literalToken("a")
            val b by regexToken("[bB]")
            override val root by -a * b map { it.text }
        }

        checkAll(proc, comb, testCases = testCases)
    }

    private fun <T> checkAll(
        vararg grammars: Grammar<T>,
        testCases: List<Pair<String, T>>,
    ) {
        require(grammars.isNotEmpty())
        for (g in grammars) {
            for ((input, expected) in testCases) {
                assertEquals(actual = g.parseOrThrow(input), expected = expected)
            }
        }
    }

}
