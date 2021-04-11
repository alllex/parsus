package me.alllex.parsus.token

import me.alllex.parsus.parser.GrammarContext

/**
 * Most general form of a token, defined by a function that [matches] the input.
 */
@Suppress("unused")
inline fun GrammarContext.token(
    name: String = "{lambda}",
    ignored: Boolean = false,
    firstChars: String = "",
    crossinline matcher: (CharSequence, Int) -> Int
): Token {
    return object : Token(name, ignored) {
        override fun match(input: CharSequence, fromIndex: Int) = matcher(input, fromIndex)
        override val firstChars: String = firstChars
        override fun toString() = name + if (ignored) " [ignorable]" else ""
    }
}
