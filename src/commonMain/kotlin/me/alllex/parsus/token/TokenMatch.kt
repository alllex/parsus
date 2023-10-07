package me.alllex.parsus.token

/**
 * A match of the [token] that starts at the [offset] position in the input
 * and is of specified [length].
 */
data class TokenMatch(
    val token: Token,
    val offset: Int,
    val length: Int,
) {
    /**
     * Offset of the next character after the match.
     */
    val nextOffset: Int get() = offset + length
}
