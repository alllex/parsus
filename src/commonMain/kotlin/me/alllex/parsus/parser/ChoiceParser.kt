package me.alllex.parsus.parser

import me.alllex.parsus.token.Token

internal class ChoiceParser<out T>(
    val parsers: List<Parser<T>>,
) : ParserImpl<T>(
    null,
    firstTokens = if (parsers.hasUnknownFirstTokens()) emptySet() else parsers.flatMap { it.firstTokens }.toSet()
) {

    private val parsersByFirstToken: Map<Token, List<Parser<T>>>? =
        if (parsers.hasUnknownFirstTokens()) null else mutableMapOf<Token, MutableList<Parser<T>>>()
            .apply {
                for (parser in parsers) {
                    for (token in parser.firstTokens) {
                        getOrPut(token) { mutableListOf() }.add(parser)
                    }
                }
            }

    override suspend fun ParsingScope.parse(): T {
        val currentToken = currentToken?.token ?: fail(NoMatchingToken(currentOffset))
        val parsers = if (parsersByFirstToken == null) parsers else {
            parsersByFirstToken[currentToken] ?: fail(NoViableAlternative(currentOffset))
        }
        for (parser in parsers) {
            val r = tryParse(parser)
            if (r is ParsedValue) return r.value
        }
        fail(NoViableAlternative(currentOffset))
    }

    companion object {
        private fun List<Parser<*>>.hasUnknownFirstTokens() = any { it.firstTokens.isEmpty() }
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
