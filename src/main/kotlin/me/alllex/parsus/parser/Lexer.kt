package me.alllex.parsus.parser

import me.alllex.parsus.token.Token

/**
 * Lexer is responsible for [finding][findMatch] token-matches in the given position
 * in the input string.
 */
internal class Lexer(
    val input: String,
    private val tokens: List<Token<*>>,
) {

    private val tokensByFirstChar: Map<Char, List<Token<*>>>
    private var cachedFromIndex: Int = -1
    private var cachedTokenMatch: TokenMatch<*>? = null

    init {
        tokensByFirstChar = mutableMapOf<Char, MutableList<Token<*>>>()
        for (grammarToken in tokens) {
            val firstChars = grammarToken.matcher.firstChars
            for (c in firstChars) {
                tokensByFirstChar.getOrPut(c) { mutableListOf() }.add(grammarToken)
            }
        }
    }

    fun findMatch(fromIndex: Int): TokenMatch<*>? {
        if (fromIndex == cachedFromIndex && cachedTokenMatch != null) {
            return cachedTokenMatch
        }

        val foundTokenMatch = findMatchIgnoring(fromIndex)
        cachedFromIndex = fromIndex
        cachedTokenMatch = foundTokenMatch
        return foundTokenMatch
    }

    private fun findMatchIgnoring(fromIndex: Int): TokenMatch<*>? {
        var pos = fromIndex
        while (true) {
            val lex = findMatchImpl(pos) ?: return null
            if (!lex.token.skip) return lex
            pos = lex.offset + lex.length
        }
    }

    private fun findMatchImpl(fromIndex: Int): TokenMatch<*>? {
        if (fromIndex < input.length) {
            val nextChar = input[fromIndex]
            val byFirstChar = tokensByFirstChar[nextChar].orEmpty()
            for (i in byFirstChar.indices) {
                val grammarToken = byFirstChar[i]
                matchImpl(fromIndex, grammarToken)?.let { return it }
            }
        }

        for (i in tokens.indices) {
            val grammarToken = tokens[i]
            matchImpl(fromIndex, grammarToken)?.let { return it }
        }
        return null
    }

    private fun matchImpl(fromIndex: Int, token: Token<*>): TokenMatch<*>? {
        val length = token.matcher.match(input, fromIndex)
        if (length <= 0) return null
        return TokenMatch(token, fromIndex, length)
    }
}
