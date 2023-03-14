package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import kotlin.reflect.KProperty0

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

    /**
     * Executes the parser in the parsing scope and returns the result on success.
     *
     * This method should not be called directly, use [invoke operator][ParsingScope.invoke] instead: `myParser()`.
     */
    suspend fun ParsingScope.parse(): T

    /**
     * Full set of tokes that the parser expects as the first token in the input.
     *
     * **Important:** it should be all possible tokens if the parser can handle variations.
     * If the full set is not known, this method must return an empty set.
     */
    val firstTokens: Set<Token> get() = emptySet()
}

@PublishedApi
internal abstract class ParserImpl<out T>(
    var name: String? = null,
    override val firstTokens: Set<Token> = emptySet(),
) : Parser<T> {
    override fun toString(): String = name ?: super.toString()
}

/**
 * Converts given [block] into a parser.
 *
 * Optionally, the [firstTokens] set can be specified to improve performance.
 * The set must be empty if the first tokens are not strictly known.
 * See [Parser.firstTokens] for more details.
 */
inline fun <T> parser(
    name: String,
    firstTokens: Set<Token> = emptySet(),
    crossinline block: suspend ParsingScope.() -> T
): Parser<T> {
    return object : ParserImpl<T>(name, firstTokens = firstTokens) {
        override suspend fun ParsingScope.parse(): T = block()
        override val firstTokens: Set<Token> get() = firstTokens
    }
}

/**
 * Converts given [block] into a parser.
 *
 * Optionally, the [firstTokens] set can be specified to improve performance.
 * The set must be empty if the first tokens are not strictly known.
 * See [Parser.firstTokens] for more details.
 */
inline fun <T> parser(
    firstTokens: Set<Token> = emptySet(),
    crossinline block: suspend ParsingScope.() -> T
): Parser<T> {
    return object : ParserImpl<T>(firstTokens = firstTokens) {
        override suspend fun ParsingScope.parse(): T = block()
    }
}

/**
 * Returns a reference to a parser to allow defining recursive parsers via combinators.
 *
 * ```kotlin
 * val lpar by literalToken("(")
 * val rpar by literalToken(")")
 * val item by -lpar * ref(::item) * -rpar
 * ```
 */
fun <T> ref(
    parserProperty: KProperty0<Parser<T>>
) : Parser<T> {
    return object : ParserImpl<T>(name = parserProperty.name, firstTokens = emptySet()) {
        private val parser by lazy { parserProperty() }
        override suspend fun ParsingScope.parse(): T = parser.invoke()
        override fun toString(): String = "ref(${name ?: parser.toString()})"
    }
}
