package me.alllex.parsus.token

import me.alllex.parsus.parser.Grammar

/**
 * Creates and registers a function-defined token in this grammar.
 *
 * This is the most general form of a token, defined by a function that [matches][matcher] the input.
 */
inline fun Grammar<*>.token(
    name: String = "{lambda}",
    ignored: Boolean = false,
    firstChars: String = "",
    crossinline matcher: (input: CharSequence, fromIndex: Int) -> Int
): Token {
    return object : Token(name, ignored) {
        override fun match(input: CharSequence, fromIndex: Int) = matcher(input, fromIndex)
        override val firstChars: String = firstChars
        override fun toString() = "LambdaToken($name${if (ignored) " [ignored]" else ""})"
    }.also {
        register(it)
    }
}
