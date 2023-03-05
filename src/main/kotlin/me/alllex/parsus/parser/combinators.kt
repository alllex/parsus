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
infix fun <R> Parser<R>.or(p: Parser<R>): Parser<R> = parser { choose(this@or, p) }

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

fun <T> Parser<T>.between(left: Parser<*>, right: Parser<*>): Parser<T> = parser {
    left()
    val result = this@between()
    right()
    result
}

/**
 * Returns the result of [the first parser][p1] if parsing succeeds,
 * otherwise returns the result of [the second parser][p2].
 */
suspend fun <R> ParsingScope.choose(p1: Parser<R>, p2: Parser<R>): R {
    val startOffset = currentOffset
    var r = tryParse(p1)
    if (r is ParsedValue) return r.value

    r = tryParse(p2)
    if (r is ParsedValue) return r.value
    fail(NoViableAlternative(startOffset))
}

@Deprecated("Use `choose` instead", ReplaceWith("this.choose(p1, p2)"), DeprecationLevel.WARNING)
suspend fun <R> ParsingScope.any(p1: Parser<R>, p2: Parser<R>): R = choose(p1, p2)

suspend fun <R> ParsingScope.choose(p: Parser<R>, vararg ps: Parser<R>): R = choose(p, ps.toList())

@Deprecated("Use `choose` instead", ReplaceWith("this.choose(p, ps)"), DeprecationLevel.WARNING)
suspend fun <R> ParsingScope.any(p: Parser<R>, vararg ps: Parser<R>): R = choose(p, *ps)

suspend fun <R> ParsingScope.choose(p: Parser<R>, ps: List<Parser<R>>): R {
    if (ps.isEmpty()) return p()

    val startOffset = this.currentOffset
    for (i in -1..ps.lastIndex) {
        val alt = if (i == -1) p else ps[i]
        val r = tryParse(alt)
        if (r is ParsedValue) return r.value
    }

    fail(NoViableAlternative(startOffset))
}

@Deprecated("Use `choose` instead", ReplaceWith("this.choose(p, ps)"), DeprecationLevel.WARNING)
suspend fun <R> ParsingScope.any(p: Parser<R>, ps: List<Parser<R>>): R = choose(p, ps)

suspend fun <R : Any> ParsingScope.tryOrNull(p: Parser<R>): R? = tryParse(p).getOrElse { null }

suspend fun <R : Any> ParsingScope.poll(p: Parser<R>): R? = tryOrNull(p)

@Deprecated("Use `poll` instead", ReplaceWith("this.poll(p)"), DeprecationLevel.WARNING)
suspend fun <R : Any> ParsingScope.trying(p: Parser<R>): R? = tryParse(p).getOrElse { null }

/**
 * Executes given parser, ignoring the result.
 */
suspend fun ParsingScope.skip(p: Parser<*>): IgnoredValue {
    p() // execute parser, but ignore the result
    return IgnoredValue
}

@Deprecated("Use `skip` instead", ReplaceWith("this.skip(p)"), DeprecationLevel.WARNING)
suspend inline fun ParsingScope.ignoring(p: Parser<*>): IgnoredValue = skip(p)

/**
 * Returns true if the parser executes successfully and false otherwise.
 */
suspend fun ParsingScope.checkPresent(p: Parser<Any>): Boolean = tryOrNull(p) != null

@Deprecated("Use `checkPresent` instead", ReplaceWith("this.checkPresent(p)"), DeprecationLevel.WARNING)
suspend fun ParsingScope.having(p: Parser<Any>): Boolean = checkPresent(p)

@Deprecated("Use `poll` instead", ReplaceWith("this.poll(p)"), DeprecationLevel.WARNING)
suspend fun <R : Any> ParsingScope.zeroOrOne(p: Parser<R>): R? = tryOrNull(p)

suspend fun <R : Any> ParsingScope.repeatOneOrMore(p: Parser<R>): List<R> = repeat(p, atLeast = 1)

@Deprecated("Use `repeatOneOrMore` instead", ReplaceWith("this.repeatOneOrMore(p)"), DeprecationLevel.WARNING)
suspend fun <R : Any> ParsingScope.oneOrMore(p: Parser<R>): List<R> = repeatOneOrMore(p)

suspend fun <R : Any> ParsingScope.repeatZeroOrMore(p: Parser<R>): List<R> = repeat(p, atLeast = 0)

@Deprecated("Use `repeatZeroOrMore` instead", ReplaceWith("this.repeatZeroOrMore(p)"), DeprecationLevel.WARNING)
suspend fun <R : Any> ParsingScope.zeroOrMore(p: Parser<R>): List<R> = repeatZeroOrMore(p)

suspend fun <R : Any> ParsingScope.repeat(p: Parser<R>, atLeast: Int, atMost: Int = -1): List<R> {
    require(atLeast >= 0) { "atLeast must not be negative" }
    require(atMost == -1 || atLeast <= atMost) { "atMost has invalid value" }

    val startOffset = currentOffset
    val results = mutableListOf<R>()
    var repetition = 0
    while (atMost == -1 || repetition < atMost) {
        results += poll(p) ?: break
        repetition++
    }

    if (repetition < atLeast) fail(NotEnoughRepetition(startOffset, atLeast, repetition))
    return results
}

@Deprecated("Use `repeat` instead", ReplaceWith("this.repeat(p, atLeast, atMost)"), DeprecationLevel.WARNING)
suspend fun <R : Any> ParsingScope.repeating(p: Parser<R>, atLeast: Int, atMost: Int = -1): List<R> = repeat(p, atLeast, atMost)

suspend fun <T : Any> ParsingScope.split(
    term: Parser<T>,
    separator: Parser<Any>,
    allowEmpty: Boolean = true
): List<T> {

    val values = mutableListOf<T>()
    values += if (!allowEmpty) term() else poll(term) ?: return emptyList()
    while (true) {
        poll(separator) ?: break
        values += term()
    }
    return values
}

@Deprecated("Use `split` instead", ReplaceWith("this.split(p)"), DeprecationLevel.WARNING)
suspend fun <T : Any> ParsingScope.separated(
    term: Parser<T>,
    separator: Parser<Any>,
    allowEmpty: Boolean = true
): List<T> = split(term, separator, allowEmpty)

suspend inline fun <T : Any, S : Any> ParsingScope.reduce(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T {
    var l: T = term()
    while (true) {
        val o = poll(operator) ?: break
        val r = term()
        l = transform(l, o, r)
    }
    return l
}

@Deprecated("Use `reduce` instead", ReplaceWith("this.reduce(term, operator, transform)"), DeprecationLevel.WARNING)
suspend inline fun <T : Any, S : Any> ParsingScope.leftAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T = reduce(term, operator, transform)

suspend inline fun <T : Any, S : Any> ParsingScope.reduceRight(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T {
    val stack = mutableListOf<Pair<T, S>>()
    var t = term()
    while (true) {
        val o = poll(operator) ?: break
        stack += t to o
        t = term()
    }

    for ((l, o) in stack.asReversed()) {
        t = transform(l, o, t)
    }
    return t
}

@Deprecated("Use `reduceRight` instead", ReplaceWith("this.reduceRight(term, operator, transform)"), DeprecationLevel.WARNING)
suspend inline fun <T : Any, S : Any> ParsingScope.rightAssociative(
    term: Parser<T>,
    operator: Parser<S>,
    transform: (T, S, T) -> T
): T = reduceRight(term, operator, transform)
