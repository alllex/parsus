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
