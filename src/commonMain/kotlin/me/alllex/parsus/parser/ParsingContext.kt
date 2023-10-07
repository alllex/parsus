package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.tokenizer.Tokenizer
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * Executes parsers, keeping track of current position in the input and error-continuations.
 *
 * For each [run][runParser] a new context must be created.
 */
internal class ParsingContext(
    private val tokenizer: Tokenizer,
    private val debugMode: Boolean = false
) : ParsingScope {

    private var backtrackCont: Continuation<ParseError>? = null
    private var cont: Continuation<Any?>? = null
    private var position: Int = 0
    private var lastTokenMatchContext = LastTokenMatchContext(tokenizer.input, currentOffset = 0)
    private var result: Result<Any?> = PENDING_RESULT

    fun <T> runParser(parser: Parser<T>): ParseResult<T> {
        withCont(createParserCoroutine(parser, continuingWith(debugName { "Root $parser" }) { parsedValue ->
            this.backtrackCont = null
            this.cont = null
            this.result = parsedValue.map(::ParsedValue)
        }))

        runParseLoop()

        @Suppress("UNCHECKED_CAST")
        return result.getOrThrow() as ParseResult<T>
    }

    override val TokenMatch.text: String get() = tokenizer.input.substring(offset, offset + length)

    override val currentOffset: Int get() = position

    @Deprecated("The new \"scannerless\" parsing approach does not eagerly tokenize the input. The `currentToken` is always null.")
    override val currentToken: TokenMatch?
        get() = tokenizer.findContextFreeMatch(position)

    override suspend fun <R> Parser<R>.invoke(): R = parse()

    override suspend fun <R> tryParse(p: Parser<R>): ParseResult<R> {
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

        // TODO: clean up, as this should not happen anymore
        if (match.token != token) return MismatchedToken(token, match, getParseErrorContextProviderOrNull())

        val newPosition = match.nextOffset
        this.position = newPosition
        this.lastTokenMatchContext.currentOffset = newPosition
        this.lastTokenMatchContext.lastMatch = match

        return ParsedValue(match)
    }

    private fun getParseErrorContextProviderOrNull(): ParseErrorContextProvider {
        return this.lastTokenMatchContext
    }

    override suspend fun fail(error: ParseError): Nothing {
        suspendCoroutineUninterceptedOrReturn<Any?> {
            withCont(backtrackCont) // may be null
            this.result = Result.success(error) // TODO: maybe should additionally wrap into private class
            COROUTINE_SUSPENDED // go back into parse loop
        }
        error("the coroutine must have been cancelled")
    }

    private suspend fun <T> tryParseImpl(parser: Parser<T>): ParseResult<T> {
        return suspendCoroutineUninterceptedOrReturn { mergeCont ->
            val prevBacktrack = this.backtrackCont
            val curPosition = this.position

            val backtrackRestoringCont = continuingWith<T>(debugName { "Forward $parser" }) { parsedValue ->
                // If no exceptions and `fail` is never called while `parser` runs we get here
                this.backtrackCont = prevBacktrack
                // do not restore position, as the input was processed

                withCont(mergeCont)
                this.result = parsedValue.map { ParsedValue(it) }
            }

            val newCont = createParserCoroutine(parser, backtrackRestoringCont)

            // backtrack path
            val newBacktrack = continuingWith<ParseError>(debugName { "Backtrack[$curPosition] $parser" }) {
                // We get here if `fail` is called while `parser` runs
                this.backtrackCont = prevBacktrack
                this.position = curPosition

                withCont(mergeCont)
                this.result = it
            }

            this.result = Result.success(Unit)

            // We'll continue with the happy path
            withCont(newCont)
            // backtracking via `orElse` if the happy path fails
            this.backtrackCont = newBacktrack

            COROUTINE_SUSPENDED // go back into parse loop
        }
    }

    private fun runParseLoop() {
        while (true) {
            val cont = this.cont ?: break
            val resumeValue = this.result

            this.cont = null
            this.result = PENDING_RESULT

            cont.resumeWith(resumeValue)
        }
    }

    private fun <T> createParserCoroutine(parser: Parser<T>, then: Continuation<T>): Continuation<Unit> {
        val doParse: suspend ParsingScope.() -> T = {
            parser.run {
                parse()
            }
        }

        return doParse.createCoroutineUnintercepted(this, then)
    }

    private fun withCont(continuation: Continuation<*>?) {
        @Suppress("UNCHECKED_CAST")
        this.cont = continuation as Continuation<Any?>?
    }

    private inline fun debugName(block: () -> String): String? {
        return if (debugMode) block() else null
    }

    override fun toString(): String {
        return "ParsingContext(position=$position, result=$result)"
    }

    companion object {
        private val PENDING_RESULT = Result.success(COROUTINE_SUSPENDED)

        private inline fun <T> continuingWith(
            debugName: String? = null,
            crossinline resumeWith: (Result<T>) -> Unit
        ): Continuation<T> {
            return if (debugName == null) Continuation(EmptyCoroutineContext, resumeWith)
            else object : Continuation<T> {
                override val context: CoroutineContext get() = EmptyCoroutineContext
                override fun resumeWith(result: Result<T>) = resumeWith(result)
                override fun toString(): String = debugName
            }
        }
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
