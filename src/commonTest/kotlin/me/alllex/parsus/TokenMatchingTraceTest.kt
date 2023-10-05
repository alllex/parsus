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
            val input = " a ef  b cd "
            val tracedResult = parseTracingTokenMatching(input)
            assertThat(tracedResult.result).isEqualTo(ParsedValue(
                node(node(a.lex(1), ef.lex(3)), node(b.lex(7), cd.lex(9))))
            )
            val formattedTrace = formatTokenMatchingTrace(tracedResult.trace)
            println(formattedTrace)
            assertThat("\n" + formattedTrace).isEqualTo(
                """
__________________
······␣a␣ef␣␣b␣cd␣
      x [0] a LiteralToken('a')
__________________
······␣a␣ef␣␣b␣cd␣
      ^ [0 - 0] ws RegexToken(ws [\s+] [ignored])
__________________
·····␣a␣ef␣␣b␣cd␣·
      ^ [1 - 1] a LiteralToken('a')
__________________
····␣a␣ef␣␣b␣cd␣··
      x [2] cd LiteralToken('cd')
__________________
····␣a␣ef␣␣b␣cd␣··
      ^ [2 - 2] ws RegexToken(ws [\s+] [ignored])
__________________
···␣a␣ef␣␣b␣cd␣···
      x [3] cd LiteralToken('cd')
      x [3] ws RegexToken(ws [\s+] [ignored])
__________________
····␣a␣ef␣␣b␣cd␣··
      x [2] ef LiteralToken('ef')
__________________
···␣a␣ef␣␣b␣cd␣···
      ^^ [3 - 4] ef LiteralToken('ef')
__________________
·␣a␣ef␣␣b␣cd␣·····
      x [5] a LiteralToken('a')
__________________
·␣a␣ef␣␣b␣cd␣·····
      ^^ [5 - 6] ws RegexToken(ws [\s+] [ignored])
__________________
…␣ef␣␣b␣cd␣·······
      x [7] a LiteralToken('a')
      x [7] ws RegexToken(ws [\s+] [ignored])
__________________
·␣a␣ef␣␣b␣cd␣·····
      x [5] b LiteralToken('b')
__________________
…␣ef␣␣b␣cd␣·······
      ^ [7 - 7] b LiteralToken('b')
__________________
…ef␣␣b␣cd␣········
      x [8] cd LiteralToken('cd')
__________________
…ef␣␣b␣cd␣········
      ^ [8 - 8] ws RegexToken(ws [\s+] [ignored])
__________________
…f␣␣b␣cd␣·········
      ^^ [9 - 10] cd LiteralToken('cd')
__________________
…␣b␣cd␣···········
      x [11] a LiteralToken('a')
__________________
…␣b␣cd␣···········
      ^ [11 - 11] ws RegexToken(ws [\s+] [ignored])
__________________
…b␣cd␣············
      x [12] a LiteralToken('a')
      x [12] ws RegexToken(ws [\s+] [ignored])
__________________
…␣b␣cd␣···········
      x [11] b LiteralToken('b')
__________________
…b␣cd␣············
      x [12] b LiteralToken('b')
__________________
…␣b␣cd␣···········
      x [11] EOF Token(EOF)
__________________
…b␣cd␣············
      ^ [12 - 12] EOF Token(EOF)
"""
            )
        }
    }

}
