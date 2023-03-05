package me.alllex.parsus.token

import me.alllex.parsus.parser.GrammarContext

/**
 * A token that [matches] the input using a [regex].
 */
class RegexToken(
    regex: Regex,
    name: String? = null,
    ignored: Boolean = false,
) : Token(name, ignored) {

    private val pattern: String = regex.pattern
    private val regex: Regex = prependPatternWithInputStart(pattern, regex.options)
//    private val matcher: Matcher = this.regex.toPattern().matcher("")
    //private val matcher: RegexMatcher = this.regex.toMatcher()

    private fun prependPatternWithInputStart(patternString: String, options: Set<RegexOption>): Regex {
        return if (patternString.startsWith(inputStartPrefix))
            patternString.toRegex(options)
        else {
//            val newlineAfterComments = if (RegexOption.COMMENTS in options) "\n" else ""
//            val patternToEmbed = if (RegexOption.LITERAL in options) Regex.escape(patternString) else patternString
//            ("$inputStartPrefix(?:$patternToEmbed$newlineAfterComments)").toRegex(options - RegexOption.LITERAL)
          "$inputStartPrefix(?:$patternString)".toRegex(options)
          // TODO is the warning correct?
          //      > Unnecessary non-capturing group '(?:patternString)'
        }
    }

    override fun match(input: CharSequence, fromIndex: Int): Int {
        val result = regex.find(input, fromIndex) ?: return 0
        return result.range.last + 1

        //matcher.reset(input).region(fromIndex, input.length)
        //if (!matcher.find()) return 0
        //val end = matcher.end()
        //return  end - fromIndex
    }

    override fun toString(): String = "${name ?: ""} [$pattern]" + if (ignored) " [ignorable]" else ""

    companion object {
        private const val inputStartPrefix = "" // "\\A" // TODO '\A' doesn't work on JS?
    }
}

fun GrammarContext.regexToken(
//    @Language("RegExp") // TODO @Language replacement
    pattern: String,
    name: String? = null,
    ignored: Boolean = false
): RegexToken = RegexToken(Regex(pattern), name, ignored)

@Suppress("unused")
fun GrammarContext.regexToken(
    regex: Regex,
    name: String? = null,
    ignored: Boolean = false
): RegexToken = RegexToken(regex, name, ignored)

//internal expect fun Regex.toMatcher() : RegexMatcher
//
//internal interface RegexMatcher {
//  fun reset(input: CharSequence): RegexMatcher
//  fun region(fromIndex: Int, length: Int)
//  fun find(): Boolean
//  fun end(): Int
//}
