package me.alllex.parsus.pattern

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.token

class PatternToken internal constructor(
    private val pattern: PatternTree,
    name: String? = null,
    ignored: Boolean = false
) : Token(name, ignored) {

    override fun match(input: CharSequence, fromIndex: Int): Int {
        return pattern.match(input, fromIndex)
    }
}

@Suppress("unused")
fun GrammarContext.patternToken(
    pattern: String,
    name: String? = null,
    ignored: Boolean = false
): PatternToken {
    require(pattern.isNotEmpty()) { "pattern must not be empty" }

    val pt = PatternGrammar.parseToEnd(pattern)
    return PatternToken(pt, name, ignored)
}

internal sealed class PatternTree {
    abstract fun match(input: CharSequence, pos: Int): Int
}

internal data class LiteralPattern(val literal: Char) : PatternTree() {
    override fun match(input: CharSequence, pos: Int): Int {
        return if (input[pos] == literal) 1 else 0
    }
}

internal object WildcardPattern : PatternTree() {
    override fun match(input: CharSequence, pos: Int): Int = 1
}

internal data class RangePattern(
    val not: Boolean,
    val ranges: List<CharRange>
) : PatternTree() {

    override fun match(input: CharSequence, pos: Int): Int {
        val c = input[pos]
        val m = ranges.any { c in it }
        return if (m != not) 1 else -1
    }
}

internal data class SequencePattern(
    val patterns: List<PatternTree>
) : PatternTree() {
    override fun match(input: CharSequence, pos: Int): Int {
        var p = pos
        for (pattern in patterns) {
            val r = pattern.match(input, p)
            if (r < 0) return r
            p += r
        }
        return p - pos
    }
}

internal data class AlternativePattern(
    val patterns: List<PatternTree>
) : PatternTree() {
    override fun match(input: CharSequence, pos: Int): Int {
        for (pattern in patterns) {
            val r = pattern.match(input, pos)
            if (r > 0) return r
        }
        return 0
    }
}

internal data class RepeatingPattern(
    val pattern: PatternTree,
    val atLeast: Int,
    val atMost: Int = UNBOUNDED
) : PatternTree() {
    override fun match(input: CharSequence, pos: Int): Int {
        var repeat = 0
        var p = pos
        while (atMost == UNBOUNDED || repeat < atMost) {
            val r = pattern.match(input, p)
            if (r < 0) break
            p += r
            repeat++
        }
        if (repeat < atLeast) return -1
        return p - pos
    }

    companion object {
        const val UNBOUNDED = -1
    }
}

internal object PatternGrammar : Grammar<PatternTree>() {

    private val slash by literalToken("\\")
    private val dot by literalToken(".")
    private val lpar by literalToken("(")
    private val rpar by literalToken("(")
    private val lbr by literalToken("[")
    private val rbr by literalToken("]")
    private val star by literalToken("*")
    private val dash by literalToken("-")
    private val q by literalToken("?")
    private val plus by literalToken("+")
    private val pipe by literalToken("|")
    private val caret by literalToken("^")
    private val aChar by token { input, pos ->
        val c = input[pos]
        if (c == ')' || c == ']') 0 else 1
    }

    private val char by char(aChar)
    private val escapedChar by parser { -slash * char() }
    private val escaped by escapedChar map { LiteralPattern(it) }
    private val wildcard by dot map WildcardPattern

    private val range by parser {
        val c1 = any(escapedChar, char)
        if (!having(dash)) return@parser CharRange(c1, c1)
        val c2 = any(escapedChar, char)
        CharRange(c1, c2)
    }

    private val brackets by parser {
        lbr()
        val not = having(caret)
        val ranges = oneOrMore(range)
        rbr()
        RangePattern(not, ranges)
    }

    private val braces by parser { -lpar * root() * -rpar }

    private val term by wildcard or escaped or brackets or braces

    private val repeatMod by q or star or plus

    private val fragment by parser {
        val t = term()
        val repetition = zeroOrOne(repeatMod) ?: return@parser t
        when (val token = repetition.token) {
            q -> RepeatingPattern(t, atLeast = 0, atMost = 1)
            star -> RepeatingPattern(t, atLeast = 0)
            plus -> RepeatingPattern(t, atLeast = 1)
            else -> error("unexpected token: $token")
        }
    }

    private val seq by parser { oneOrMore(fragment) } map { SequencePattern(it) }

    private val alt by parser { separated(seq, pipe) } map { AlternativePattern(it) }

    private val pattern: Parser<PatternTree> = alt

    override val root = pattern

    private fun char(token: Token): Parser<Char> = token map { it.text[0] }
}
