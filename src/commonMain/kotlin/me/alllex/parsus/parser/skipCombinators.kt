package me.alllex.parsus.parser


/**
 * Creates a new parser that applies the given [parser], but ignores the result and returns `Unit`.
 *
 * ```kotlin
 * object : Grammar<String> {
 *     val title by regexToken("Mrs?\\.?\\s+")
 *     val surname by regexToken("\\w+")
 *     override val root by ignored(title) and surname
 * }
 * ```
 */
fun ignored(parser: Parser<*>): Parser<Unit> = SkipParser.of(parser)

/**
 * Creates a new parser that applies the given [parser], but ignores the result and returns `Unit`.
 *
 * ```kotlin
 * object : Grammar<String> {
 *     val title by regexToken("Mrs?\\.?\\s+")
 *     val surname by regexToken("\\w+")
 *     override val root by ignored(title) and surname
 * }
 * ```
 */
operator fun Parser<*>.unaryMinus(): Parser<Unit> = ignored(this)

/**
 * Creates a new parser that tries to apply the given [parser]
 * and fallbacks to returning null in case of failure.
 *
 * ```kotlin
 * object : Grammar<Tuple2<String?, String>> {
 *    val title by regexToken("Mrs?\\.?")
 *    val ws by regexToken("\\s+")
 *    val surname by regexToken("\\w+")
 *    override val root by maybe(title) * -maybe(ws) * surname
 */
fun <T : Any> maybe(parser: Parser<T>): Parser<T?> = optional(parser)

/**
 * Creates a new parser that tries to apply the given [parser]
 * and fallbacks to returning null in case of failure.
 *
 * ```kotlin
 * object : Grammar<Tuple2<String?, String>> {
 *    val title by regexToken("Mrs?\\.?\\s+")
 *    val surname by regexToken("\\w+")
 *    override val root by optional(title) * surname
 */
fun <T : Any> optional(parser: Parser<T>): Parser<T?> = parser {
    poll(parser)
}

/**
 * Runs the [parser] and returns its result or null in case of failure.
 */
fun <R : Any> ParsingScope.tryOrNull(parser: Parser<R>): R? = tryParse(parser).getOrElse { null }

/**
 * Runs the [parser] and returns its result or null in case of failure.
 */
fun <R : Any> ParsingScope.poll(parser: Parser<R>): R? = tryOrNull(parser)

/**
 * Executes given parser, ignoring the result.
 */
suspend fun ParsingScope.skip(p: Parser<*>): IgnoredValue {
    p() // execute parser, but ignore the result
    return IgnoredValue
}


/**
 * A wrapping parser that is used as a marker to denote parsed value that is ignored, e.g. by [TupleParser].
 */
@Suppress("UNCHECKED_CAST")
internal class SkipParser<out T> private constructor(
    private val parser: Parser<T>
) : ParserImpl<Unit>(firstTokens = parser.firstTokens) {

    @Suppress("UNUSED_VARIABLE")
    override suspend fun ParsingScope.parse() {
        val ignored = parser()
    }

    override fun toString(): String {
        return "SkipParser(parser=$parser)"
    }

    companion object {
        fun <T> of(parser: Parser<T>): Parser<Unit> = when (parser) {
            is SkipParser<*> -> parser as SkipParser<Unit>
            else -> SkipParser(parser)
        }
    }
}
