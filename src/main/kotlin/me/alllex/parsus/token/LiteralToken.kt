package me.alllex.parsus.token

import me.alllex.parsus.parser.GrammarContext

/**
 * Most basic kind of token, represented as a constant [string] that is expected
 * to appear in the input.
 */
class LiteralToken(
    val string: String,
    name: String? = null,
    ignored: Boolean = false
) : Token(name, ignored) {

    init {
        require(string.isNotEmpty()) { "text must not be empty" }
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        if (input.startsWith(string, fromIndex)) {
            return string.length
        }
        return 0
    }

    override val firstChars: String = "${string[0]}"

    override fun toString(): String = "LiteralToken('$string')"
}

@Suppress("unused")
fun GrammarContext.literalToken(
    text: String,
    name: String? = null,
    ignored: Boolean = false
): LiteralToken = LiteralToken(text, name, ignored)
