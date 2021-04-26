package me.alllex.parsus.token

import me.alllex.parsus.parser.ParseResult
import me.alllex.parsus.parser.Parser
import me.alllex.parsus.parser.ParsingScope
import me.alllex.parsus.parser.TokenMatch


/**
 * Token used within a [Grammar].
 */
data class Token<out T : TokenMatcher>(
    val matcher: T,
    var name: String? = null,
    val skip: Boolean = false
) : Parser<TokenMatch<T>> {

    override suspend fun ParsingScope.parse(): ParseResult<TokenMatch<T>> {
        return rawToken(this@Token)
    }
}
