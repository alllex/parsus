package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
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
     * Tries to parse given [token] at the current position in the input.
     */
    fun rawToken(token: Token): ParseResult<TokenMatch>

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
    val TokenMatch.text: String

    /**
     * Current offset in the input
     */
    val currentOffset: Int

    suspend operator fun Parser<*>.unaryMinus(): IgnoredValue = ignoring(this)

    suspend operator fun Parser<Any>.not(): Boolean = having(this)
}
