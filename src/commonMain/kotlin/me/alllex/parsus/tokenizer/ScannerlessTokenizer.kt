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
) : AbstractTokenizer(input, tokens, traceTokenMatching) {

    private val ignoredTokens = tokens.filter { it.ignored }

    // We cache one mismatch and one match of ignored tokens.
    // This is for the frequent case, when there is exactly one ignored token before the target token.
    // Example:
    //   parser = t1 or t2 or t3, ws = ignored whitespace
    //   input = " t3"
    // In this example, t1 will fail to match at 0, but ws will match at 0, so we cache the match.
    // Then t1 will try to match at 1, but it will fail again, so we try ignored tokens again,
    // but this time we get a mismatch, which we cache separately. This fails the t1 branch of the parser.
    // Now, we backtrack and try t2 at 0, which fails.
    // But we can avoid rematching ws at 0, because we cached this match.
    // Then we try t2 at position 1, which fails. But we don't retry ws, because we cached the mismatch.
    // In the last t3 branch, we try t3 at 0, which fails, but then we skip rematching ws at 0,
    // because it is still cached. Then t3 succeeds at 0, and parsing succeeds.
    private var cacheIgnoredMismatchFromIndex = -1
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
        require(fromIndex >= 0) { "fromIndex must be non-negative, but was $fromIndex" }

        if (fromIndex == cacheIgnoredMismatchFromIndex) {
            return null
        }
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

        if (match == null) {
            cacheIgnoredMismatchFromIndex = fromIndex
        } else {
            cachedIgnoredFromIndex = fromIndex
            cachedIgnoredTokenMatch = match
        }
        return match
    }

    private fun matchTarget(pos: Int, targetToken: Token) = matchImpl(pos, targetToken)
}
