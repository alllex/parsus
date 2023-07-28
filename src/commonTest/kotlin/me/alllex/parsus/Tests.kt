package me.alllex.parsus

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import me.alllex.parsus.tree.*
import kotlin.test.Test

class Tests {

    @Test
    fun singleLiteral() {
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
    fun emptyParser() {
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
    fun tokenMismatch() {
        val g = object : Grammar<SyntaxTree>() {
            val a by literalToken("aa")
            val b by literalToken("bb")
            override val root = parser { node(lexeme(a) + lexeme(b)) }
        }

        assertThat(g.parse("bb")).failedWithTokenMismatch(expected = g.a, actual = g.b, offset = 0)
        assertThat(g.parse("aa")).failedWithTokenMismatch(expected = g.b, actual = EofToken, offset = 2)
        assertThat(g.parse("aabbaa")).failedWithTokenMismatch(expected = EofToken, actual = g.a, offset = 4)
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
            assertThatParsing("b").failedWithTokenMismatch(a, b, offset = 0)
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
            assertThatParsing("aa").failedWithTokenMismatch(EofToken, a, offset = 1)
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
            assertThat(g.parse("")).failedWithTokenMismatch(g.b, EofToken, offset = 0)
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
            assertThat(g.parse("baaaab")).failedWithTokenMismatch(g.b, g.a, offset = 4)
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
    fun recursiveParser() {
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

    @Test
    fun literalTokenIgnoreCase() {
        object : Grammar<SyntaxTree>() {
            val data by literalToken("data", ignoreCase = true)
            override val root by parser { lexeme(data) }
        }.run {
            assertParsed("data").isEqualTo(data.lex())
            assertParsed("DATA").isEqualTo(data.lex("DATA"))
            assertParsed("Data").isEqualTo(data.lex("Data"))
            assertParsed("dAtA").isEqualTo(data.lex("dAtA"))
        }
    }

    @Test
    fun regexTokenIgnoreCase() {
        object : Grammar<SyntaxTree>() {
            val data by regexToken("[ab]", ignoreCase = true)
            override val root by parser { lexeme(data) }
        }.run {
            assertParsed("a").isEqualTo(data.lex("a"))
            assertParsed("b").isEqualTo(data.lex("b"))
            assertParsed("A").isEqualTo(data.lex("A"))
            assertParsed("B").isEqualTo(data.lex("B"))
        }
    }

    @Test
    fun explicitRegexTokenIgnoreCase() {
        object : Grammar<SyntaxTree>() {
            val data by regexToken(Regex("[ab]"), ignoreCase = true)
            override val root by parser { lexeme(data) }
        }.run {
            assertParsed("a").isEqualTo(data.lex("a"))
            assertParsed("b").isEqualTo(data.lex("b"))
            assertParsed("A").isEqualTo(data.lex("A"))
            assertParsed("B").isEqualTo(data.lex("B"))
        }
    }

    @Test
    fun ignoreCaseGrammar() {
        object : Grammar<SyntaxTree>(ignoreCase = true) {
            val lit by literalToken("a")
            val reLit by regexToken("[bc]")
            val re by regexToken(Regex("[de]"))
            val lam by token { s, i -> if (s[i] == 'f' || (ignoreCase && s[i] == 'F')) 1 else 0 }
            val lamStrict by token { s, i -> if (s[i] == 'g') 1 else 0 }
            override val root by parlex(lit) or parlex(reLit) or parlex(re) or parlex(lam) or parlex(lamStrict)
        }.run {
            assertParsed("a").isEqualTo(lit.lex("a"))
            assertParsed("A").isEqualTo(lit.lex("A"))
            assertParsed("b").isEqualTo(reLit.lex("b"))
            assertParsed("C").isEqualTo(reLit.lex("C"))
            assertParsed("D").isEqualTo(re.lex("D"))
            assertParsed("e").isEqualTo(re.lex("e"))
            assertParsed("f").isEqualTo(lam.lex("f"))
            assertParsed("F").isEqualTo(lam.lex("F"))
            assertParsed("g").isEqualTo(lamStrict.lex("g"))
            assertThat(parse("G")).failedWith(NoMatchingToken(0))
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
            assertThatParsing("b").failedWithTokenMismatch(a, b, 0)
            assertThat(parse(nonRootParser, "b").getOrThrow()).isEqualTo(b.lex())
        }
    }

    @Test
    fun forceIgnoredTokenParsing() {
        object : Grammar<SyntaxTree>() {
            val ws by regexToken("\\s+", ignored = true)
            val a by literalToken("a")
            override val root by parser {
                val a1 = lexeme(a)
                ws()
                val a2 = lexeme(a)
                node(a1, a2)
            }
        }.run {
            assertParsed("a a").isEqualTo(node(a.lex("a", 0), a.lex("a", 2)))
            assertParsed(" a a ").isEqualTo(node(a.lex("a", 1), a.lex("a", 3)))
            assertNotParsed("aa").failedWithTokenMismatch(ws, a, 1)
            assertNotParsed(" aa").failedWithTokenMismatch(ws, a, 2)
        }

        object : Grammar<SyntaxTree>() {
            val ws by regexToken("\\s+", ignored = true)
            val a by literalToken("a")
            override val root by parlex(a) and (-ws * parlex(a)) map { node(it.first, it.second) }
        }.run {
            assertParsed("a a").isEqualTo(node(a.lex("a", 0), a.lex("a", 2)))
            assertParsed(" a a ").isEqualTo(node(a.lex("a", 1), a.lex("a", 3)))
            assertNotParsed("aa").failedWithTokenMismatch(ws, a, 1)
            assertNotParsed(" aa").failedWithTokenMismatch(ws, a, 2)
        }
    }

    companion object {

        private fun parlex(token: Token) = parser { lexeme(token) }

        private fun <T> Grammar<T>.assertParsed(text: String): Assert<T> = assertThat(parseOrThrow(text))

        private fun <T> Grammar<T>.assertNotParsed(text: String): Assert<ParseError> =
            assertThat(parse(text)).isInstanceOf(ParseError::class)

        private fun <T> Grammar<T>.assertThatParsing(text: String): Assert<ParseResult<T>> = assertThat(parse(text))

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

        private fun LiteralToken.lex(offset: Int = 0): Lexeme {
            return Lexeme(TokenMatch(this, offset, string.length), string)
        }

        private fun Token.lex(text: String, offset: Int = 0): Lexeme {
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
