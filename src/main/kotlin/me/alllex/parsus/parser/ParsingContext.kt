package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatcher
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.*

/**
 * Executes parsers, keeping track of current position in the input and error-continuations.
 *
 * For each [run][runParser] a new context must be created.
 */
internal class ParsingContext(
    private val lexer: Lexer,
) : ParsingScope {

    private val defaultErrCont: Continuation<ParseError> = continuingWith { r ->
        if (r.isSuccess) {
            val err = r.getOrThrow()
            this.result = Result.failure(ParseException(err))
        } else {
            this.result = r
        }
        this.cont = null
    }

    private var cont: Continuation<Any?>? = null
    private var errCont: Continuation<ParseError> = defaultErrCont
    private var position: Int = 0
    private var result: Result<Any?> = PENDING_RESULT

    fun <T> runParser(parser: Parser<T>): T {
        scheduleParsing(parser, continuingWith {
            this.cont = null
            this.result = it
        })

        runParseLoop()

        val parseResult = result.getOrThrow()

        @Suppress("UNCHECKED_CAST")
        return when (parseResult) {
            is ParsedValue<*> -> (parseResult as ParsedValue<T>).value
            is ParseError -> throw ParseException(parseResult)
            else -> error("unexpected result: $parseResult")
        }
    }

    override val TokenMatch<*>.text: String get() = lexer.input.substring(offset, offset + length)

    override suspend fun <R> Parser<R>.invoke(): R = parseUnwrapping(this)

    override suspend fun <R> raw(p: Parser<R>): ParseResult<R> = parseResult(p)

    override fun <T : TokenMatcher> rawToken(token: Token<T>): ParseResult<TokenMatch<T>> {
        val fromIndex = this.position
        val match = lexer.findMatch(fromIndex)
            ?: return NoMatchingToken(fromIndex)
        if (match.token != token) return MismatchedToken(token, match)
        this.position = match.offset + match.length
        @Suppress("UNCHECKED_CAST")
        return ParsedValue(match) as ParseResult<TokenMatch<T>>
    }

    override suspend fun <R> any(p1: Parser<R>, p2: Parser<R>): R {
        val curPosition = this.position
        val r1 = parseResult(p1)
        if (r1 is ParsedValue) return r1.value

        this.position = curPosition
        val r2 = parseResult(p2)
        if (r2 is ParsedValue) return r2.value

        fail(NoViableAlternative(curPosition))
    }

    override suspend fun fail(error: ParseError): Nothing {
        suspendCoroutineUninterceptedOrReturn<Any?> {
            this.result = Result.success(error)
            withCont(errCont)
            COROUTINE_SUSPENDED
        }
        error("the coroutine must have been cancelled")
    }

    private suspend fun <R> parseResult(p: Parser<R>): ParseResult<R> {
        return suspendCoroutineUninterceptedOrReturn { c ->
            val curErrCont = this.errCont
            val wrappedCont = continuingWith<ParseResult<R>> {
                this.errCont = curErrCont
                c.resumeWith(it)
            }
            this.errCont = wrappedCont
            scheduleParsing(p, wrappedCont)
            COROUTINE_SUSPENDED
        }
    }

    private suspend fun <R> parseUnwrapping(p: Parser<R>): R {
        val parseResult = parseResult(p)
        if (parseResult is ParsedValue) return parseResult.value
        fail(parseResult as ParseError)
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

    private fun <T> scheduleParsing(p: Parser<T>, c: Continuation<ParseResult<T>>) {
        scheduleParsing(c) { p.run { parse() } }
    }

    private fun <T> scheduleParsing(
        c: Continuation<ParseResult<T>>,
        block: suspend ParsingScope.() -> ParseResult<T>,
    ) {
        withCont(block.createCoroutineUnintercepted(this, c))
    }

    private fun withCont(continuation: Continuation<*>) {
        @Suppress("UNCHECKED_CAST")
        this.cont = continuation as Continuation<Any?>
    }

    companion object {
        private val PENDING_RESULT = Result.success(COROUTINE_SUSPENDED)

        private inline fun <T> continuingWith(
            crossinline resumeWith: (Result<T>) -> Unit
        ): Continuation<T> {
            return Continuation(EmptyCoroutineContext, resumeWith)
        }
    }
}
