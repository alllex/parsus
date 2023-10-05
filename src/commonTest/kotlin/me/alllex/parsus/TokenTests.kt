package me.alllex.parsus

import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import me.alllex.parsus.tree.SyntaxTree
import me.alllex.parsus.tree.lexeme
import kotlin.test.Test

class TokenTests {

    @Test
    fun literalTokenThatPrefixesRegexTokenWithHigherPriority() {
        object : Grammar<Int>() {
            val r by regexToken("abba") map 1
            val ab by literalToken("ab") map 2
            override val root by r or ab
        }.run {
            assertParsed("abba").isEqualTo(1)
        }
    }

    @Test
    fun tokenPriorityIsDrivenByParser() {
        object : Grammar<TokenMatch>() {
            val single by literalToken("<")
            val double by literalToken("<<")

            // even though single token is declared first, it is not matched first
            override val root by double or single
        }.run {
            assertParsed("<<").isEqualTo(TokenMatch(double, 0, 2))
        }
    }

    @Test
    fun explicitIgnoredTokenParsing() {
        object : Grammar<SyntaxTree>() {
            val ws by regexToken("\\s+", ignored = true)
            val a by literalToken("a")
            override val root by parser {
                val a1 = lexeme(a)
                val w = lexeme(ws)
                val a2 = lexeme(a)
                node(a1, w, a2)
            }
        }.run {
            assertParsed("a a").isEqualTo(node(a.lex("a", 0), ws.lex(" ", 1), a.lex("a", 2)))
            assertParsed(" a a ").isEqualTo(node(a.lex("a", 1), ws.lex(" ", 2), a.lex("a", 3)))
            assertNotParsed("aa").failedWithUnmatchedToken(ws, 1)
            assertNotParsed(" aa").failedWithUnmatchedToken(ws, 2)
        }

        object : Grammar<SyntaxTree>() {
            val ws by regexToken("\\s+", ignored = true)
            val a by literalToken("a")
            override val root by parlex(a) and (-ws * parlex(a)) map { node(it.first, it.second) }
        }.run {
            assertParsed("a a").isEqualTo(node(a.lex("a", 0), a.lex("a", 2)))
            assertParsed(" a a ").isEqualTo(node(a.lex("a", 1), a.lex("a", 3)))
            assertNotParsed("aa").failedWithUnmatchedToken(ws, 1)
            assertNotParsed(" aa").failedWithUnmatchedToken(ws, 2)
        }
    }

}
