package me.alllex.parsus.token

import java.util.regex.Matcher

/**
 * A token that [matches] the input using a [regex].
 */
class RegexTokenMatcher(regex: Regex) : TokenMatcher {

    private val pattern: String = regex.pattern
    private val regex: Regex = prependPatternWithInputStart(pattern, regex.options)
    private val matcher: Matcher = this.regex.toPattern().matcher("")

    private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption>): Regex {
        return if (patternString.startsWith(inputStartPrefix))
            patternString.toRegex(options)
        else {
            val newlineAfterComments = if (RegexOption.COMMENTS in options) "\n" else ""
            val patternToEmbed = if (RegexOption.LITERAL in options) Regex.escape(patternString) else patternString
            ("$inputStartPrefix(?:$patternToEmbed$newlineAfterComments)").toRegex(options - RegexOption.LITERAL)
        }
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        matcher.reset(input).region(fromIndex, input.length)
        if (!matcher.find()) return 0

        val end = matcher.end()
        return end - fromIndex
    }

    companion object {
        private const val inputStartPrefix = "\\A"
    }
}

fun regexToken(
    regex: String,
    name: String? = null,
    skip: Boolean = false,
): Token<RegexTokenMatcher> {

    val re = RegexTokenMatcher(regex.toRegex())
    return Token(re, name, skip)
}
