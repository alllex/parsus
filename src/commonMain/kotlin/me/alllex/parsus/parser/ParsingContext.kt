package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.tokenizer.Tokenizer
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.startCoroutine

/**
 * Executes parsers, keeping track of current position in the input and error-continuations.
 *
 * For each [run][runParser] a new context must be created.
 */
internal class ParsingContext(private val tokenizer: Tokenizer) : ParsingScope {

    private val inputLength = tokenizer.input.length

    private var position: Int = 0
    private var result: ParseResult<*>? = null
    private var lastTokenMatchContext = LastTokenMatchContext(tokenizer.input, currentOffset = 0)

    fun <T> runParser(parser: Parser<T>): ParseResult<T> = tryParseImpl(parser)

    override val TokenMatch.text: String get() = tokenizer.input.substring(offset, offset + length)

    override val currentOffset: Int get() = position

    @Deprecated("The new \"scannerless\" parsing approach does not eagerly tokenize the input. The `currentToken` is always null.")
    override val currentToken: TokenMatch?
        get() = tokenizer.findContextFreeMatch(position)

    override suspend fun <R> Parser<R>.invoke(): R = parse()

    override fun <R> tryParse(p: Parser<R>): ParseResult<R> {
        if (p is Token) {
            val tr = tryParse(p)
            @Suppress("UNCHECKED_CAST")
            return tr as ParseResult<R> // Token can only be a `Parser<TokenMatch>`
        }
        return tryParseImpl(p)
    }

    override fun tryParse(token: Token): ParseResult<TokenMatch> {
        val fromIndex = this.position
        val match = tokenizer.findMatchOf(fromIndex, token)
            ?: return UnmatchedToken(token, fromIndex, getParseErrorContextProviderOrNull())

        // This can only happen with EagerTokenizer
        if (match.token != token) return MismatchedToken(token, match, getParseErrorContextProviderOrNull())

        val newPosition = match.nextOffset.coerceAtMost(inputLength)
        this.position = newPosition
        this.lastTokenMatchContext.currentOffset = newPosition
        this.lastTokenMatchContext.lastMatch = match

        return ParsedValue(match)
    }

    private fun getParseErrorContextProviderOrNull(): ParseErrorContextProvider {
        return this.lastTokenMatchContext
    }

    override suspend fun fail(error: ParseError): Nothing {
        this.result = error
        suspendCoroutineUninterceptedOrReturn<Nothing> { COROUTINE_SUSPENDED }
    }

    // It's equivalent to: try { parser.parse() } catch { this.position = curPosition }.
    // The whole suspend machinery mimics checked exceptions
    private fun <T> tryParseImpl(parser: Parser<T>): ParseResult<T> {
        val curPosition = this.position
        val block: suspend ParsingScope.() -> ParseResult<T> = { parser.run { ParsedValue(parse()) } }
        block.startCoroutine(this, Continuation(EmptyCoroutineContext) { res -> result = res.getOrThrow() })
        @Suppress("UNCHECKED_CAST")
        val res = result!!.map { it as T }
        if (res is ParseError) this.position = curPosition
        return res
    }

    override fun toString(): String {
        return "ParsingContext(position=$position, result=${result})"
    }
}

internal class LastTokenMatchContext(
    val input: String,
    var currentOffset: Int,
    var lastMatch: TokenMatch? = null,
) : ParseErrorContextProvider {

    override fun toString() = "LastTokenMatchContext(currentOffset=$currentOffset, lastMatch=$lastMatch)"

    override fun getParseErrorContext(offset: Int): ParseErrorContext? {
        if (offset != currentOffset) {
            return null
        }

        val lastMatch = this.lastMatch
        val lookAhead = 20
        return if (lastMatch == null || lastMatch.nextOffset != offset) {
            ParseErrorContext(
                inputSection = getInputSection(offset, offset + lookAhead),
                lookBehind = 0,
                lookAhead = lookAhead,
                previousTokenMatch = null
            )
        } else {
            ParseErrorContext(
                inputSection = getInputSection(lastMatch.offset, lastMatch.nextOffset + lookAhead),
                lookBehind = lastMatch.length,
                lookAhead = lookAhead,
                previousTokenMatch = lastMatch
            )
        }
    }

    private fun getInputSection(inputSectionStart: Int, inputSectionStop: Int) =
        input.substring(inputSectionStart, inputSectionStop.coerceAtMost(input.length))
}
