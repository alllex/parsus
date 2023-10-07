package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.util.replaceNonPrintable

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

    open val contextProvider: ParseErrorContextProvider? get() = null

    abstract fun describe(): String

    override fun toString(): String = describe()

    protected fun format(message: String, messageAtOffset: String): String = buildString {
        append(message)
        contextProvider?.getParseErrorContext(offset)?.run {
            appendLine()
            append(" ".repeat(lookBehind)).append(messageAtOffset)
            appendLine()
            append(" ".repeat(lookBehind)).append("| (or after ignored tokens)")
            appendLine()
            appendLine(replaceNonPrintable(inputSection))
            if (previousTokenMatch != null) {
                append("^".repeat(previousTokenMatch.length.coerceAtLeast(1)))
                append(" Previous token: ${previousTokenMatch.token} at offset=${previousTokenMatch.offset}")
                appendLine()
            }
        }
    }
}

data class ParseErrorContext(
    val inputSection: String,
    val lookBehind: Int,
    val lookAhead: Int,
    val previousTokenMatch: TokenMatch?,
)

fun interface ParseErrorContextProvider {
    fun getParseErrorContext(offset: Int): ParseErrorContext?
}

data class UnmatchedToken(
    val expected: Token,
    override val offset: Int,
    override val contextProvider: ParseErrorContextProvider? = null
) : ParseError() {

    override fun describe(): String = format(
        message = "Unmatched token at offset=$offset, when expected: $expected",
        messageAtOffset = "Expected token: $expected at offset=$offset"
    )
}

data class MismatchedToken(val expected: Token, val found: TokenMatch) : ParseError() {
    override val offset: Int get() = found.offset
    override fun describe(): String = "Expected $expected, found $found"
}
data class NoMatchingToken(override val offset: Int) : ParseError() {
    override fun describe(): String = "No matching token"
}
data class NoViableAlternative(override val offset: Int) : ParseError() {
    override fun describe(): String = "No viable alternative"
}
data class NotEnoughRepetition(override val offset: Int, val expectedAtLeast: Int, val actualCount: Int) : ParseError() {
    override fun describe(): String = "Expected at least $expectedAtLeast, found $actualCount"
}

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
