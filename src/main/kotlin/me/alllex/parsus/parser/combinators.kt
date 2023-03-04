package me.alllex.parsus.parser

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
infix fun <R> Parser<R>.or(p: Parser<R>): Parser<R> = parser { any(this@or, p) }

/**
 * Applies given function to the result of [this] parser.
 *
 * ```kotlin
 *  val int by regexToken("\\d+")
 *  val number by parser { int() } map { it.text.toInt() }
 * ```
 */
inline infix fun <T, R> Parser<T>.map(crossinline f: ParsingScope.(T) -> R): Parser<R> = parser { f(parse()) }

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
 * Returns the result of [the first parser][p1] if parsing succeeds,
 * otherwise returns the result of [the second parser][p2].
 */
suspend fun <R> ParsingScope.any(p1: Parser<R>, p2: Parser<R>): R {
    val startOffset = currentOffset
    val r1 = tryParse(p1)
    if (r1 is ParsedValue) return r1.value

    val r2 = tryParse(p2)
    if (r2 is ParsedValue) return r2.value

    fail(NoViableAlternative(startOffset))
}

suspend fun <R> ParsingScope.any(p: Parser<R>, vararg ps: Parser<R>): R = any(p, ps.toList())

suspend fun <R> ParsingScope.any(p: Parser<R>, ps: List<Parser<R>>): R {
    if (ps.isEmpty()) return p()

    val startOffset = this.currentOffset
    for (i in -1..ps.lastIndex) {
        val alt = if (i == -1) p else ps[i]
        val r = tryParse(alt)
        if (r is ParsedValue) return r.value
    }

    fail(NoViableAlternative(startOffset))
}

suspend fun <R : Any> ParsingScope.trying(p: Parser<R>): R? = tryParse(p).getOrElse { null }

/**
 * Executes given parser, ignoring the result.
 */
suspend fun ParsingScope.ignoring(p: Parser<*>): IgnoredValue {
    p() // execute parser, but ignore the result
    return IgnoredValue
}

suspend fun ParsingScope.having(p: Parser<Any>): Boolean = trying(p) != null

suspend fun <R : Any> ParsingScope.zeroOrOne(p: Parser<R>): R? = trying(p)

suspend fun <R : Any> ParsingScope.oneOrMore(p: Parser<R>): List<R> = repeating(p, atLeast = 1)

suspend fun <R : Any> ParsingScope.zeroOrMore(p: Parser<R>): List<R> = repeating(p, atLeast = 0)

suspend fun <R : Any> ParsingScope.repeating(p: Parser<R>, atLeast: Int, atMost: Int = -1): List<R> {
    require(atLeast >= 0) { "atLeast must not be negative" }
    require(atMost == -1 || atLeast <= atMost) { "atMost has invalid value" }

    val startOffset = currentOffset
    val results = mutableListOf<R>()
    var repetition = 0
    while (atMost == -1 || repetition < atMost) {
        results += trying(p) ?: break
        repetition++
    }

    if (repetition < atLeast) fail(NotEnoughRepetition(startOffset, atLeast, repetition))
    return results
}

suspend fun <T : Any> ParsingScope.separated(
    term: Parser<T>,
    separator: Parser<Any>,
    allowEmpty: Boolean = true
): List<T> {

    val values = mutableListOf<T>()
    values += if (!allowEmpty) term() else trying(term) ?: return emptyList()
    while (true) {
        trying(separator) ?: break
        values += term()
    }
    return values
}

suspend inline fun <T : Any, S : Any> ParsingScope.leftAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T {
    var l: T = term()
    while (true) {
        val o = trying(operator) ?: break
        val r = term()
        l = transform(l, o, r)
    }
    return l
}

suspend inline fun <T : Any, S : Any> ParsingScope.rightAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T {
    val stack = mutableListOf<Pair<T, S>>()
    var t = term()
    while (true) {
        val o = trying(operator) ?: break
        stack += t to o
        t = term()
    }

    for ((l, o) in stack.asReversed()) {
        t = transform(l, o, t)
    }
    return t
}
