package me.alllex.parsus.token

import me.alllex.parsus.parser.ParseResult
import me.alllex.parsus.parser.Parser
import me.alllex.parsus.parser.ParsingScope

/**
 * Token is a semantic unit in the parsing process.
 *
 * A token is associated with a pattern that is used to [match]
 * a fragment of the input during parsing.
 */
abstract class Token(
    var name: String? = null,
    val ignored: Boolean = false
) : Parser<TokenMatch> {

    /**
     * Matches the pattern of this token against the input,
     * and returns the length of the matched lexeme.
     */
    abstract fun match(input: CharSequence, fromIndex: Int): Int

    override suspend fun ParsingScope.parse(): ParseResult<TokenMatch> = rawToken(this@Token)

    /**
     * List of characters that *can* be the first characters in this token's underlying pattern.
     *
     * Lexer implementations can take advantage of this to match tokens more efficiently.
     */
    open val firstChars: String = ""

    override fun toString(): String {
        if (name != null) return "Token($name)" + if (ignored) " [ignored]" else ""
        return super.toString()
    }
}
