package me.alllex.parsus.parser

import kotlin.reflect.KProperty0

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

@PublishedApi
internal abstract class ParserImpl<out T>(var name: String? = null) : Parser<T> {
    override fun toString(): String = name ?: super.toString()
}

/**
 * Converts given [block] into a parser.
 */
inline fun <T> parser(
    name: String,
    crossinline block: suspend ParsingScope.() -> T
): Parser<T> {
    return object : ParserImpl<T>(name) {
        override suspend fun ParsingScope.parse(): T = block()
    }
}

inline fun <T> parser(
    crossinline block: suspend ParsingScope.() -> T
): Parser<T> {
    return object : ParserImpl<T>() {
        override suspend fun ParsingScope.parse(): T = block()
    }
}

fun <T> ref(
    parserProperty: KProperty0<Parser<T>>
) : Parser<T> {
    return object : ParserImpl<T>() {
        override suspend fun ParsingScope.parse(): T = parserProperty().invoke()
        override fun toString(): String = "ref(${parserProperty.name})"
    }
}
