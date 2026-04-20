package me.alllex.parsus.parser

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.token.EofToken
import me.alllex.parsus.token.Token
import me.alllex.parsus.tokenizer.ScannerlessTokenizer
import me.alllex.parsus.trace.TokenMatchingTrace
import me.alllex.parsus.trace.TracedParseResult
import kotlin.reflect.KProperty

/**
 * Marker interface to scope extensions.
 */
interface GrammarContext

/**
 * Grammar defines all tokens that are expected to occur in the input
 * and the [root] parser which is used to [parse] the resulting value.
 *
 * The tokens must either be [registered][register] manually or using a property-delegate with the `by` keyword.
 * Declaring parsers using `by` is also a preferred way as it allows to use them recursively.
 *
 * ```kotlin
 * // Grammar that parses sums of numbers: 1 + 4 + 2
 * object G : Grammar<Int>() {
 *     init { regexToken("\\s+", ignored = true) }
 *     val lpar by literalToken("(")
 *     val rpar by literalToken(")")
 *     val plus by literalToken("+")
 *     val int by regexToken("\\d+")
 *
 *     val number by parser { int() } map { it.text.toInt() }
 *     val braced by parser { skip(lpar) * root() * skip(rpar) }
 *     val term by number or braced
 *
 *     override val root: Parser<Int> by parser {
 *         leftAssociative(term, plus) { l, _, r -> l + r }
 *     }
 * }
 * ```
 */
abstract class Grammar<out V>(val ignoreCase: Boolean = false) : GrammarContext {

    private val _tokens = mutableListOf<Token>()
    private var freezeTokens = false

    init {
        // important that it is the first
        register(EofToken)
    }

    abstract val root: Parser<V>

    /**
     * Parses entire input with the [root] parser of the grammar
     * and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails the result will be a [ParseError].
     */
    fun parse(input: String): ParseResult<V> {
        return parseEntire(root, input)
    }

    /**
     * Parses entire input with the [root] parser of the grammar
     * and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails throws a [ParseException] containing an error.
     */
    @Throws(ParseException::class)
    fun parseOrThrow(input: String): V {
        return parse(input).getOrThrow()
    }

    /**
     * Parses entire input with the provided [parser] (instead of the [root] of the grammar)
     * and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails the result will be a [ParseError].
     */
    fun <T> parse(parser: Parser<T>, input: String): ParseResult<T> {
        return parseEntire(parser, input)
    }

    /**
     * Parses entire input with the provided [parser] (instead of the [root] of the grammar)
     * and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails throws a [ParseException] containing an error.
     */
    @Throws(ParseException::class)
    fun <T> parseOrThrow(parser: Parser<T>, input: String): T {
        return parse(parser, input).getOrThrow()
    }

    /**
     * Parses entire input with the [root] parser of the grammar
     * and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails the result will be a [ParseError].
     */
    @Deprecated("Use parse instead", ReplaceWith("parse(input)"))
    fun parseEntire(input: String): ParseResult<V> {
        return parse(input)
    }

    /**
     * Parses entire input with the [root] parser of the grammar
     * and returns the parsed value wrapped in the [ParseResult].
     * If parsing fails throws a [ParseException] containing an error.
     */
    @Deprecated("Use parseOrThrow instead", ReplaceWith("parseOrThrow(input)"))
    @Throws(ParseException::class)
    fun parseEntireOrThrow(input: String): V {
        return parseOrThrow(input)
    }

    @ExperimentalParsusApi
    fun parseTracingTokenMatching(input: String): TracedParseResult<V, TokenMatchingTrace> {
        return parseTracingEntire(root, input)
    }

    override fun toString(): String {
        return "Grammar(${_tokens.size} tokens, root = $root)"
    }

    /**
     * Registers a token in the grammar.
     *
     * Tokens must be registered before parsing.
     * Either register them as property delegates or in the init blocks.
     */
    fun register(token: Token) {
        check(!freezeTokens) { "Tokens must be registered before parsing" }
        check(token !in _tokens) { "Token $token is already registered" }

        _tokens += token
    }

    private fun checkRegistered(token: Token) {
        check(token in _tokens) { "Token $token is not registered" }
    }

    protected operator fun <T : Token> T.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): T =
        also {
            if (it.name == null) {
                it.name = property.name
            }
            checkRegistered(it)
        }

    protected operator fun <T : Token> T.getValue(thisRef: Grammar<*>, property: KProperty<*>): T = this

    protected operator fun <R> Parser<R>.provideDelegate(thisRef: Grammar<*>, property: KProperty<*>): Parser<R> =
        also {
            if (it is ParserImpl && it.name == null) {
                it.name = property.name
            }
        }

    protected operator fun <R> Parser<R>.getValue(thisRef: Grammar<*>, property: KProperty<*>): Parser<R> = this

    private fun <T> parseEntire(parser: Parser<T>, input: String): ParseResult<T> {
        beforeParsing()
        // If tokenizer impl is changed to EagerTokenizer, then ChoiceParser impl has to be changed to EagerChoiceParser
        val tokenizer = ScannerlessTokenizer(input, _tokens)
        val parsingContext = ParsingContext(tokenizer)
        return parsingContext.runParser(createUntilEofParser(parser))
    }

    @ExperimentalParsusApi
    private fun <T> parseTracingEntire(parser: Parser<T>, input: String): TracedParseResult<T, TokenMatchingTrace> {
        beforeParsing()
        // If tokenizer impl is changed to EagerTokenizer, then ChoiceParser impl has to be changed to EagerChoiceParser
        val tokenizer = ScannerlessTokenizer(input, _tokens, traceTokenMatching = true)
        val parsingContext = ParsingContext(tokenizer)
        val result = parsingContext.runParser(createUntilEofParser(parser))
        val trace = tokenizer.getTokenMatchingTrace() ?: error("Token matching trace is not available")
        return TracedParseResult(result, trace)
    }

    private fun beforeParsing() {
        freezeTokens = true
    }

    private fun <T> createUntilEofParser(parser: Parser<T>): Parser<T> {
        val untilEofParser = parser {
            val r = parser()
            EofToken()
            r
        }
        return untilEofParser
    }
}

/**
 * Attempts to parse the entire input and returns the parsed value or the default value if parsing fails.
 */
inline fun <V> Grammar<V>.parseOrElse(input: String, default: (ParseError) -> V): V {
    return when (val result = parse(input)) {
        is ParsedValue -> result.value
        is ParseError -> default(result)
    }
}

/**
 * Attempts to parse the entire input and returns the parsed value or `null` if parsing fails.
 */
fun <V> Grammar<V>.parseOrNull(input: String): V? = parseOrElse(input) { null }
