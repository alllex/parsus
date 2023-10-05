package me.alllex.parsus

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import me.alllex.parsus.trace.formatTokenMatchingTrace
import me.alllex.parsus.tree.SyntaxTree
import kotlin.test.Test

@OptIn(ExperimentalParsusApi::class)
class TokenMatchingTraceTest {

    @Test
    fun tokenMatchingTraceIsFormatted() {
        object : Grammar<SyntaxTree>() {
            @Suppress("unused")
            val ws by regexToken("\\s+", ignored = true)
            val a by literalToken("a")
            val b by literalToken("b")
            val cd by literalToken("cd")
            val ef by literalToken("ef")
            val aOrB by parlex(a) or parlex(b)
            val cdOrEf by parlex(cd) or parlex(ef)
            val p by aOrB * cdOrEf map { (v1, v2) -> node(v1, v2) }
            override val root by oneOrMore(p) map { node(it) }
        }.run {
            val input = "aefbcd"
            val tracedResult = parseTracingTokenMatching(input)
            assertThat(tracedResult.result).isEqualTo(ParsedValue(
                node(node(a.lex(0), ef.lex(1)), node(b.lex(3), cd.lex(4))))
            )
            val formattedTrace = formatTokenMatchingTrace(tracedResult.trace)
            println(formattedTrace)
            assertThat("\n" + formattedTrace).isEqualTo(
                """
____________
······aefbcd
      ^ [0 - 0] a LiteralToken('a')
____________
·····aefbcd·
      x [1] cd LiteralToken('cd')
      x [1] ws RegexToken(ws [\s+] [ignored])
____________
·····aefbcd·
      ^^ [1 - 2] ef LiteralToken('ef')
____________
···aefbcd···
      x [3] a LiteralToken('a')
      x [3] ws RegexToken(ws [\s+] [ignored])
____________
···aefbcd···
      ^ [3 - 3] b LiteralToken('b')
____________
··aefbcd····
      ^^ [4 - 5] cd LiteralToken('cd')
____________
…efbcd······
      x [6] a LiteralToken('a')
      x [6] ws RegexToken(ws [\s+] [ignored])
      x [6] b LiteralToken('b')
      x [6] ws RegexToken(ws [\s+] [ignored])
____________
…efbcd······
      ^ [6 - 6] EOF Token(EOF)
"""
            )
        }
    }

}
