package me.alllex.parsus.token

/**
 * Most basic kind of token, represented as a constant [literal] that is expected
 * to appear in the input.
 */
class LiteralTokenMatcher(
    val literal: String,
) : TokenMatcher {

    init {
        require(literal.isNotEmpty()) { "text must not be empty" }
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        return if (input.startsWith(literal, fromIndex)) literal.length else 0
    }

    override val firstChars: String = "${literal[0]}"

    override fun toString(): String = "LiteralTokenMatcher('$literal')"
}

fun literalToken(
    literal: String,
    name: String? = null,
    skip: Boolean = false,
): Token<LiteralTokenMatcher> {

    return Token(LiteralTokenMatcher(literal), name, skip)
}
