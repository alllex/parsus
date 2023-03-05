package me.alllex.parsus.parser

import me.alllex.parsus.token.EofToken
import me.alllex.parsus.token.Token
import kotlin.reflect.KProperty

/**
 * Marker interface to scope extensions.
 */
interface GrammarContext

/**
 * Grammar defines all tokens that are expected to occur in the input
 * and the [root] parser which is used to [parse][parseEntire] the resulting value.
 *
 * The tokens must either be [registered][registerToken] manually or using a property-delegate with the `by` keyword.
 * Declaring parsers using `by` is also a preferred way as it allows to use them recursively.
 *
 * ```kotlin
 * // Grammar that parses sums of numbers: 1 + 4 + 2
 * object G : Grammar<Int>() {
 *     init { register(regexToken("\\s+", ignored = true)) }
 *     val lpar by literalToken("(")
 *     val rpar by literalToken(")")
 *     val plus by literalToken("+")
 *     val int by regexToken("\\d+")
 *
 *     val number by parser { int() } map { it.text.toInt() }
 *     val braced by parser { -lpar * root() * -rpar }
 *     val term by number or braced
 *
 *     override val root: Parser<Int> by parser {
 *         leftAssociative(term, plus) { l, _, r -> l + r }
 *     }
 * }
 * ```
 */
abstract class Grammar<out V>(
    private val debugMode: Boolean = false,
) : GrammarContext {

    private val _tokens = mutableListOf<Token>()

    init {
        // important that it is the first
        registerToken(EofToken)
    }

    abstract val root: Parser<V>

    /**
     * Parses entire input and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails the result will be a [ParseError].
     */
    fun parseEntire(input: String): ParseResult<V> {
        val lexer = Lexer(input, _tokens)
        val parsingContext = ParsingContext(lexer, debugMode)
        val untilEofParser = parser {
            val r = root()
            EofToken()
            r
        }

        return parsingContext.runParser(untilEofParser)
    }

    /**
     * If parsing is successful, returns a value.
     * Otherwise, throws a [ParseException] containing an error.
     */
    fun parseEntireOrThrow(input: String): V {
        return parseEntire(input).getOrThrow()
    }

    /**
     * If parsing is successful, returns a value.
     * Otherwise, throws a [ParseException] containing an error.
     */
    @Deprecated("Use `parseEntireOrThrow` instead", ReplaceWith("this.parseEntireOrThrow(input)"), DeprecationLevel.WARNING)
    fun parseToEnd(input: String): V = parseEntireOrThrow(input)

    protected fun register(vararg tokens: Token) {
        tokens.forEach { registerToken(it) }
    }

    protected fun registerToken(token: Token) {
        _tokens += token
    }

    protected operator fun <T : Token> T.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): T =
        also {
            if (it.name == null) {
                it.name = property.name
            }
            registerToken(it)
        }

    protected operator fun <T : Token> T.getValue(thisRef: Grammar<*>, property: KProperty<*>): T = this

    protected operator fun <R> Parser<R>.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Parser<R> =
        also {
            if (it is ParserImpl && it.name == null) {
                it.name = property.name
            }
        }

    protected operator fun <R> Parser<R>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Parser<R> = this
}
