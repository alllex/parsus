package me.alllex.parsus

import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import me.alllex.parsus.tree.Lexeme
import me.alllex.parsus.tree.SyntaxTree
import kotlin.test.Test

class AndTests {

    @Test
    fun parserAndParser() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p: Parser<Tuple2<Lexeme, Lexeme>> by parlex(a) and parlex(b)
            override val root = p map { node(it) }
        }.run {
            assertParsed("ab").isEqualTo(node(a.lex(0), b.lex(1)))
        }
    }

    @Test
    fun parserAndIgnored() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p: Parser<Lexeme> by parlex(a) and -parlex(b)
            override val root = p map { node(it) }
        }.run {
            assertParsed("ab").isEqualTo(node(a.lex(0)))
        }
    }

    @Test
    fun ignoredAndParser() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p: Parser<Lexeme> by -parlex(a) and parlex(b)
            override val root = p map { node(it) }
        }.run {
            assertParsed("ab").isEqualTo(node(b.lex(1)))
        }
    }

    @Test
    fun ignoredAndIgnored() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val p: Parser<Unit> by -parlex(a) and -parlex(b)
            override val root = p map { node() }
        }.run {
            assertParsed("ab").isEqualTo(node())
        }
    }

    @Test
    fun tuple2AndParserCreatesTuple3() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val p: Parser<Tuple3<Lexeme, Lexeme, Lexeme>> by parlex(a) and parlex(b) and parlex(c)
            override val root = p map { node(it) }
        }.run {
            assertParsed("abc").isEqualTo(node(a.lex(0), b.lex(1), c.lex(2)))
        }
    }

    @Test
    fun tuple3AndParserCreatesTuple4() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val d by literalToken("d")
            val p: Parser<Tuple4<Lexeme, Lexeme, Lexeme, Lexeme>> by parlex(a) and parlex(b) and parlex(c) and parlex(d)
            override val root = p map { node(it) }
        }.run {
            assertParsed("abcd").isEqualTo(node(a.lex(0), b.lex(1), c.lex(2), d.lex(3)))
        }
    }

    @Test
    fun tuple4AndParserCreatesTuple5() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val d by literalToken("d")
            val e by literalToken("e")
            val p: Parser<Tuple5<Lexeme, Lexeme, Lexeme, Lexeme, Lexeme>> by parlex(a) and parlex(b) and parlex(c) and parlex(d) and parlex(e)
            override val root = p map { node(it) }
        }.run {
            assertParsed("abcde").isEqualTo(node(a.lex(0), b.lex(1), c.lex(2), d.lex(3), e.lex(4)))
        }
    }

    @Test
    fun tuple5AndParserCreatesTuple6() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val d by literalToken("d")
            val e by literalToken("e")
            val f by literalToken("f")
            val p: Parser<Tuple6<Lexeme, Lexeme, Lexeme, Lexeme, Lexeme, Lexeme>> by parlex(a) and parlex(b) and parlex(c) and parlex(d) and parlex(e) and parlex(f)
            override val root = p map { node(it) }
        }.run {
            assertParsed("abcdef").isEqualTo(node(a.lex(0), b.lex(1), c.lex(2), d.lex(3), e.lex(4), f.lex(5)))
        }
    }

    @Test
    fun tuple6AndParserCreatesTuple7() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val c by literalToken("c")
            val d by literalToken("d")
            val e by literalToken("e")
            val f by literalToken("f")
            val g by literalToken("g")
            val p: Parser<Tuple7<Lexeme, Lexeme, Lexeme, Lexeme, Lexeme, Lexeme, Lexeme>> by parlex(a) and parlex(b) and parlex(c) and parlex(d) and parlex(e) and parlex(f) and parlex(g)
            override val root = p map { node(it) }
        }.run {
            assertParsed("abcdefg").isEqualTo(node(a.lex(0), b.lex(1), c.lex(2), d.lex(3), e.lex(4), f.lex(5), g.lex(6)))
        }
    }

    @Test
    fun dateTimeGrammar() {
        object : Grammar<String>() {
            val n4 by regexToken("\\d{4}") map { it.text }
            val n2 by regexToken("\\d{2}") map { it.text }
            val dash by literalToken("-")
            val colon by literalToken(":")
            val space by literalToken(" ")

            val dateTime by
                n4 and -dash and n2 and -dash and n2 and -space and n2 and -colon and n2 and -colon and n2 map { (y, mo, d, h, m, s) ->
                    "$y-$mo-$d $h:$m:$s"
                }

            override val root by dateTime
        }.run {
            assertParsed("2021-01-01 00:00:00").isEqualTo("2021-01-01 00:00:00")
            assertParsed("2023-05-27 12:34:56").isEqualTo("2023-05-27 12:34:56")
        }
    }

}
