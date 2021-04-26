package me.alllex.parsus.parser

import me.alllex.parsus.token.Token

/**
 * Parser executes a procedure of converting a portion of input into a value.
 *
 * Parser either succeeds returning a [parsed value][ParsedValue], or fails returning an [error][ParseError].
 * The [scope][ParsingScope] is required to orchestrate composed parsers and propagate errors.
 *
 * The best way of constructing a parser is using a shorthand [parser] function.
 * ```kotlin
 * object : Grammar<*> {
 *     val a by literalToken("a")
 *     val b by literalToken("b")
 *     val p1 by parser { lexeme(a) }
 *     val p2 by parser { lexeme(b) }
 *     override val root by parser {
 *       val t1 = p1().text
 *       val t2 = p2().text
 *       "$t1$t2"
 *     }
 * }
 * ```
 */
interface Parser<out T> {
    suspend fun ParsingScope.parse(): ParseResult<T>
}

/**
 * Converts given [block] into a parser.
 */
@Suppress("FunctionName")
inline fun <T> Parser(crossinline block: suspend ParsingScope.() -> ParseResult<T>): Parser<T> {
    return object : Parser<T> {
        override suspend fun ParsingScope.parse(): ParseResult<T> = block()
    }
}

/**
 * Converts given [block] into a parser.
 */
inline fun <R> parser(crossinline block: suspend ParsingScope.() -> R): Parser<R> = Parser { ParsedValue(block()) }

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
    override fun toString(): String = "ParseError"
}

/**
 * Applies given function to the result of [this] parser.
 * ```kotlin
 *  val int by regexToken("\\d+")
 *  val number by parser { int() } map { it.text.toInt() }
 * ```
 */
inline infix fun <T, R> Parser<T>.map(crossinline f: ParsingScope.(T) -> R): Parser<R> = Parser {
    when (val v = parse()) {
        is ParsedValue -> ParsedValue(f(v.value))
        is ParseError -> v
    }
}

/**
 * When parsing is successful, simply returns given value.
 *
 * It is useful when a parsed token needs to be substituted with a semantic value.
 * ```kotlin
 * interface Marker
 * object MainMarker : Marker
 *
 * object G : Grammar<Marker> {
 *     val main by literalToken("main")
 *     override val root by parser { main() } map MainMarker
 * }
 * ```
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun <T, R> Parser<T>.map(v: R): Parser<R> = map { v }

data class MismatchedToken(val expected: Token<*>, val found: TokenMatch<*>) : ParseError()

data class NoMatchingToken(val fromIndex: Int) : ParseError()

data class NoViableAlternative(val fromIndex: Int) : ParseError()

data class NotEnoughRepetition(val expectedAtLeast: Int, val actualCount: Int) : ParseError()

@Suppress("MemberVisibilityCanBePrivate")
class ParseException(val error: ParseError) : Exception() {
    override fun toString(): String = "ParseException($error)"
}
