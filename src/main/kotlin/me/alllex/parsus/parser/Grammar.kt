package me.alllex.parsus.parser

import me.alllex.parsus.token.*
import me.alllex.parsus.token.EofTokenMatcher
import kotlin.reflect.KProperty

/**
 * Grammar defines all tokens that are expected to occur in the input
 * and the [root] parser which is used to [parse][parseToEnd] the resulting value.
 *
 * The tokens must either be [registered][register] manually or using a property-delegate with the `by` keyword.
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
    private val nameTokensFromProperties: Boolean = true
) {

    private val tokens = mutableListOf<Token<*>>()
    private val eofToken = Token(EofTokenMatcher, name = "EOF", skip = false)

    init {
        // important that it is the first
        register(eofToken)
    }

    abstract val root: Parser<V>

    /**
     * If parsing is successful, returns a value.
     * Otherwise, throws a [ParseException] containing an error.
     */
    fun parseToEnd(input: String): V {
        val lexer = Lexer(input, tokens)
        val parsingContext = ParsingContext(lexer)
        val untilEofParser = parser {
            val r = root()
            eofToken()
            r
        }

        return parsingContext.runParser(untilEofParser)
    }

    fun register(token: Token<*>) {
        tokens += token
    }

    protected operator fun <T : TokenMatcher> Token<T>.provideDelegate(
        thisRef: Grammar<*>,
        property: KProperty<*>
    ): Token<T> =
        also {
            if (nameTokensFromProperties && it.name == null) {
                it.name = property.name
            }
            register(it)
        }

    protected operator fun <T : TokenMatcher> Token<T>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Token<T> = this

    protected operator fun <R> Parser<R>.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Parser<R> = this

    protected operator fun <R> Parser<R>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Parser<R> = this
}
