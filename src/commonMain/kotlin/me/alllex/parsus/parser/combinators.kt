package me.alllex.parsus.parser

/**
 * Creates a parser from a pair of parsers, returning a pair of their results.
 */
infix fun <A, B> Parser<A>.and(p: Parser<B>): Parser<Pair<A, B>> = parser(firstTokens) {
    val a = this@and()
    val b = p()
    a to b
}

/**
 * Applies given function to the result of [this] parser.
 *
 * ```kotlin
 *  val int by regexToken("\\d+")
 *  val number by parser { int() } map { it.text.toInt() }
 * ```
 */
inline infix fun <T, R> Parser<T>.map(crossinline f: ParsingScope.(T) -> R): Parser<R> =
    parser(this.firstTokens) { f(parse()) }

/**
 * When parsing is successful, simply returns given value.
 *
 * It is useful when a parsed token needs to be substituted with a semantic value.
 * ```kotlin
 * interface Marker
 * object MainMarker : Marker
 *
 * object G : Grammar<Marker> {
 *     val main by literalToken("main")
 *     override val root by parser { main() } map MainMarker
 * }
 * ```
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun <T, R> Parser<T>.map(v: R): Parser<R> = map { v }

/**
 * Returns the result of this parser executing [left] and [right] parsers before and after it respectively.
 */
fun <T> Parser<T>.between(left: Parser<*>, right: Parser<*>): Parser<T> = parser(left.firstTokens) {
    left()
    val result = this@between()
    right()
    result
}

/**
 * Returns a new parser that applies the given [parser], but ignores the result.
 *
 * ```kotlin
 * object : Grammar<String> {
 *     val title by regexToken("Mrs?\\.?\\s+")
 *     val surname by regexToken("\\w+")
 *     override val root by ignored(title) * surname
 * }
 * ```
 */
fun ignored(parser: Parser<*>): Parser<Unit> = parser map Unit

/**
 * Returns a new parser that tries to apply the given [parser]
 * and fallbacks to returning null in case of failure.
 *
 * ```kotlin
 * object : Grammar<Pair<String?, String>> {
 *    val title by regexToken("Mrs?\\.?\\s+")
 *    val surname by regexToken("\\w+")
 *    override val root by optional(title) * surname
 */
fun <T : Any> optional(parser: Parser<T>): Parser<T?> = parser {
    poll(parser)
}

fun <T : Any> repeated(p: Parser<T>, atLeast: Int, atMost: Int = -1): Parser<List<T>> =
    parser(if (atLeast > 0) p.firstTokens else emptySet()) {
        repeat(p, atLeast, atMost)
    }

fun <T : Any> zeroOrMore(p: Parser<T>) = repeated(p, atLeast = 0)

fun <T : Any> oneOrMore(p: Parser<T>) = repeated(p, atLeast = 1)

infix fun <T : Any> Int.times(parser: Parser<T>): Parser<List<T>> =
    repeated(parser, atLeast = this, atMost = this)

infix fun <T : Any> IntRange.times(parser: Parser<T>): Parser<List<T>> =
    repeated(parser, atLeast = first, atMost = last)

infix fun <T : Any> Int.timesOrMore(parser: Parser<T>): Parser<List<T>> = repeated(parser, atLeast = this)

fun <T : Any, S : Any> separated(
    term: Parser<T>,
    separator: Parser<S>,
    allowEmpty: Boolean = true,
    trailingSeparator: Boolean = false,
): Parser<List<T>> =
    parser(if (!allowEmpty) term.firstTokens else emptySet()) {
        split(term, separator, allowEmpty, trailingSeparator)
    }

inline fun <T : Any, S : Any> leftAssociative(
    term: Parser<T>,
    separator: Parser<S>,
    crossinline transform: (T, S, T) -> T
): Parser<T> =
    parser(term.firstTokens) {
        reduce(term, separator, transform)
    }

inline fun <T : Any, S : Any> leftAssociative(
    term: Parser<T>,
    separator: Parser<S>,
    crossinline transform: (T, T) -> T
): Parser<T> = leftAssociative(term, separator) { a, _, b -> transform(a, b) }

inline fun <T : Any, S : Any> rightAssociative(
    term: Parser<T>,
    separator: Parser<S>,
    crossinline transform: (T, S, T) -> T
): Parser<T> =
    parser(term.firstTokens) {
        reduceRight(term, separator, transform)
    }

inline fun <T : Any, S : Any> rightAssociative(
    term: Parser<T>,
    separator: Parser<S>,
    crossinline transform: (T, T) -> T
): Parser<T> = rightAssociative(term, separator) { a, _, b -> transform(a, b) }
