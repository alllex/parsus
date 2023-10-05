package me.alllex.parsus.trace

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch


@ExperimentalParsusApi
data class TokenMatchingEvent(
    val token: Token,
    val offset: Int,
    val match: TokenMatch?,
)

@ExperimentalParsusApi
data class TokenMatchingTrace(
    val input: String,
    val events: List<TokenMatchingEvent>,
)

@ExperimentalParsusApi
fun formatTokenMatchingTrace(
    trace: TokenMatchingTrace,
    lookBehind: Int = 5,
    lookAhead: Int = 20,
): String {
    val sb = StringBuilder()
    val input = trace.input
    var lastMismatchOffset = -1
    for (event in trace.events) {
        val offset = event.offset
        val match = event.match
        val matchLength = match?.length ?: 0

        // avoid re-printing the input line, when the previous event was *also* a mismatch at the same offset
        if (match != null || offset != lastMismatchOffset) {
            val rawToOffset = offset + matchLength + lookAhead
            val toOffset = rawToOffset.coerceAtMost(input.length)
            val displayLineLength = lookBehind + matchLength + lookAhead + 1
            sb.append("_".repeat(displayLineLength.coerceAtMost(input.length)))
            sb.appendLine()

            val prefix = when {
                offset <= lookBehind -> " ".repeat(lookBehind - offset + 1) + input.substring(0, offset)
                else -> "…" + input.substring(offset - lookBehind, offset)
            }
            sb.append(prefix)

            sb.append(input.substring(offset, toOffset))
            if (toOffset < input.length) {
                sb.append("…")
            }
            sb.appendLine()
        }

        lastMismatchOffset = if (match != null) -1 else offset

        val matchSymbol = if (match != null) "^" else "x"
        sb.append(" ".repeat(lookBehind + 1))
        sb.append(matchSymbol.repeat(matchLength.coerceAtLeast(1)))
        sb.append(" [$offset").append(if (match != null) " - ${offset + matchLength - 1}" else "")
            .append("] ").append(event.token)
        sb.appendLine()
    }
    return sb.toString()
}
