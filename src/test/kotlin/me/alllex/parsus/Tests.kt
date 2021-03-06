@file:Suppress("DEPRECATION")

package me.alllex.parsus

import assertk.Assert
import assertk.Result
import assertk.all
import assertk.assertThat
import assertk.assertions.*
import me.alllex.parsus.token.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.tree.*
import org.junit.Test

class Tests {

    @Test
    fun `Single literal`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            override val root = parser { lexeme(a) }
        }.let { g ->
            assertThat(g.parseToEnd("a")).isEqualTo(
                g.a.lex(0)
            )
        }
    }

    @Test
    fun `Empty parser`() {
        object : Grammar<Unit>() {
            override val root = parser {}
        }.let { g ->
            assertThat(g.parseToEnd("")).isEqualTo(Unit)
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val empty = parser {}
            override val root = parser { -empty * lexeme(a) }
        }.let { g ->
            assertThat(g.parseToEnd("a")).isEqualTo(g.a.lex(0))
        }
    }

    @Test
    fun `Token mismatch`() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("aa")
            val b by literalToken("bb")
            override val root = parser { lexeme(a) + lexeme(b) }
        }

        assertThat {
            g.parseToEnd("bb")
        }.hasMismatchedToken(expected = g.a, actual = g.b, offset = 0)

        assertThat {
            g.parseToEnd("aa")
        }.hasMismatchedToken(expected = g.b, actual = EofToken, offset = 2)

        assertThat {
            g.parseToEnd("aabbaa")
        }.hasMismatchedToken(expected = EofToken, actual = g.a, offset = 4)
    }

    @Test
    fun `Two literal tokens`() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            override val root = parser { lexeme(a) + lexeme(b) }
        }

        assertThat(g.parseToEnd("ab")).isEqualTo(
            node(g.a.lex(0), g.b.lex(1))
        )
    }

    @Test
    fun `Regex token`() {
        val g = object : Grammar<SyntaxTree>() {
            val a by regexToken("a+b+")
            override val root = parser { lexeme(a) }
        }

        assertThat(g.parseToEnd("ab")).isEqualTo(g.a.lex("ab", 0))
        assertThat(g.parseToEnd("aaabb")).isEqualTo(g.a.lex("aaabb", 0))
    }

    @Test
    fun `Nested parsers`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val pb = parser { lexeme(b) }
            override val root = parser { lexeme(a) + pb() }
        }.let { g ->
            assertThat(g.parseToEnd("ab")).isEqualTo(
                node(g.a.lex(0), g.b.lex(1))
            )
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p1 = parser { lexeme(a) }
            val p2 = parser { lexeme(b) + p1() }
            override val root = parser { p1() + lexeme(b) + p2() }
        }.let { g ->
            assertThat(g.parseToEnd("abba")).isEqualTo(
                node(
                    g.a.lex(0),
                    g.b.lex(1),
                    node(g.b.lex(2), g.a.lex(3))
                )
            )
        }
    }

    @Test
    fun `Optional parser`() {
        object : Grammar<SyntaxTree?>() {
            val a by literalToken("a")
            val ap = parser { lexeme(a) }
            override val root = parser { zeroOrOne(ap) }
        }.let { g ->
            assertThat(g.parseToEnd("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseToEnd("")).isNull()
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            @Suppress("unused")
            val c by literalToken("c")
            val pa = parser { lexeme(a) }
            val pb = parser { lexeme(b) }
            override val root = parser { any(pa, pb) }
        }.let { g ->
            assertThat(g.parseToEnd("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseToEnd("b")).isEqualTo(g.b.lex(0))
            assertThat { g.parseToEnd("c") }.hasNoViableAlternative(0)
        }

        object : Grammar<SyntaxTree?>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val pa = parser { lexeme(a) }
            val pb = parser { lexeme(b) }
            override val root = parser { zeroOrOne(pa) + pb() }
        }.let { g ->
            assertThat(g.parseToEnd("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseToEnd("ab")).isEqualTo(node(g.a.lex(0), g.b.lex(1)))
        }
    }

    @Test
    fun `Parsing alternatives`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { any(ap, bp) }
        }.let { g ->
            assertThat(g.parseToEnd("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseToEnd("b")).isEqualTo(g.b.lex(0))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { lexeme(c) + any(ap, bp) + lexeme(c) }
        }.let { g ->
            assertThat(g.parseToEnd("cac")).isEqualTo(
                node(g.c.lex(0), g.a.lex(1), g.c.lex(2))
            )
            assertThat(g.parseToEnd("cbc")).isEqualTo(
                node(g.c.lex(0), g.b.lex(1), g.c.lex(2))
            )
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            val cp = parser { lexeme(c) }
            override val root = parser { any(ap, bp, cp) }
        }.let { g ->
            assertThat(g.parseToEnd("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseToEnd("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseToEnd("c")).isEqualTo(g.c.lex(0))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p1 = parser { lexeme(a) }
            val p2 = parser { lexeme(b) }
            override val root = parser { p1() + any(p1, p2) + p2() }
        }.let { g ->
            assertThat(g.parseToEnd("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parseToEnd("abb")).isEqualTo(node(g.a, g.b, g.b))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val p1 = parser { lexeme(a) }
            val p2 = parser { lexeme(b) }
            val p3 = parser { lexeme(c) }
            val p4 = parser { any(p1, p2) }
            override val root = parser { p1() + any(p3, p4) }
        }.let { g ->
            assertThat(g.parseToEnd("ac")).isEqualTo(node(g.a, g.c))
            assertThat(g.parseToEnd("aa")).isEqualTo(node(g.a, g.a))
            assertThat(g.parseToEnd("ab")).isEqualTo(node(g.a, g.b))
        }
    }

    @Test
    fun `Zero or more`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val ab = parser { lexeme(b) }
            override val root = parser { node(zeroOrMore(ap)) + ab() }
        }.let { g ->
            assertThat(g.parseToEnd("b")).isEqualTo(node(g.b))
            assertThat(g.parseToEnd("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseToEnd("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat { g.parseToEnd("") }.hasMismatchedToken(g.b, EofToken, offset = 0)
        }
    }

    @Test
    fun `One or more`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(oneOrMore(ap)) + bp() }
        }.let { g ->
            assertThat(g.parseToEnd("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseToEnd("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat { g.parseToEnd("b") }.hasNotEnoughRepetition(1, 0)
        }
    }

    @Test
    fun `Repeats in range`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(repeating(ap, 2, 3)) + bp() }
        }.let { g ->
            assertThat(g.parseToEnd("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parseToEnd("aaab")).isEqualTo(node(g.a, g.a, g.a, g.b))
            assertThat { g.parseToEnd("ab") }.hasNotEnoughRepetition(2, 1)
            assertThat { g.parseToEnd("aaaab") }.hasMismatchedToken(g.b, g.a, offset = 3)
        }
    }

    companion object {

        private fun node(vararg literals: LiteralToken, startOffset: Int = 0): Node {
            var offset = startOffset
            val lexemes = mutableListOf<Lexeme>()
            for (literal in literals) {
                val l = literal.lex(offset)
                lexemes += l
                offset += l.match.length
            }

            return Node(lexemes)
        }

        private fun node(vararg children: SyntaxTree) = Node(*children)

        private fun node(children: List<SyntaxTree>) = Node(children)

        private fun LiteralToken.lex(offset: Int): Lexeme {
            return Lexeme(TokenMatch(this, offset, string.length), string)
        }

        private fun Token.lex(text: String, offset: Int): Lexeme {
            return Lexeme(TokenMatch(this, offset, text.length), text)
        }

        private fun <T> Assert<Result<T>>.hasNotEnoughRepetition(
            expectedAtLeast: Int,
            actualCount: Int,
        ) {
            isFailure()
                .isInstanceOf(ParseException::class)
                .prop(ParseException::error)
                .isInstanceOf(NotEnoughRepetition::class)
                .all {
                    prop(NotEnoughRepetition::expectedAtLeast).isEqualTo(expectedAtLeast)
                    prop(NotEnoughRepetition::actualCount).isEqualTo(actualCount)
                }
        }

        private fun <T> Assert<Result<T>>.hasMismatchedToken(
            expected: Token,
            actual: Token,
            offset: Int,
        ) {
            isFailure()
                .isInstanceOf(ParseException::class)
                .prop(ParseException::error)
                .isInstanceOf(MismatchedToken::class)
                .all {
                    prop("expected token", MismatchedToken::expected).isEqualTo(expected)
                    prop("actual lexeme", MismatchedToken::found).all {
                        prop(TokenMatch::token).isEqualTo(actual)
                        prop(TokenMatch::offset).isEqualTo(offset)
                    }
                }
        }

        private fun <T> Assert<Result<T>>.hasNoViableAlternative(
            fromIndex: Int
        ) {
            isFailure()
                .isInstanceOf(ParseException::class)
                .prop(ParseException::error)
                .isInstanceOf(NoViableAlternative::class)
                .prop(NoViableAlternative::fromIndex).isEqualTo(fromIndex)
        }
    }
}
