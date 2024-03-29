package me.alllex.parsus.parser


internal class TupleParser<out T>(
    internal val parsers: List<Parser<Any?>>,
    internal val transform: (List<Any?>) -> T,
) : ParserImpl<T>(
    null,
    firstTokens = parsers.first().firstTokens
) {

    val arity: Int get() = parsers.count { it !is SkipParser<*> }

    override suspend fun ParsingScope.parse(): T {
        val items = buildList {
            for (parser in parsers) {
                if (parser is SkipParser<*>) {
                    parser()
                } else {
                    val item = parser()
                    add(item)
                }
            }
        }

        return transform(items)
    }

    override fun toString(): String {
        return "TupleParser(parsers=$parsers)"
    }

}

internal fun Parser<*>.tryUnwrapTupleParsers(): List<Parser<Any?>> =
    if (this is TupleParser<*>) parsers else listOf(this)

internal fun <T> retuple(parser1: Parser<Any?>, parser2: Parser<Any?>, transform: (List<Any?>) -> T): TupleParser<T> {
    val newParsers = parser1.tryUnwrapTupleParsers() + parser2
    return TupleParser(newParsers, transform)
}
