package me.alllex.parsus.trace

import me.alllex.parsus.annotations.ExperimentalParsusApi
import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatch
import me.alllex.parsus.util.replaceNonPrintable


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

    val input = trace.input.let { rawInput ->
        buildString {
            for (char in rawInput) {
                append(replaceNonPrintable(char))
            }
        }
    }

    val sb = StringBuilder()
    var lastMismatchOffset = -1
    for (event in trace.events) {
        val offset = event.offset
        val match = event.match
        val matchLength = match?.length ?: 0

        // avoid re-printing the input line, when the previous event was *also* a mismatch at the same offset
        if (match != null || offset != lastMismatchOffset) {
            val rawToOffset = offset + matchLength + lookAhead
            val toOffset = rawToOffset.coerceAtMost(input.length)
            val inputDisplayLineLength = lookBehind + (matchLength + lookAhead).coerceAtMost(input.length) + 1
            sb.append("_".repeat(inputDisplayLineLength))
            sb.appendLine()

            var inputDisplayLinePrintedLength = 0
            val prefix = when {
                offset <= lookBehind -> "·".repeat(lookBehind - offset + 1) + input.substring(0, offset)
                else -> "…" + input.substring(offset - lookBehind, offset)
            }
            sb.append(prefix)
            inputDisplayLinePrintedLength += prefix.length

            val inputChunkAtOffset = input.substring(offset, toOffset)
            sb.append(inputChunkAtOffset)
            inputDisplayLinePrintedLength += inputChunkAtOffset.length

            if (toOffset < input.length) {
                sb.append("…")
                inputDisplayLinePrintedLength += 1
            }

            if (inputDisplayLinePrintedLength < inputDisplayLineLength) {
                sb.append("·".repeat(inputDisplayLineLength - inputDisplayLinePrintedLength))
            }
            sb.appendLine()
        }

        lastMismatchOffset = if (match != null) -1 else offset

        val matchSymbol = if (match != null) "^" else "x"
        sb.append(" ".repeat(lookBehind + 1))
        sb.append(matchSymbol.repeat(matchLength.coerceAtLeast(1)))
        sb.append(" [$offset").append(if (match != null) " - ${offset + matchLength - 1}" else "")
            .append("] ").append(event.token.name?.let { "$it " } ?: "").append(event.token)
        sb.appendLine()
    }
    return sb.toString()
}
