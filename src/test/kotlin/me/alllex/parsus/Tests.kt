package me.alllex.parsus

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import me.alllex.parsus.tree.*
import org.junit.Test

class Tests {

    @Test
    fun `Single literal`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            override val root = parser { lexeme(a) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(
                g.a.lex(0)
            )
        }
    }

    @Test
    fun `Empty parser`() {
        object : Grammar<Unit>() {
            override val root = parser {}
        }.let { g ->
            assertThat(g.parseEntireOrThrow("")).isEqualTo(Unit)
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val empty = parser {}
            override val root = parser { -empty * lexeme(a) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(g.a.lex(0))
        }
    }

    @Test
    fun `Token mismatch`() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("aa")
            val b by literalToken("bb")
            override val root = parser { node(lexeme(a) + lexeme(b)) }
        }

        assertThat(g.parseEntire("bb")).failedWithTokenMismatch(expected = g.a, actual = g.b, offset = 0)
        assertThat(g.parseEntire("aa")).failedWithTokenMismatch(expected = g.b, actual = EofToken, offset = 2)
        assertThat(g.parseEntire("aabbaa")).failedWithTokenMismatch(expected = EofToken, actual = g.a, offset = 4)
    }

    @Test
    fun `Two literal tokens`() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            override val root = parser { node(lexeme(a) + lexeme(b)) }
        }

        assertThat(g.parseEntireOrThrow("ab")).isEqualTo(
            node(g.a.lex(0), g.b.lex(1))
        )
    }

    @Test
    fun `Regex token`() {
        val g = object : Grammar<SyntaxTree>() {
            val a by regexToken("a+b+")
            override val root = parser { lexeme(a) }
        }

        assertThat(g.parseEntireOrThrow("ab")).isEqualTo(g.a.lex("ab", 0))
        assertThat(g.parseEntireOrThrow("aaabb")).isEqualTo(g.a.lex("aaabb", 0))
    }

    @Test
    fun `Lambda token`() {
        val g = object : Grammar<SyntaxTree>() {
            val numToken by token(firstChars = "+-0123456789.") { it, at ->
                var index = at
                val maybeSign = it[index]
                val sign = maybeSign == '+' || maybeSign == '-'
                if (sign) index++

                val length = it.length
                while (index < length && it[index].isDigit()) {
                    index++
                }

                if (index < length && it[index] == '.') { // decimal
                    index++
                    while (index < length && it[index].isDigit()) {
                        index++
                    }
                }
                if (index == at || (index == at + 1 && sign)) return@token 0
                index - at
            }
            override val root = parser { lexeme(numToken) }
        }

        assertThat(g.parseEntireOrThrow("+42.5")).isEqualTo(g.numToken.lex("+42.5", 0))
    }

    @Test
    fun `Nested parsers`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val pb = parser { lexeme(b) }
            override val root = parser { node(lexeme(a) + pb()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("ab")).isEqualTo(
                node(g.a.lex(0), g.b.lex(1))
            )
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p1 = parser { lexeme(a) }
            val p2 = parser { node(lexeme(b) + p1()) }
            override val root = parser { node(p1() + lexeme(b) + p2()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("abba")).isEqualTo(
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
            override val root = parser { poll(ap) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseEntireOrThrow("")).isNull()
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")

            @Suppress("unused")
            val c by literalToken("c")
            val pa = parser { lexeme(a) }
            val pb = parser { lexeme(b) }
            override val root = parser { choose(pa, pb) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseEntireOrThrow("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseEntire("c")).failedWith(NoViableAlternative(0))
        }

        object : Grammar<SyntaxTree?>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val pa = parser { lexeme(a) }
            val pb = parser { lexeme(b) }
            override val root = parser { node(poll(pa) + pb()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("b")).isEqualTo(node(g.b))
            assertThat(g.parseEntireOrThrow("ab")).isEqualTo(node(g.a.lex(0), g.b.lex(1)))
        }
    }

    @Test
    fun `Parsing alternatives`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { choose(ap, bp) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseEntireOrThrow("b")).isEqualTo(g.b.lex(0))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(lexeme(c) + choose(ap, bp) + lexeme(c)) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("cac")).isEqualTo(
                node(g.c.lex(0), g.a.lex(1), g.c.lex(2))
            )
            assertThat(g.parseEntireOrThrow("cbc")).isEqualTo(
                node(g.c.lex(0), g.b.lex(1), g.c.lex(2))
            )
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            init { registerToken(literalToken("d")) }
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            val cp = parser { lexeme(c) }
            val abcp = parser { choose(ap, bp, cp) }
            override val root = parser { node(abcp() + abcp())}
        }.let { g ->
            assertThat(g.parseEntireOrThrow("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseEntireOrThrow("bc")).isEqualTo(node(g.b, g.c))
            assertThat(g.parseEntireOrThrow("ca")).isEqualTo(node(g.c, g.a))
            assertThat(g.parseEntire("d")).failedWith(NoViableAlternative(0))
            assertThat(g.parseEntire("bd")).failedWith(NoViableAlternative(1))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p1 = parser { lexeme(a) }
            val p2 = parser { lexeme(b) }
            override val root = parser { node(p1() + choose(p1, p2) + p2()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parseEntireOrThrow("abb")).isEqualTo(node(g.a, g.b, g.b))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val p1 = parser { lexeme(a) }
            val p2 = parser { lexeme(b) }
            val p3 = parser { lexeme(c) }
            val p4 = parser { choose(p1, p2) }
            override val root = parser { node(p1() + choose(p3, p4)) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("ac")).isEqualTo(node(g.a, g.c))
            assertThat(g.parseEntireOrThrow("aa")).isEqualTo(node(g.a, g.a))
            assertThat(g.parseEntireOrThrow("ab")).isEqualTo(node(g.a, g.b))
        }
    }

    @Test
    fun `Backtracking on mismatch`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            val cp = parser { lexeme(c) }
            val abp = parser { node(ap(), bp()) }
            val acp = parser { node(ap(), cp()) }
            override val root = parser { choose(abp, acp) }
        }.run {
            assertThat(parseEntireOrThrow("ab")).isEqualTo(node(a, b))
            assertThat(parseEntireOrThrow("ac")).isEqualTo(node(a, c))
        }
    }

    @Test
    fun `Zero or more`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val ab = parser { lexeme(b) }
            override val root = parser { node(repeatZeroOrMore(ap) + ab()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("b")).isEqualTo(node(g.b))
            assertThat(g.parseEntireOrThrow("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseEntireOrThrow("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parseEntire("")).failedWithTokenMismatch(g.b, EofToken, offset = 0)
        }
    }

    @Test
    fun `One or more`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(repeatOneOrMore(ap) + bp()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseEntireOrThrow("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parseEntire("b")).failedWithNotEnoughRepetition(0, 1, 0)
        }
    }

    @Test
    fun `Repeats in range`() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(bp() + repeat(ap, 2, 3) + bp()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("baab")).isEqualTo(node(g.b, g.a, g.a, g.b))
            assertThat(g.parseEntireOrThrow("baaab")).isEqualTo(node(g.b, g.a, g.a, g.a, g.b))
            assertThat(g.parseEntire("bab")).failedWithNotEnoughRepetition(1, 2, 1)
            assertThat(g.parseEntire("baaaab")).failedWithTokenMismatch(g.b, g.a, offset = 4)
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(repeat(bp, 3) + repeat(ap, 2, 3) + bp()) }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("bbbbaab")).isEqualTo(node(g.b, g.b, g.b, g.b, g.a, g.a, g.b))
            assertThat(g.parseEntire("bbbbbab")).failedWithNotEnoughRepetition(5, 2, 1)
        }
    }

    @Test
    fun `Left associative`() {
        object : Grammar<SyntaxTree>() {
            val s by literalToken(" ")
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            val abp = ap or bp
            override val root = parser {
                reduce<SyntaxTree, TokenMatch>(abp, s) { l, _, r -> node(l, r) }
            }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseEntireOrThrow("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseEntireOrThrow("a b")).isEqualTo(node(g.a.lex(0), g.b.lex(2)))
            assertThat(g.parseEntireOrThrow("a b a")).isEqualTo(node(node(g.a.lex(0), g.b.lex(2)), g.a.lex(4)))
        }
    }

    @Test
    fun `Right associative`() {
        object : Grammar<SyntaxTree>() {
            val s by literalToken(" ")
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            val abp = ap or bp
            override val root = parser {
                reduceRight<SyntaxTree, TokenMatch>(abp, s) { l, _, r -> node(l, r) }
            }
        }.let { g ->
            assertThat(g.parseEntireOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseEntireOrThrow("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseEntireOrThrow("a b")).isEqualTo(node(g.a.lex(0), g.b.lex(2)))
            assertThat(g.parseEntireOrThrow("a b a")).isEqualTo(node(g.a.lex(0), node(g.b.lex(2), g.a.lex(4))))
        }
    }

    @Test
    fun `Left and right associative`() {
        object : Grammar<SyntaxTree>() {
            val x by literalToken("x")
            val y by literalToken("y")
            val and by literalToken("&")
            val impl by literalToken("->")
            val idp = parser { lexeme(x) } or parser { lexeme(y) }
            val andp = parser { lexeme(and) }
            val implp = parser { lexeme(impl) }
            val andChain by parser {
                reduce<SyntaxTree, Lexeme>(idp, andp) { l, op, r -> node(l, op, r) }
            }
            val implChain by parser {
                reduceRight(andChain, implp) { l, op, r -> node(l, op, r) }
            }
            override val root = implChain
        }.let { g ->
            assertThat(g.parseEntireOrThrow("x")).isEqualTo(g.x.lex(0))
            assertThat(g.parseEntireOrThrow("x&y->y&x->x")).isEqualTo(with(g) {
                node(node(x, and, y), impl.lex(3), node(node(y, and, x, startOffset = 5), impl.lex(8), x.lex(10)))
            })
        }
    }

    @Test
    fun `Between combinator`() {
        object : Grammar<SyntaxTree>() {
            val lp by literalToken("(")
            val rp by literalToken(")")
            val a by literalToken("a")
            val ap = parser { lexeme(a) }
            val inPar = ap.between(lp, rp)
            val atom = ap or inPar
            override val root = parser { node(repeatOneOrMore(atom)) }
        }.run {
            assertParsed("aa").isEqualTo(node(a, a))
            assertParsed("a(a)").isEqualTo(node(a.lex(0), a.lex(2)))
            assertParsed("(a)a").isEqualTo(node(a.lex(1), a.lex(3)))
            assertParsed("a(a)a").isEqualTo(node(a.lex(0), a.lex(2), a.lex(4)))
        }
    }

    companion object {

        private fun <T> Grammar<T>.assertParsed(text: String): Assert<T> = assertThat(parseEntireOrThrow(text))

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

        private fun <T> Assert<ParseResult<T>>.failedWith(parseError: ParseError) {
            isEqualTo(parseError)
        }

        private fun <T> Assert<ParseResult<T>>.failedWithNotEnoughRepetition(offset: Int, expectedAtLeast: Int, actualCount: Int) {
            isInstanceOf(NotEnoughRepetition::class)
                .all {
                    prop(NotEnoughRepetition::offset).isEqualTo(offset)
                    prop(NotEnoughRepetition::expectedAtLeast).isEqualTo(expectedAtLeast)
                    prop(NotEnoughRepetition::actualCount).isEqualTo(actualCount)
                }
        }

        private fun <T> Assert<ParseResult<T>>.failedWithTokenMismatch(expected: Token, actual: Token, offset: Int) {
            isInstanceOf(MismatchedToken::class)
                .all {
                    prop("expected token", MismatchedToken::expected).isEqualTo(expected)
                    prop("actual lexeme", MismatchedToken::found).all {
                        prop(TokenMatch::token).isEqualTo(actual)
                        prop(TokenMatch::offset).isEqualTo(offset)
                    }
                }
        }
    }
}
