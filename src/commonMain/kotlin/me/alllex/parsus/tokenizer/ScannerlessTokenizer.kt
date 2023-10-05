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

    private var cachedIgnoredFromIndex: Int = -1
    private var cachedIgnoredTokenMatch: TokenMatch? = null

    override fun findContextFreeMatch(fromIndex: Int): TokenMatch? = null

    override fun findMatchOf(fromIndex: Int, targetToken: Token): TokenMatch? {
        var pos = fromIndex
        while (true) {
            matchTarget(pos, targetToken)?.let { return it }

            val ignoredMatch = matchIgnored(pos)
            @Suppress("LiftReturnOrAssignment")
            if (ignoredMatch != null) {
                val posAfterIgnored = ignoredMatch.offset + ignoredMatch.length
                if (posAfterIgnored > pos) {
                    pos = posAfterIgnored
                    continue
                } else {
                    // An ignored token matched, but it did not advance the position.
                    // This should not happen normally, but this is a safeguard.
                    return null
                }
            } else {
                // No ignored tokens matched at the current position either,
                // so it is a mismatch overall
                return null
            }
        }
        // The loop will exit via a mismatch, because no tokens can match "after the end of input"
    }

    private fun matchIgnored(fromIndex: Int): TokenMatch? {
        if (fromIndex == cachedIgnoredFromIndex) {
            return cachedIgnoredTokenMatch
        }

        var match: TokenMatch? = null
        for (ignoredToken in ignoredTokens) {
            match = matchImpl(fromIndex, ignoredToken)
            if (match != null) {
                break
            }
        }

        cachedIgnoredFromIndex = fromIndex
        cachedIgnoredTokenMatch = match
        return match
    }

    private fun matchTarget(pos: Int, targetToken: Token) = matchImpl(pos, targetToken)
}
