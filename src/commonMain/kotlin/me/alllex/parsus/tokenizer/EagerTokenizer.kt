package me.alllex.parsus.tokenizer

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch

/**
 * This tokenizer eagerly tries to match tokens from the input,
 * based on the full token set ordered by priority.
 * It deterministically matches tokens from the input,
 * not taking into account tokens expected by parsers.
 */
internal class EagerTokenizer(
    input: String,
    tokens: List<Token>,
    traceTokenMatching: Boolean = false,
) : AbstractTokenizer(input, tokens, traceTokenMatching) {

    private val tokensByFirstChar: Map<Char, List<Token>>
    private var cachedFromIndex: Int = -1
    private var cachedTokenMatch: TokenMatch? = null

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

    override fun findContextFreeMatch(fromIndex: Int): TokenMatch? {
        return findMatchCaching(fromIndex)
    }

    override fun findMatchOf(fromIndex: Int, targetToken: Token): TokenMatch? {
        return findMatchCaching(fromIndex)
    }

    private fun findMatchCaching(fromIndex: Int): TokenMatch? {
        if (fromIndex == cachedFromIndex && cachedTokenMatch != null) {
            return cachedTokenMatch
        }

        val foundTokenMatch = findMatchIgnoring(fromIndex)
        cachedFromIndex = fromIndex
        cachedTokenMatch = foundTokenMatch
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
}
