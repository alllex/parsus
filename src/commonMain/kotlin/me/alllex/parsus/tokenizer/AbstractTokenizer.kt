package me.alllex.parsus.tokenizer

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.trace.TokenMatchingEvent
import me.alllex.parsus.trace.TokenMatchingTrace

@OptIn(ExperimentalParsusApi::class)
internal abstract class AbstractTokenizer(
    override val input: String,
    protected val tokens: List<Token>,
    traceTokenMatching: Boolean = false,
) : Tokenizer {

    private val traceEvents: MutableList<TokenMatchingEvent>? = if (traceTokenMatching) mutableListOf() else null

    override fun getTokenMatchingTrace(): TokenMatchingTrace? {
        return traceEvents?.let { TokenMatchingTrace(input, it) }
    }

    protected fun matchImpl(fromIndex: Int, token: Token): TokenMatch? {
        val length = token.match(input, fromIndex)
        if (length == 0) {
            traceMismatch(token, fromIndex)
            return null
        }

        val match = TokenMatch(token, fromIndex, length)
        traceMatch(token, match)
        return match
    }

    private fun traceMismatch(token: Token, offset: Int) {
        traceEvents?.add(TokenMatchingEvent(token, offset, null))
    }

    private fun traceMatch(token: Token, match: TokenMatch) {
        traceEvents?.add(TokenMatchingEvent(token, match.offset, match))
    }
}
