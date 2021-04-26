package me.alllex.parsus.token

/**
 * Token is a semantic unit in the parsing process.
 *
 * A token is associated with a pattern that is used to [match]
 * a fragment of the input during parsing.
 */
fun interface TokenMatcher {

    /**
     * Tries to match input [from][fromIndex] given position,
     * returning the length of the matched fragment.
     *
     * If returned length is not positive, it means this token
     * does not match the input in given position.
     */
    fun match(input: CharSequence, fromIndex: Int): Int

    /**
     * List of characters that *can* be the first characters for this matcher.
     *
     * Lexer implementations can take advantage of this to match tokens more efficiently.
     */
    val firstChars: String get() = ""
}

inline fun matcher(
    firstChars: String,
    crossinline matcher: (CharSequence, Int) -> Int
): TokenMatcher {
    return object : TokenMatcher {
        override fun match(input: CharSequence, fromIndex: Int) = matcher(input, fromIndex)
        override val firstChars: String = firstChars
    }
}

inline fun matchingToken(
    name: String? = null,
    skip: Boolean = false,
    firstChars: String = "",
    crossinline matcher: (CharSequence, Int) -> Int
): Token<TokenMatcher> {

    return Token(matcher(firstChars, matcher), name, skip)
}
