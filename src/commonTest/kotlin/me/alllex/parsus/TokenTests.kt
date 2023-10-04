package me.alllex.parsus

import assertk.assertions.isEqualTo
import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.map
import me.alllex.parsus.parser.or
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
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

}
