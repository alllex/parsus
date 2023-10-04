package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import me.alllex.parsus.tree.Lexeme
import me.alllex.parsus.tree.SyntaxTree
import me.alllex.parsus.tree.lexeme
import me.alllex.parsus.tree.plus
import kotlin.test.Test

class Tests {

    @Test
    fun tokenMismatch() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("aa")
            val b by literalToken("bb")
            override val root = parser { node(lexeme(a) + lexeme(b)) }
        }

        assertThat(g.parse("bb")).failedWithUnmatchedToken(expected = g.a, offset = 0)
        assertThat(g.parse("aa")).failedWithUnmatchedToken(expected = g.b, offset = 2)
        assertThat(g.parse("aabbaa")).failedWithUnmatchedToken(expected = EofToken, offset = 4)
    }

    @Test
    fun twoLiteralTokens() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            override val root = parser { node(lexeme(a) + lexeme(b)) }
        }

        assertThat(g.parseOrThrow("ab")).isEqualTo(
            node(g.a.lex(0), g.b.lex(1))
        )
    }

    @Test
    fun regexToken() {
        val g = object : Grammar<SyntaxTree>() {
            val a by regexToken("a+b+")
            override val root = parser { lexeme(a) }
        }

        assertThat(g.parseOrThrow("ab")).isEqualTo(g.a.lex("ab", 0))
        assertThat(g.parseOrThrow("aaabb")).isEqualTo(g.a.lex("aaabb", 0))
    }

    @Test
    fun lambdaToken() {
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

        assertThat(g.parseOrThrow("+42.5")).isEqualTo(g.numToken.lex("+42.5", 0))
    }

    @Test
    fun nestedParsers() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val pb = parser { lexeme(b) }
            override val root = parser { node(lexeme(a) + pb()) }
        }.let { g ->
            assertThat(g.parseOrThrow("ab")).isEqualTo(
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
            assertThat(g.parseOrThrow("abba")).isEqualTo(
                node(
                    g.a.lex(0),
                    g.b.lex(1),
                    node(g.b.lex(2), g.a.lex(3))
                )
            )
        }
    }

    @Test
    fun optionalParser() {
        object : Grammar<SyntaxTree?>() {
            val a by literalToken("a")
            val ap = parser { lexeme(a) }
            override val root = parser { poll(ap) }
        }.let { g ->
            assertThat(g.parseOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseOrThrow("")).isNull()
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
            assertThat(g.parseOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseOrThrow("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parse("c")).failedWith(NoViableAlternative(0))
        }

        object : Grammar<SyntaxTree?>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val pa = parser { lexeme(a) }
            val pb = parser { lexeme(b) }
            override val root = parser { node(poll(pa) + pb()) }
        }.let { g ->
            assertThat(g.parseOrThrow("b")).isEqualTo(node(g.b))
            assertThat(g.parseOrThrow("ab")).isEqualTo(node(g.a.lex(0), g.b.lex(1)))
        }
    }

    @Test
    fun parsingAlternatives() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { choose(ap, bp) }
        }.let { g ->
            assertThat(g.parseOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseOrThrow("b")).isEqualTo(g.b.lex(0))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(lexeme(c) + choose(ap, bp) + lexeme(c)) }
        }.let { g ->
            assertThat(g.parseOrThrow("cac")).isEqualTo(
                node(g.c.lex(0), g.a.lex(1), g.c.lex(2))
            )
            assertThat(g.parseOrThrow("cbc")).isEqualTo(
                node(g.c.lex(0), g.b.lex(1), g.c.lex(2))
            )
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")

            init {
                literalToken("d")
            }

            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            val cp = parser { lexeme(c) }
            val abcp = parser { choose(ap, bp, cp) }
            override val root = parser { node(abcp() + abcp()) }
        }.let { g ->
            assertThat(g.parseOrThrow("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseOrThrow("bc")).isEqualTo(node(g.b, g.c))
            assertThat(g.parseOrThrow("ca")).isEqualTo(node(g.c, g.a))
            assertThat(g.parse("d")).failedWith(NoViableAlternative(0))
            assertThat(g.parse("bd")).failedWith(NoViableAlternative(1))
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p1 = parser { lexeme(a) }
            val p2 = parser { lexeme(b) }
            override val root = parser { node(p1() + choose(p1, p2) + p2()) }
        }.let { g ->
            assertThat(g.parseOrThrow("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parseOrThrow("abb")).isEqualTo(node(g.a, g.b, g.b))
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
            assertThat(g.parseOrThrow("ac")).isEqualTo(node(g.a, g.c))
            assertThat(g.parseOrThrow("aa")).isEqualTo(node(g.a, g.a))
            assertThat(g.parseOrThrow("ab")).isEqualTo(node(g.a, g.b))
        }
    }

    @Test
    fun backtrackingOnMismatch() {
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
            assertThat(parseOrThrow("ab")).isEqualTo(node(a, b))
            assertThat(parseOrThrow("ac")).isEqualTo(node(a, c))
        }
    }

    @Test
    fun skipParser() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parlex(a)
            val ab = parlex(b)
            override val root = parser {
                skip(ap)
                ab()
            }
        }.run {
            assertParsed("ab").isEqualTo(b.lex(1))
            assertThatParsing("b").failedWithUnmatchedToken(a, offset = 0)
        }
    }

    @Test
    fun hasParser() {
        object : Grammar<Pair<Boolean, Boolean>>() {
            val a by literalToken("a")
            val b by literalToken("b")
            override val root = parser {
                val hasA = has(a)
                val hasB = has(b)
                hasA to hasB
            }
        }.run {
            assertParsed("ab").isEqualTo(true to true)
            assertParsed("a").isEqualTo(true to false)
            assertParsed("b").isEqualTo(false to true)
            assertParsed("").isEqualTo(false to false)
            assertThatParsing("aa").failedWithUnmatchedToken(EofToken, offset = 1)
        }
    }

    @Test
    fun repeatZeroOrMore() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val ab = parser { lexeme(b) }
            override val root = parser { node(repeatZeroOrMore(ap) + ab()) }
        }.let { g ->
            assertThat(g.parseOrThrow("b")).isEqualTo(node(g.b))
            assertThat(g.parseOrThrow("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseOrThrow("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parse("")).failedWithUnmatchedToken(g.b, offset = 0)
        }
    }

    @Test
    fun repeatOneOrMore() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(repeatOneOrMore(ap) + bp()) }
        }.let { g ->
            assertThat(g.parseOrThrow("ab")).isEqualTo(node(g.a, g.b))
            assertThat(g.parseOrThrow("aab")).isEqualTo(node(g.a, g.a, g.b))
            assertThat(g.parse("b")).failedWithNotEnoughRepetition(0, 1, 0)
        }
    }

    @Test
    fun repeatInRange() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(bp() + repeat(ap, 2, 3) + bp()) }
        }.let { g ->
            assertThat(g.parseOrThrow("baab")).isEqualTo(node(g.b, g.a, g.a, g.b))
            assertThat(g.parseOrThrow("baaab")).isEqualTo(node(g.b, g.a, g.a, g.a, g.b))
            assertThat(g.parse("bab")).failedWithNotEnoughRepetition(1, 2, 1)
            assertThat(g.parse("baaaab")).failedWithUnmatchedToken(g.b, offset = 4)
        }

        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val ap = parser { lexeme(a) }
            val bp = parser { lexeme(b) }
            override val root = parser { node(repeat(bp, 3) + repeat(ap, 2, 3) + bp()) }
        }.let { g ->
            assertThat(g.parseOrThrow("bbbbaab")).isEqualTo(node(g.b, g.b, g.b, g.b, g.a, g.a, g.b))
            assertThat(g.parse("bbbbbab")).failedWithNotEnoughRepetition(5, 2, 1)
        }
    }

    @Test
    fun reduce() {
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
            assertThat(g.parseOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseOrThrow("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseOrThrow("a b")).isEqualTo(node(g.a.lex(0), g.b.lex(2)))
            assertThat(g.parseOrThrow("a b a")).isEqualTo(node(node(g.a.lex(0), g.b.lex(2)), g.a.lex(4)))
        }
    }

    @Test
    fun reduceRight() {
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
            assertThat(g.parseOrThrow("a")).isEqualTo(g.a.lex(0))
            assertThat(g.parseOrThrow("b")).isEqualTo(g.b.lex(0))
            assertThat(g.parseOrThrow("a b")).isEqualTo(node(g.a.lex(0), g.b.lex(2)))
            assertThat(g.parseOrThrow("a b a")).isEqualTo(node(g.a.lex(0), node(g.b.lex(2), g.a.lex(4))))
        }
    }

    @Test
    fun reduceAndReduceRight() {
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
            assertThat(g.parseOrThrow("x")).isEqualTo(g.x.lex(0))
            assertThat(g.parseOrThrow("x&y->y&x->x")).isEqualTo(with(g) {
                node(node(x, and, y), impl.lex(3), node(node(y, and, x, startOffset = 5), impl.lex(8), x.lex(10)))
            })
        }
    }

    @Test
    fun betweenCombinator() {
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

    @Test
    fun separated() {
        object : Grammar<SyntaxTree>() {
            val com by literalToken(",")
            val a by literalToken("a")
            val ap by parser { lexeme(a) }
            val p: Parser<SyntaxTree> by separated(ap, com, trailingSeparator = true) map { node(it) }
            override val root = p
        }.run {
            assertParsed("").isEqualTo(node())
            assertParsed("a").isEqualTo(node(a))
            assertParsed("a,").isEqualTo(node(a))
            assertParsed("a,a").isEqualTo(node(a.lex(0), a.lex(2)))
            assertParsed("a,a,").isEqualTo(node(a.lex(0), a.lex(2)))
        }
    }

}
