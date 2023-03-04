package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
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

    private var backtrackCont: Continuation<ParseError>? = null
    private var cont: Continuation<Any?>? = null
    private var position: Int = 0
    private var result: Result<Any?> = PENDING_RESULT

    fun <T> runParser(parser: Parser<T>): ParseResult<T> {
        withCont(createParserCoroutine(parser, continuingWith { parsedValue ->
            this.backtrackCont = null
            this.cont = null
            this.result = parsedValue.map(::ParsedValue)
        }))

        runParseLoop()

        @Suppress("UNCHECKED_CAST")
        return result.getOrThrow() as ParseResult<T>
    }

    override val TokenMatch.text: String get() = lexer.input.substring(offset, offset + length)

    override suspend fun <R> Parser<R>.invoke(): R = parse()

    override suspend fun <R> raw(p: Parser<R>): ParseResult<R> = tryParse(p)

    override fun rawToken(token: Token): ParseResult<TokenMatch> {
        val fromIndex = this.position
        val match = lexer.findMatch(fromIndex)
            ?: return NoMatchingToken(fromIndex)
        if (match.token != token) return MismatchedToken(token, match)
        this.position = match.offset + match.length
        return ParsedValue(match)
    }

    override suspend fun <R> any(p1: Parser<R>, p2: Parser<R>): R {
        val curPosition = this.position
        val r1 = tryParse(p1)
        if (r1 is ParsedValue) return r1.value

        val r2 = tryParse(p2)
        if (r2 is ParsedValue) return r2.value

        fail(NoViableAlternative(curPosition))
    }

    override suspend fun fail(error: ParseError): Nothing {
        suspendCoroutineUninterceptedOrReturn<Any?> {
            withCont(backtrackCont) // may be null
            this.result = Result.success(error) // TODO: maybe should additionally wrap into private class
            COROUTINE_SUSPENDED // go back into parse loop
        }
        error("the coroutine must have been cancelled")
    }

    private suspend fun <T> tryParse(parser: Parser<T>): ParseResult<T> {
        return suspendCoroutineUninterceptedOrReturn { mergeCont ->
            val prevBacktrack = this.backtrackCont
            val curPosition = this.position

            val backtrackRestoringCont = continuingWith<T> { parsedValue ->
                // If no exceptions and `fail` is never called while `parser` runs we get here
                this.backtrackCont = prevBacktrack
                // do not restore position, as the input was processed

                withCont(mergeCont)
                this.result = parsedValue.map { ParsedValue(it) }
            }

            val newCont = createParserCoroutine(parser, backtrackRestoringCont)

            // backtrack path
            val newBacktrack = continuingWith<ParseError> {
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
        val doParse: suspend ParsingScope.() -> T = { parser.run { parse() } }
        return doParse.createCoroutineUnintercepted(this, then)
    }

    private fun withCont(continuation: Continuation<*>?) {
        @Suppress("UNCHECKED_CAST")
        this.cont = continuation as Continuation<Any?>?
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
