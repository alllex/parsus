package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatcher
import kotlin.coroutines.RestrictsSuspension

/**
 * Provide necessary scope to compose and execute parsers.
 */
@RestrictsSuspension
interface ParsingScope {

    /**
     * Runs [this] parser and returns parsed value.
     *
     * If parser fails, execution is continued at the next alternative.
     */
    suspend operator fun <R> Parser<R>.invoke(): R

    /**
     * Tries to parse given [grammar token][token] at the current position in the input.
     */
    fun <T : TokenMatcher> rawToken(token: Token<T>): ParseResult<TokenMatch<T>>

    /**
     * Runs the parser, returning wrapped result.
     *
     * If this or any underlying parser fails, execution is continued here
     * with a wrapped [error][ParseError].
     */
    suspend fun <R> raw(p: Parser<R>): ParseResult<R>

    /**
     * Returns the result of [the first parser][p1] if parsing succeeds,
     * otherwise returns the result of [the second parser][p2].
     */
    suspend fun <R> any(p1: Parser<R>, p2: Parser<R>): R

    /**
     * Bails out with given [error], continuing execution at the next alternative.
     */
    suspend fun fail(error: ParseError): Nothing

    /**
     * Extracts the text corresponding to the token match from the input.
     */
    val TokenMatch<*>.text: String

    suspend operator fun Parser<*>.unaryMinus(): IgnoredValue = ignoring(this)

    suspend operator fun Parser<Any>.not(): Boolean = having(this)
}

infix fun <R> Parser<R>.or(p: Parser<R>): Parser<R> = parser { any(this@or, p) }

suspend fun <R> ParsingScope.any(p: Parser<R>, vararg ps: Parser<R>): R = any(p, ps.toList())

suspend fun <R> ParsingScope.any(p: Parser<R>, ps: List<Parser<R>>): R {
    if (ps.isEmpty()) return p()

    return any(p, parser {
        val head = ps.first()
        val tail = ps.subList(1, ps.size)
        any(head, tail)
    })
}

suspend fun <R : Any> ParsingScope.trying(p: Parser<R>): R? {
    val r = raw(p)
    return if (r is ParsedValue) r.value else null
}

/**
 * Executes given parser, ignoring the result.
 */
suspend fun ParsingScope.ignoring(p: Parser<*>): IgnoredValue {
    p() // execute parser, but ignore the result
    return IgnoredValue
}

suspend fun ParsingScope.having(p: Parser<Any>): Boolean = trying(p) != null

suspend fun <R : Any> ParsingScope.zeroOrOne(p: Parser<R>): R? = trying(p)

suspend fun <R : Any> ParsingScope.oneOrMore(p: Parser<R>): List<R> = repeating(p, atLeast = 1)

suspend fun <R : Any> ParsingScope.zeroOrMore(p: Parser<R>): List<R> = repeating(p, atLeast = 0)

suspend fun <R : Any> ParsingScope.repeating(p: Parser<R>, atLeast: Int, atMost: Int = -1): List<R> {
    require(atLeast >= 0) { "atLeast must not be negative" }
    require(atMost == -1 || atLeast <= atMost) { "atMost has invalid value" }

    val results = mutableListOf<R>()
    var repetition = 0
    while (atMost == -1 || repetition < atMost) {
        results += trying(p) ?: break
        repetition++
    }

    if (repetition < atLeast) fail(NotEnoughRepetition(atLeast, repetition))
    return results
}

suspend fun <T : Any> ParsingScope.separated(
    term: Parser<T>,
    separator: Parser<Any>,
    allowEmpty: Boolean = true
): List<T> {

    val values = mutableListOf<T>()
    values += if (!allowEmpty) term() else trying(term) ?: return emptyList()
    while (true) {
        trying(separator) ?: break
        values += term()
    }
    return values
}

suspend fun <T : Any, S : Any> ParsingScope.leftAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T {
    var l: T = term()
    while (true) {
        val (o, r) = trying(parser { operator() to term() }) ?: break
        l = transform(l, o, r)
    }
    return l
}

suspend fun <T : Any, S : Any> ParsingScope.rightAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T {
    val stack = mutableListOf<Pair<T, S>>()
    var t = term()
    while (true) {
        val o = trying(operator) ?: break
        stack += t to o
        t = term()
    }

    for ((l, o) in stack.asReversed()) {
        t = transform(l, o, t)
    }
    return t
}
