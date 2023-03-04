package me.alllex.parsus.parser

/**
 * Parser executes a procedure of converting a portion of input into a value.
 *
 * Parser either succeeds returning a [parsed value][ParsedValue], or fails returning an [error][ParseError].
 * The [scope][ParsingScope] is required to orchestrate composed parsers and propagate errors.
 *
 * The best way of constructing a parser is using a shorthand [parser] function.
 * ```kotlin
 * object : Grammar<*> {
 *     val a by literalToken("a")
 *     val b by literalToken("b")
 *     val p1 by parser { lexeme(a) }
 *     val p2 by parser { lexeme(b) }
 *     override val root by parser {
 *       val t1 = p1().text
 *       val t2 = p2().text
 *       "$t1$t2"
 *     }
 * }
 * ```
 */
interface Parser<out T> {
    suspend fun ParsingScope.parse(): T
}

/**
 * Converts given [block] into a parser.
 */
inline fun <T> parser(crossinline block: suspend ParsingScope.() -> T): Parser<T> = object : Parser<T> {
    override suspend fun ParsingScope.parse(): T = block()
}

/**
 * Applies given function to the result of [this] parser.
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

