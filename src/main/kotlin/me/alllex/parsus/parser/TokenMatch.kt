package me.alllex.parsus.parser

import me.alllex.parsus.token.Token
import me.alllex.parsus.token.TokenMatcher

/**
 * A match of the [token] that starts at the [offset] position in the input
 * and is of specified [length].
 */
data class TokenMatch<out T : TokenMatcher>(
    val token: Token<T>,
    val offset: Int,
    val length: Int,
)
