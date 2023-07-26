package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch

/**
 * Result of a parse that is either a [parsed value][ParsedValue]
 * or an [error][ParseError].
 */
sealed class ParseResult<out T>

/**
 * Successful result of parsing.
 */
data class ParsedValue<T>(val value: T) : ParseResult<T>()

/**
 * Result of failed parsing.
 */
abstract class ParseError : ParseResult<Nothing>() {
    /**
     * Offset in the input at which the parsing error occurred
     */
    abstract val offset: Int

    override fun toString(): String = "ParseError"
}

data class MismatchedToken(val expected: Token, val found: TokenMatch) : ParseError() {
    override val offset: Int get() = found.offset
}
data class NoMatchingToken(override val offset: Int) : ParseError()
data class NoViableAlternative(override val offset: Int) : ParseError()
data class NotEnoughRepetition(override val offset: Int, val expectedAtLeast: Int, val actualCount: Int) : ParseError()

class ParseException(val error: ParseError) : Exception() {
    override fun toString(): String = "ParseException($error)"
}

inline fun <T, R> ParseResult<T>.map(f: (T) -> R): ParseResult<R> {
    return when (this) {
        is ParsedValue -> ParsedValue(f(value))
        is ParseError -> this
    }
}

inline fun <T> ParseResult<T>.getOrElse(f: (ParseError) -> T): T {
    return when (this) {
        is ParsedValue -> value
        is ParseError -> f(this)
    }
}

@Throws(ParseException::class)
fun <T> ParseResult<T>.getOrThrow(): T {
    return when (this) {
        is ParsedValue -> value
        is ParseError -> throw ParseException(this)
    }
}
