package me.alllex.parsus.token

import me.alllex.parsus.parser.Grammar

/**
 * Most basic kind of token, represented as a constant [string] that is expected
 * to appear in the input.
 */
class LiteralToken(
    val string: String,
    name: String? = null,
    ignored: Boolean = false,
    val ignoreCase: Boolean = false,
) : Token(name, ignored) {

    init {
        require(string.isNotEmpty()) { "text must not be empty" }
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        if (input.startsWith(string, fromIndex, ignoreCase)) {
            return string.length
        }
        return 0
    }

    override val firstChars: String = "${string[0]}"

    override fun toString(): String = "LiteralToken('$string')"
}

/**
 * Creates and registers a literal token in this grammar.
 *
 * This is the most basic form of a token, defined by a constant [text] that is expected
 * to appear in the input.
 */
fun Grammar<*>.literalToken(
    text: String,
    name: String? = null,
    ignored: Boolean = false,
    ignoreCase: Boolean = false,
): LiteralToken = LiteralToken(text, name, ignored, ignoreCase).also { register(it) }
