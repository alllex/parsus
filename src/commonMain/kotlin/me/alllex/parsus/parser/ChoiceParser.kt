package me.alllex.parsus.parser

import me.alllex.parsus.token.Token

internal class ChoiceParser<out T>(
    val parsers: List<Parser<T>>,
) : ParserImpl<T>(
    null,
    firstTokens = if (parsers.hasUnknownFirstTokens()) emptySet() else parsers.flatMap { it.firstTokens }.toSet()
) {

    private val parsersByFirstToken: Map<Token, List<Parser<T>>> =
        mutableMapOf<Token, MutableList<Parser<T>>>()
            .apply {
                val pendingUnknownFirstTokens = mutableListOf<Parser<T>>()
                for (parser in parsers) {
                    if (parser.hasUnknownFirstTokens()) {
                        pendingUnknownFirstTokens += parser
                        values.forEach { it += parser }
                    } else {
                        for (token in parser.firstTokens) {
                            val parsersForToken = getOrPut(token) { pendingUnknownFirstTokens.toMutableList() }
                            parsersForToken += parser
                        }
                    }
                }
            }

    private val unknownFirstTokenParsers = parsers.filter { it.hasUnknownFirstTokens() }

    override suspend fun ParsingScope.parse(): T {
        // TODO: clean up
//        val currentToken = currentToken?.token ?: fail(NoMatchingToken(currentOffset))
//        val parsers = parsersByFirstToken[currentToken] ?: unknownFirstTokenParsers
        for (parser in parsers) {
            val r = tryParse(parser)
            if (r is ParsedValue) return r.value
        }
        fail(NoViableAlternative(currentOffset))
    }

    companion object {
        private fun Parser<*>.hasUnknownFirstTokens() = firstTokens.isEmpty()
        private fun List<Parser<*>>.hasUnknownFirstTokens() = any { it.hasUnknownFirstTokens() }
    }
}

/**
 * Creates a combined parser that will try the receiver parser first,
 * and fall back to the other parser in case of a parse error.
 *
 * ```kotlin
 *  val id by regexToken("\\[a-z]+")
 *  val int by regexToken("\\d+")
 *  val term by id or (int map { it.text.toInt() })
 * ```
 */
infix fun <R> Parser<R>.or(p: Parser<R>): Parser<R> = when {
    this is ChoiceParser && p is ChoiceParser -> ChoiceParser(parsers + p.parsers)
    this is ChoiceParser -> ChoiceParser(parsers + p)
    p is ChoiceParser -> ChoiceParser(listOf(this) + p.parsers)
    else -> ChoiceParser(listOf(this, p))
}
