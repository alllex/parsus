package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import kotlin.coroutines.RestrictsSuspension

/**
 * Provides necessary scope to compose and execute parsers.
 *
 * Instances of the scope are automatically created when parsing [starts][Grammar.parse].
 */
@RestrictsSuspension
interface ParsingScope {

    /**
     * Runs the parser and returns its parsed value.
     *
     * If the parser fails, execution is continued at the next alternative.
     */
    suspend operator fun <R> Parser<R>.invoke(): R

    /**
     * Runs the parser, returning wrapped result.
     *
     * If this or any underlying parser fails, execution is continued here
     * with a wrapped [error][ParseError].
     */
    suspend fun <R> tryParse(p: Parser<R>): ParseResult<R>

    /**
     * Tries to parse given [token] at the current position in the input.
     */
    fun tryParse(token: Token): ParseResult<TokenMatch>

    /**
     * Bails out with given [error], continuing execution at the next alternative.
     */
    suspend fun fail(error: ParseError): Nothing

    /**
     * Current offset in the input
     */
    val currentOffset: Int

    /**
     * The token at the current offset in the input.
     */
    val currentToken: TokenMatch?

    /**
     * Extracts the text corresponding to the token match from the input.
     */
    val TokenMatch.text: String

    suspend operator fun Parser<*>.unaryMinus(): IgnoredValue = skip(this)

    suspend operator fun Parser<Any>.unaryPlus(): Boolean = checkPresent(this)
}
