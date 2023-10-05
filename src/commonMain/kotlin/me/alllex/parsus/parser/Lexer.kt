package me.alllex.parsus.parser

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.trace.TokenMatchingEvent
import me.alllex.parsus.trace.TokenMatchingTrace

/**
 * Lexer is responsible for [finding][findMatch] token-matches in the given position
 * in the input string.
 */
@OptIn(ExperimentalParsusApi::class)
internal class Lexer(
    val input: String,
    private val tokens: List<Token>,
    traceTokenMatching: Boolean = false,
) {

    private val ignoredTokens = tokens.filter { it.ignored }
    private val tokensByFirstChar: Map<Char, List<Token>>
//    private var cachedFromIndex: Int = -1
//    private var cachedTokenMatch: TokenMatch? = null

    private val traceEvents: MutableList<TokenMatchingEvent>? = if (traceTokenMatching) mutableListOf() else null

    init {
        tokensByFirstChar = mutableMapOf<Char, MutableList<Token>>()
        val unknownFirstCharTokens = mutableListOf<Token>()
        for (token in tokens) {
            val firstChars = token.firstChars
            if (firstChars.isEmpty()) {
                // If the token first char is unknown, then the first char heuristic cannot be applied.
                // Therefore, we assume that such tokens can start with any character and put them in appropriate buckets
                // to ensure the token priority correctness.
                unknownFirstCharTokens += token
                tokensByFirstChar.values.forEach { it += token }
            } else {
                for (c in firstChars) {
                    tokensByFirstChar.getOrPut(c) { unknownFirstCharTokens.toMutableList() }
                        .add(token)
                }
            }
        }
    }

    internal fun getTokenMatchingTrace(): TokenMatchingTrace? {
        return traceEvents?.let { TokenMatchingTrace(input, it) }
    }

    fun findMatchOf(fromIndex: Int, targetToken: Token): TokenMatch? {
        var pos = fromIndex
        while (true) {
            matchImpl(pos, targetToken)?.let { return it }

            val preIgnorePos = pos
            for (ignoredToken in ignoredTokens) {
                val ignoredMatch = matchImpl(pos, ignoredToken)
                if (ignoredMatch != null) {
                    pos = ignoredMatch.offset + ignoredMatch.length
                    break
                }
            }

            if (preIgnorePos == pos) {
                // No ignored tokens matched, so we can't find the target token
                return null
            }
        }
        // The loop will exit via a mismatch, because no tokens can match "after the end of input"
    }

    fun findMatch(fromIndex: Int): TokenMatch? {
//        if (fromIndex == cachedFromIndex && cachedTokenMatch != null) {
//            return cachedTokenMatch
//        }

        val foundTokenMatch = findMatchIgnoring(fromIndex)
//        cachedFromIndex = fromIndex
//        cachedTokenMatch = foundTokenMatch
        return foundTokenMatch
    }

    private fun findMatchIgnoring(fromIndex: Int): TokenMatch? {
        var pos = fromIndex
        while (true) {
            val lex = findMatchImpl(pos) ?: return null
            if (lex.token.ignored) {
                pos = lex.offset + lex.length
                continue
            }

            return lex
        }
    }

    private fun findMatchImpl(fromIndex: Int): TokenMatch? {
        if (fromIndex < input.length) {
            val nextChar = input[fromIndex]
            val byFirstChar = tokensByFirstChar[nextChar].orEmpty()
            for (token in byFirstChar) {
                matchImpl(fromIndex, token)?.let { return it }
            }
        }

        for (token in tokens) {
            matchImpl(fromIndex, token)?.let { return it }
        }
        return null
    }

    private fun matchImpl(fromIndex: Int, token: Token): TokenMatch? {
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
