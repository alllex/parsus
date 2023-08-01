package me.alllex.parsus

import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.NoMatchingToken
import me.alllex.parsus.parser.or
import me.alllex.parsus.parser.parser
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import me.alllex.parsus.token.token
import me.alllex.parsus.tree.SyntaxTree
import me.alllex.parsus.tree.lexeme
import kotlin.test.Test

class IgnoreCaseTests {

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
            assertNotParsed("G").failedWith(NoMatchingToken(0))
        }
    }

}
