package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.trace.formatTokenMatchingTrace
import me.alllex.parsus.tree.SyntaxTree
import me.alllex.parsus.tree.lexeme
import me.alllex.parsus.tree.plus
import kotlin.test.Test

@OptIn(ExperimentalParsusApi::class)
class TokenMatchingTraceTest {

    @Test
    fun tokenMatchingTraceIsFormatted() {
        object : Grammar<SyntaxTree>() {
            val a by literalToken("a")
            val b by literalToken("b")
            val cd by literalToken("cd")
            val ab by parser { node(lexeme(a) + lexeme(b)) }
            override val root by ab * parlex(cd) map { (v1, v2) -> node(v1, v2) }
        }.run {
            val input = "abcd"
            val tracedResult = parseTracingTokenMatching(input)
            assertThat(tracedResult.result).isEqualTo(ParsedValue(node(node(a.lex(0), b.lex(1)), cd.lex(2))))
            val formattedTrace = formatTokenMatchingTrace(tracedResult.trace)
            assertThat("\n" + formattedTrace).isEqualTo(
                """
__________
······abcd
      x [0] Token(EOF)
__________
······abcd
      ^ [0 - 0] LiteralToken('a')
__________
·····abcd·
      x [1] Token(EOF)
__________
·····abcd·
      ^ [1 - 1] LiteralToken('b')
__________
····abcd··
      x [2] Token(EOF)
__________
····abcd··
      ^^ [2 - 3] LiteralToken('cd')
__________
··abcd····
      ^ [4 - 4] Token(EOF)
"""
            )
        }
    }

}
