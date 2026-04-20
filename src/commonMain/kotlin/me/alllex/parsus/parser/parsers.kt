package me.alllex.parsus.parser

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

suspend fun <R> ParsingScope.choose(p: Parser<R>, vararg ps: Parser<R>): R = choose(p, ps.toList())

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

/**
 * Returns true if the parser executes successfully (consuming input) and false otherwise (not consuming any input).
 */
fun ParsingScope.has(p: Parser<Any>): Boolean = checkPresent(p)

/**
 * Returns true if the parser executes successfully (consuming input) and false otherwise (not consuming any input).
 */
fun ParsingScope.checkPresent(p: Parser<Any>): Boolean = tryOrNull(p) != null

suspend fun <R : Any> ParsingScope.repeatOneOrMore(p: Parser<R>): List<R> = repeat(p, atLeast = 1)

suspend fun <R : Any> ParsingScope.repeatZeroOrMore(p: Parser<R>): List<R> = repeat(p, atLeast = 0)

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

suspend fun <T : Any> ParsingScope.split(
    term: Parser<T>,
    separator: Parser<Any>,
    allowEmpty: Boolean = true,
    trailingSeparator: Boolean = false,
): List<T> {

    val values = mutableListOf<T>()
    values += if (!allowEmpty) term() else poll(term) ?: return emptyList()
    values += repeat(ignored(separator) * term, atLeast = 0)
    if (trailingSeparator) {
        poll(separator)
    }
    return values
}

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
