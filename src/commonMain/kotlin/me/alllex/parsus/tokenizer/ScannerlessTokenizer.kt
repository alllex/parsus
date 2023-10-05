package me.alllex.parsus.tokenizer

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch

/**
 * Scannerless tokenizer tries to parse the target token at the give position.
 *
 * It treats the target token as having higher priority than all other tokens.
 */
internal class ScannerlessTokenizer(
    input: String,
    tokens: List<Token>,
    traceTokenMatching: Boolean = false,
): AbstractTokenizer(input, tokens, traceTokenMatching) {

    private val ignoredTokens = tokens.filter { it.ignored }

    override fun findContextFreeMatch(fromIndex: Int): TokenMatch? = null

    override fun findMatchOf(fromIndex: Int, targetToken: Token): TokenMatch? {
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
}
